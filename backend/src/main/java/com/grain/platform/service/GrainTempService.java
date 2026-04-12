package com.grain.platform.service;

import com.grain.platform.common.PageResult;
import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.dto.grain.GrainTempRecordFilterOptionsDto;
import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.dto.grain.GrainTempRecordUpsertRequest;
import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.entity.GrainTempPoint;
import com.grain.platform.entity.GrainTempRecord;
import com.grain.platform.entity.GrainTempSummary;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.mapper.GrainTempPointMapper;
import com.grain.platform.mapper.GrainTempRecordMapper;
import com.grain.platform.mapper.GrainTempSummaryMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GrainTempService {

    private final GrainTempPointMapper grainTempPointMapper;
    private final GrainTempRecordMapper grainTempRecordMapper;
    private final GrainTempSummaryMapper grainTempSummaryMapper;
    private final GrainTempImportService grainTempImportService;
    private final MetricService metricService;

    public GrainTempService(GrainTempPointMapper grainTempPointMapper,
                            GrainTempRecordMapper grainTempRecordMapper,
                            GrainTempSummaryMapper grainTempSummaryMapper,
                            GrainTempImportService grainTempImportService,
                            MetricService metricService) {
        this.grainTempPointMapper = grainTempPointMapper;
        this.grainTempRecordMapper = grainTempRecordMapper;
        this.grainTempSummaryMapper = grainTempSummaryMapper;
        this.grainTempImportService = grainTempImportService;
        this.metricService = metricService;
    }

    public List<GrainTempRecordItemDto> listRecords(Long warehouseId,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    String zoneCode,
                                                    Integer layerNo) {
        return grainTempRecordMapper.selectByCondition(
                warehouseId, startTime, endTime, zoneCode, layerNo, null, null, null, null);
    }

    public GrainTempRecordFilterOptionsDto recordFilterOptions(Long warehouseId) {
        // 筛选选项只返回当前仓库已有值，避免前端再自己去重并承担脏数据清洗。
        List<String> zoneCodes = grainTempRecordMapper.selectDistinctZoneCodes(warehouseId).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
        List<Integer> layerNos = grainTempRecordMapper.selectDistinctLayerNos(warehouseId).stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        List<Integer> pointNos = grainTempRecordMapper.selectDistinctPointNos(warehouseId).stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        return new GrainTempRecordFilterOptionsDto(zoneCodes, layerNos, pointNos);
    }

    public PageResult<GrainTempRecordItemDto> listRecordPage(Long warehouseId,
                                                             LocalDateTime startTime,
                                                             LocalDateTime endTime,
                                                             String zoneCode,
                                                             Integer layerNo,
                                                             Integer pointNo,
                                                             BigDecimal tempMin,
                                                             BigDecimal tempMax,
                                                             String keyword,
                                                             Integer pageNum,
                                                             Integer pageSize) {
        // 分页查询先做关键词与页码归一化，再统一裁到合法页，避免空结果时页码漂到末尾之外。
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        int finalPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = grainTempRecordMapper.countByCondition(
                warehouseId, startTime, endTime, zoneCode, layerNo, pointNo, tempMin, tempMax, kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<GrainTempRecordItemDto> list = grainTempRecordMapper.selectPageByCondition(
                warehouseId,
                startTime,
                endTime,
                zoneCode,
                layerNo,
                pointNo,
                tempMin,
                tempMax,
                kw,
                offset,
                finalPageSize
        );
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public List<GrainTempSummaryItemDto> listSummaries(Long warehouseId,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        return grainTempSummaryMapper.selectByCondition(warehouseId, startTime, endTime);
    }

    public PageResult<GrainTempSummaryItemDto> listSummaryPage(Long warehouseId,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime,
                                                               String warningLevel,
                                                               BigDecimal tempMin,
                                                               BigDecimal tempMax,
                                                               String keyword,
                                                               Integer pageNum,
                                                               Integer pageSize) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String finalWarningLevel = (warningLevel == null || warningLevel.isBlank()) ? null : warningLevel.trim();
        BigDecimal finalTempMin = tempMin;
        BigDecimal finalTempMax = tempMax;
        if (finalTempMin != null && finalTempMax != null && finalTempMin.compareTo(finalTempMax) > 0) {
            finalTempMin = tempMax;
            finalTempMax = tempMin;
        }

        int finalPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = grainTempSummaryMapper.countByCondition(
                warehouseId,
                startTime,
                endTime,
                finalWarningLevel,
                finalTempMin,
                finalTempMax,
                kw
        );
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<GrainTempSummaryItemDto> list = grainTempSummaryMapper.selectPageByCondition(
                warehouseId,
                startTime,
                endTime,
                finalWarningLevel,
                finalTempMin,
                finalTempMax,
                kw,
                offset,
                finalPageSize
        );
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public GrainTempImportResultDto importData(MultipartFile file, Long operatorUserId, Long allowedWarehouseId) throws IOException {
        return grainTempImportService.importData(file, operatorUserId, allowedWarehouseId);
    }

    public byte[] getImportTemplate() throws IOException {
        return grainTempImportService.getExcelTemplate();
    }

    public IdVO createRecord(GrainTempRecordUpsertRequest request, Long operatorUserId) {
        GrainTempPoint point = ensurePoint(request);
        GrainTempRecord record = new GrainTempRecord();
        record.setWarehouseId(request.warehouseId());
        record.setPointId(point.getId());
        record.setCollectedAt(request.collectedAt());
        record.setTemperatureValue(toDecimal(request.temperatureValue()));
        record.setSourceType("MANUAL");
        record.setBatchNo(null);
        record.setQualityFlag("NORMAL");
        record.setRemark(request.remark());
        record.setCreatedBy(operatorUserId);
        grainTempRecordMapper.insert(record);
        rebuildSummary(request.warehouseId(), request.collectedAt());
        return new IdVO(record.getId());
    }

    public IdVO updateRecord(Long id, GrainTempRecordUpsertRequest request, Long operatorUserId) {
        GrainTempRecord existing = requireRecord(id);
        Long oldWarehouseId = existing.getWarehouseId();
        LocalDateTime oldCollectedAt = existing.getCollectedAt();

        GrainTempPoint point = ensurePoint(request);
        existing.setWarehouseId(request.warehouseId());
        existing.setPointId(point.getId());
        existing.setCollectedAt(request.collectedAt());
        existing.setTemperatureValue(toDecimal(request.temperatureValue()));
        existing.setSourceType("MANUAL");
        existing.setBatchNo(null);
        existing.setQualityFlag("NORMAL");
        existing.setRemark(request.remark());
        existing.setCreatedBy(operatorUserId);
        grainTempRecordMapper.update(existing);

        // 原始记录一旦变更就重算对应时间点汇总，仓库或采集时间变了还要把新旧两个桶都补齐。
        rebuildSummary(oldWarehouseId, oldCollectedAt);
        if (!oldWarehouseId.equals(request.warehouseId()) || !oldCollectedAt.equals(request.collectedAt())) {
            rebuildSummary(request.warehouseId(), request.collectedAt());
        }
        return new IdVO(existing.getId());
    }

    public void deleteRecord(Long id) {
        GrainTempRecord existing = requireRecord(id);
        grainTempRecordMapper.deleteById(id);
        rebuildSummary(existing.getWarehouseId(), existing.getCollectedAt());
    }

    public Long getRecordWarehouseId(Long id) {
        return requireRecord(id).getWarehouseId();
    }

    public List<PredictionPointDto> listPredictionSeries(Long warehouseId,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         String targetType) {
        List<GrainTempSummary> rows = grainTempSummaryMapper.selectSeriesByTarget(warehouseId, startTime, endTime, targetType);
        return rows.stream()
                .map(item -> new PredictionPointDto(item.getCollectedAt(), selectTargetValue(item, targetType)))
                .filter(item -> item.value() != null)
                .toList();
    }

    private Double selectTargetValue(GrainTempSummary summary, String targetType) {
        String finalTargetType = (targetType == null || targetType.isBlank()) ? "AVG_TEMP" : targetType;
        return switch (finalTargetType) {
            case "LAYER_1_AVG" -> summary.getLayer1Avg() == null ? null : summary.getLayer1Avg().doubleValue();
            case "LAYER_2_AVG" -> summary.getLayer2Avg() == null ? null : summary.getLayer2Avg().doubleValue();
            case "LAYER_3_AVG" -> summary.getLayer3Avg() == null ? null : summary.getLayer3Avg().doubleValue();
            case "LAYER_4_AVG" -> summary.getLayer4Avg() == null ? null : summary.getLayer4Avg().doubleValue();
            default -> summary.getAvgTemp() == null ? null : summary.getAvgTemp().doubleValue();
        };
    }

    private GrainTempRecord requireRecord(Long id) {
        GrainTempRecord record = grainTempRecordMapper.selectEntityById(id);
        if (record == null) {
            throw new IllegalArgumentException("粮温记录不存在：" + id);
        }
        return record;
    }

    private GrainTempPoint ensurePoint(GrainTempRecordUpsertRequest request) {
        GrainTempPoint point = grainTempPointMapper.selectByUniqueKey(
                request.warehouseId(),
                request.zoneCode(),
                request.layerNo(),
                request.pointNo()
        );
        if (point != null) {
            return point;
        }

        GrainTempPoint newPoint = new GrainTempPoint();
        newPoint.setWarehouseId(request.warehouseId());
        newPoint.setProbeCode(request.probeCode() == null || request.probeCode().isBlank()
                ? "AUTO-" + request.zoneCode()
                : request.probeCode());
        newPoint.setZoneCode(request.zoneCode());
        newPoint.setLayerNo(request.layerNo());
        newPoint.setPointNo(request.pointNo());
        newPoint.setPointName(request.zoneCode() + "-" + request.layerNo() + "-" + request.pointNo() + "测点");
        newPoint.setStatus("ACTIVE");
        newPoint.setRemark(request.remark());
        grainTempPointMapper.insert(newPoint);
        return newPoint;
    }

    private void rebuildSummary(Long warehouseId, LocalDateTime collectedAt) {
        List<GrainTempRecordItemDto> records = grainTempRecordMapper.selectByWarehouseAndCollectedAt(warehouseId, collectedAt);
        if (records.isEmpty()) {
            grainTempSummaryMapper.deleteByWarehouseAndCollectedAt(warehouseId, collectedAt);
            return;
        }

        // 汇总重建统一收口均温、分层均温和预警口径，原始记录写操作后都复用这一条链路。
        SensorMetric metric = metricService.getMetric("temperature");
        GrainTempSummary summary = new GrainTempSummary();
        summary.setWarehouseId(warehouseId);
        summary.setCollectedAt(collectedAt);

        List<Double> values = records.stream()
                .map(GrainTempRecordItemDto::temperatureValue)
                .filter(item -> item != null)
                .toList();
        summary.setAvgTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
        summary.setMaxTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0)));
        summary.setMinTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0)));

        Map<Integer, List<GrainTempRecordItemDto>> byLayer = records.stream()
                .collect(Collectors.groupingBy(GrainTempRecordItemDto::layerNo));
        summary.setLayer1Avg(avgForLayer(byLayer.get(1)));
        summary.setLayer2Avg(avgForLayer(byLayer.get(2)));
        summary.setLayer3Avg(avgForLayer(byLayer.get(3)));
        summary.setLayer4Avg(avgForLayer(byLayer.get(4)));

        double threshold = metric.getMaxThreshold() == null ? 28.0 : metric.getMaxThreshold().doubleValue();
        if (summary.getMaxTemp().doubleValue() >= threshold) {
            summary.setWarningLevel("WARNING");
            summary.setWarningFlag(true);
            summary.setWarningMessage("检测到高温点，建议立即排查并通风降温");
        } else if (summary.getMaxTemp().doubleValue() >= threshold * 0.9) {
            summary.setWarningLevel("ATTENTION");
            summary.setWarningFlag(true);
            summary.setWarningMessage("最高粮温接近阈值，建议持续关注");
        } else {
            summary.setWarningLevel("NORMAL");
            summary.setWarningFlag(false);
            summary.setWarningMessage(null);
        }

        summary.setAnalysisResult(Boolean.TRUE.equals(summary.getWarningFlag()) ? "粮温关注" : "粮温正常");
        summary.setAnalysisRemark("由粮温原始记录变更后自动重算");
        grainTempSummaryMapper.upsert(summary);
    }

    private BigDecimal avgForLayer(List<GrainTempRecordItemDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return toDecimal(rows.stream()
                .map(GrainTempRecordItemDto::temperatureValue)
                .filter(item -> item != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));
    }

    private BigDecimal toDecimal(Double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
