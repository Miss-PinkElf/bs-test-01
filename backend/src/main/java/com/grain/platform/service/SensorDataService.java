package com.grain.platform.service;

import com.grain.platform.common.PageResult;
import com.grain.platform.common.ForbiddenException;
import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataImportResultDto;
import com.grain.platform.dto.sensor.SensorDataImportRowDto;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.dto.sensor.SensorDataUpdateRequest;
import com.grain.platform.dto.sensor.SensorTrendPointDto;
import com.grain.platform.dto.sensor.SensorTrendResponse;
import com.grain.platform.entity.SensorData;
import com.grain.platform.mapper.SensorDataMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SensorDataService {

    private final SensorDataMapper sensorDataMapper;
    private final SensorDataImportService sensorDataImportService;

    public SensorDataService(SensorDataMapper sensorDataMapper, SensorDataImportService sensorDataImportService) {
        this.sensorDataMapper = sensorDataMapper;
        this.sensorDataImportService = sensorDataImportService;
    }

    public List<SensorDataPointDto> list(Long warehouseId, String metricCode) {
        return sensorDataMapper.selectByCondition(warehouseId, metricCode, null);
    }

    public PageResult<SensorDataPointDto> listPage(Long warehouseId, String metricCode, String keyword, Integer pageNum, Integer pageSize) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        int finalPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = sensorDataMapper.countByCondition(warehouseId, metricCode, kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<SensorDataPointDto> list = sensorDataMapper.selectPageByCondition(warehouseId, metricCode, kw, offset, finalPageSize);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public SensorTrendResponse trend(Long warehouseId, String metricCode) {
        List<SensorTrendPointDto> points = sensorDataMapper.selectByCondition(warehouseId, metricCode, null)
                .stream()
                .map(item -> new SensorTrendPointDto(item.getCollectedAt(), item.getMetricValue()))
                .toList();
        return new SensorTrendResponse(points);
    }

    public IdVO create(SensorDataCreateRequest request, Long operatorUserId) {
        SensorData sensorData = new SensorData(
                null,
                request.warehouseId(),
                request.metricCode(),
                BigDecimal.valueOf(request.metricValue()),
                request.collectedAt() == null ? LocalDateTime.now() : request.collectedAt(),
                "MANUAL",
                null,
                "NORMAL",
                null,
                operatorUserId,
                null,
                null
        );
        sensorDataMapper.insert(sensorData);
        return new IdVO(sensorData.getId());
    }

    public IdVO update(Long id, SensorDataUpdateRequest request, Long operatorUserId) {
        SensorData sensorData = requireSensorData(id);
        sensorData.setWarehouseId(request.warehouseId());
        sensorData.setMetricCode(request.metricCode());
        sensorData.setMetricValue(BigDecimal.valueOf(request.metricValue()));
        sensorData.setCollectedAt(request.collectedAt() == null ? LocalDateTime.now() : request.collectedAt());
        sensorData.setSourceType("MANUAL");
        sensorData.setSourceBatchNo(null);
        sensorData.setQualityFlag("NORMAL");
        sensorData.setRemark(null);
        sensorData.setCreatedBy(operatorUserId);
        sensorDataMapper.update(sensorData);
        return new IdVO(sensorData.getId());
    }

    public void delete(Long id) {
        requireSensorData(id);
        sensorDataMapper.deleteById(id);
    }

    public SensorDataImportResultDto importData(MultipartFile file, Long operatorUserId, Long allowedWarehouseId) {
        try {
            List<SensorDataImportRowDto> rows = sensorDataImportService.parse(file);
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("导入文件为空或没有有效数据");
            }
            if (allowedWarehouseId != null
                    && rows.stream().anyMatch(item -> !allowedWarehouseId.equals(item.warehouseId()))) {
                throw new ForbiddenException("仓库管理员仅可导入所属仓库环境数据");
            }

            String batchNo = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            List<SensorData> sensorDataList = rows.stream()
                    .map(item -> new SensorData(
                            null,
                            item.warehouseId(),
                            item.metricCode(),
                            BigDecimal.valueOf(item.metricValue()),
                            item.collectedAt(),
                            "IMPORT",
                            batchNo,
                            "NORMAL",
                            item.remark(),
                            operatorUserId,
                            null,
                            null
                    ))
                    .toList();
            sensorDataMapper.insertBatch(sensorDataList);
            return new SensorDataImportResultDto(sensorDataList.size(), 0, batchNo, List.of());
        } catch (Exception ex) {
            List<String> errors = new ArrayList<>();
            errors.add(ex.getMessage());
            return new SensorDataImportResultDto(0, 1, null, errors);
        }
    }

    public String getImportTemplate() {
        return sensorDataImportService.getCsvTemplate();
    }

    public List<SensorDataPointDto> listRecentTemperatureHistory(Long warehouseId) {
        return sensorDataMapper.selectRecentByMetric(warehouseId, "temperature");
    }

    public Long getWarehouseId(Long id) {
        return requireSensorData(id).getWarehouseId();
    }

    private SensorData requireSensorData(Long id) {
        SensorData sensorData = sensorDataMapper.selectById(id);
        if (sensorData == null) {
            throw new IllegalArgumentException("环境数据不存在：" + id);
        }
        return sensorData;
    }
}
