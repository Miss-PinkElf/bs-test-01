package com.grain.platform.service;

import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.entity.GrainTempSummary;
import com.grain.platform.mapper.GrainTempRecordMapper;
import com.grain.platform.mapper.GrainTempSummaryMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrainTempService {

    private final GrainTempRecordMapper grainTempRecordMapper;
    private final GrainTempSummaryMapper grainTempSummaryMapper;
    private final GrainTempImportService grainTempImportService;

    public GrainTempService(GrainTempRecordMapper grainTempRecordMapper,
                            GrainTempSummaryMapper grainTempSummaryMapper,
                            GrainTempImportService grainTempImportService) {
        this.grainTempRecordMapper = grainTempRecordMapper;
        this.grainTempSummaryMapper = grainTempSummaryMapper;
        this.grainTempImportService = grainTempImportService;
    }

    public List<GrainTempRecordItemDto> listRecords(Long warehouseId,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    String zoneCode,
                                                    Integer layerNo) {
        return grainTempRecordMapper.selectByCondition(warehouseId, startTime, endTime, zoneCode, layerNo);
    }

    public List<GrainTempSummaryItemDto> listSummaries(Long warehouseId,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        return grainTempSummaryMapper.selectByCondition(warehouseId, startTime, endTime);
    }

    public GrainTempImportResultDto importData(MultipartFile file) throws IOException {
        return grainTempImportService.importData(file);
    }

    public String getImportTemplate() {
        return grainTempImportService.getCsvTemplate();
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
}
