package com.grain.platform.service;

import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.entity.GrainTempPoint;
import com.grain.platform.entity.GrainTempRecord;
import com.grain.platform.entity.GrainTempSummary;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.GrainTempPointMapper;
import com.grain.platform.mapper.GrainTempRecordMapper;
import com.grain.platform.mapper.GrainTempSummaryMapper;
import com.grain.platform.mapper.WarehouseMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrainTempImportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_TEMPLATE = "warehouseId,collectedAt,zoneCode,layerNo,pointNo,temperatureValue,probeCode,remark\n"
            + "1,2026-04-08 08:40:00,A,1,1,24.3,CABLE-A,第一层测点\n"
            + "1,2026-04-08 08:40:00,A,1,2,24.5,CABLE-A,第一层测点\n"
            + "1,2026-04-08 08:40:00,A,2,1,24.8,CABLE-A,第二层测点\n";
    private static final String UTF8_BOM = "\uFEFF";

    private final GrainTempPointMapper grainTempPointMapper;
    private final GrainTempRecordMapper grainTempRecordMapper;
    private final GrainTempSummaryMapper grainTempSummaryMapper;
    private final MetricService metricService;
    private final WarehouseMapper warehouseMapper;

    public GrainTempImportService(GrainTempPointMapper grainTempPointMapper,
                                  GrainTempRecordMapper grainTempRecordMapper,
                                  GrainTempSummaryMapper grainTempSummaryMapper,
                                  MetricService metricService,
                                  WarehouseMapper warehouseMapper) {
        this.grainTempPointMapper = grainTempPointMapper;
        this.grainTempRecordMapper = grainTempRecordMapper;
        this.grainTempSummaryMapper = grainTempSummaryMapper;
        this.metricService = metricService;
        this.warehouseMapper = warehouseMapper;
    }

    public GrainTempImportResultDto importData(MultipartFile file) throws IOException {
        List<ImportRow> rows = parse(file);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("粮温导入文件为空或没有有效数据");
        }

        Long warehouseId = rows.get(0).warehouseId();
        LocalDateTime collectedAt = rows.get(0).collectedAt();
        boolean sameHeader = rows.stream().allMatch(item -> item.warehouseId().equals(warehouseId) && item.collectedAt().equals(collectedAt));
        if (!sameHeader) {
            throw new IllegalArgumentException("当前粮温 MVP 导入要求同一文件中的 warehouseId 与 collectedAt 保持一致");
        }

        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库不存在：" + warehouseId);
        }

        String batchNo = "BATCH-GRAIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        List<GrainTempRecord> records = new ArrayList<>();

        for (ImportRow row : rows) {
            GrainTempPoint point = grainTempPointMapper.selectByUniqueKey(row.warehouseId(), row.zoneCode(), row.layerNo(), row.pointNo());
            if (point == null) {
                point = new GrainTempPoint();
                point.setWarehouseId(row.warehouseId());
                point.setProbeCode(row.probeCode() == null || row.probeCode().isBlank() ? "AUTO-" + row.zoneCode() : row.probeCode());
                point.setZoneCode(row.zoneCode());
                point.setLayerNo(row.layerNo());
                point.setPointNo(row.pointNo());
                point.setPointName(row.zoneCode() + "-" + row.layerNo() + "-" + row.pointNo() + "测点");
                point.setStatus("ACTIVE");
                point.setRemark(row.remark());
                grainTempPointMapper.insert(point);
            }

            GrainTempRecord record = new GrainTempRecord();
            record.setWarehouseId(row.warehouseId());
            record.setPointId(point.getId());
            record.setCollectedAt(row.collectedAt());
            record.setTemperatureValue(BigDecimal.valueOf(row.temperatureValue()));
            record.setSourceType("IMPORT");
            record.setBatchNo(batchNo);
            record.setQualityFlag("NORMAL");
            record.setRemark(row.remark());
            record.setCreatedBy(1L);
            records.add(record);
        }

        grainTempRecordMapper.upsertBatch(records);
        GrainTempSummary summary = buildSummary(warehouseId, collectedAt, rows, metricService.getMetric("temperature"));
        grainTempSummaryMapper.upsert(summary);

        return new GrainTempImportResultDto(
                batchNo,
                warehouseId,
                warehouse.getWarehouseName(),
                collectedAt.format(DATE_TIME_FORMATTER),
                rows.size(),
                true,
                summary.getWarningLevel(),
                summary.getWarningMessage()
        );
    }

    public String getCsvTemplate() {
        return UTF8_BOM + CSV_TEMPLATE;
    }

    private GrainTempSummary buildSummary(Long warehouseId,
                                          LocalDateTime collectedAt,
                                          List<ImportRow> rows,
                                          SensorMetric temperatureMetric) {
        GrainTempSummary summary = new GrainTempSummary();
        summary.setWarehouseId(warehouseId);
        summary.setCollectedAt(collectedAt);

        List<Double> values = rows.stream().map(ImportRow::temperatureValue).toList();
        summary.setAvgTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
        summary.setMaxTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0)));
        summary.setMinTemp(toDecimal(values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0)));

        Map<Integer, List<ImportRow>> byLayer = rows.stream().collect(Collectors.groupingBy(ImportRow::layerNo));
        summary.setLayer1Avg(avgForLayer(byLayer.get(1)));
        summary.setLayer2Avg(avgForLayer(byLayer.get(2)));
        summary.setLayer3Avg(avgForLayer(byLayer.get(3)));
        summary.setLayer4Avg(avgForLayer(byLayer.get(4)));

        double threshold = temperatureMetric.getMaxThreshold() == null ? 28.0 : temperatureMetric.getMaxThreshold().doubleValue();
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
        summary.setAnalysisRemark("由固定模板粮温导入自动生成");
        return summary;
    }

    private BigDecimal avgForLayer(List<ImportRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return toDecimal(rows.stream().mapToDouble(ImportRow::temperatureValue).average().orElse(0.0));
    }

    private BigDecimal toDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private List<ImportRow> parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lowerFilename.endsWith(".csv")) {
            return parseCsv(file);
        }
        if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
            return parseExcel(file);
        }
        throw new IllegalArgumentException("仅支持 CSV、XLS、XLSX 文件");
    }

    private List<ImportRow> parseCsv(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                rowIndex++;
                if (rowIndex == 1 || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    throw new IllegalArgumentException("CSV 第 " + rowIndex + " 行字段不足，至少需要 warehouseId,collectedAt,zoneCode,layerNo,pointNo,temperatureValue");
                }
                rows.add(new ImportRow(
                        parseLong(parts[0], rowIndex, "warehouseId"),
                        parseDateTime(parts[1], rowIndex, "collectedAt"),
                        parts[2].trim(),
                        parseInteger(parts[3], rowIndex, "layerNo"),
                        parseInteger(parts[4], rowIndex, "pointNo"),
                        parseDouble(parts[5], rowIndex, "temperatureValue"),
                        parts.length > 6 ? parts[6].trim() : null,
                        parts.length > 7 ? parts[7].trim() : null
                ));
            }
        }
        return rows;
    }

    private List<ImportRow> parseExcel(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row, formatter)) {
                    continue;
                }
                rows.add(new ImportRow(
                        parseLong(formatter.formatCellValue(row.getCell(0)), i + 1, "warehouseId"),
                        parseDateTimeCell(row.getCell(1), formatter, i + 1),
                        formatter.formatCellValue(row.getCell(2)).trim(),
                        parseInteger(formatter.formatCellValue(row.getCell(3)), i + 1, "layerNo"),
                        parseInteger(formatter.formatCellValue(row.getCell(4)), i + 1, "pointNo"),
                        parseDouble(formatter.formatCellValue(row.getCell(5)), i + 1, "temperatureValue"),
                        formatter.formatCellValue(row.getCell(6)).trim(),
                        formatter.formatCellValue(row.getCell(7)).trim()
                ));
            }
        } catch (Exception ex) {
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IllegalArgumentException("粮温 Excel 解析失败：" + ex.getMessage(), ex);
        }
        return rows;
    }

    private boolean isRowBlank(Row row, DataFormatter formatter) {
        for (int i = 0; i < 8; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private Long parseLong(String raw, int rowIndex, String fieldName) {
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 " + fieldName + " 不是有效整数");
        }
    }

    private Integer parseInteger(String raw, int rowIndex, String fieldName) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 " + fieldName + " 不是有效整数");
        }
    }

    private Double parseDouble(String raw, int rowIndex, String fieldName) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 " + fieldName + " 不是有效数字");
        }
    }

    private LocalDateTime parseDateTime(String raw, int rowIndex, String fieldName) {
        try {
            return LocalDateTime.parse(raw.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 " + fieldName + " 时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private LocalDateTime parseDateTimeCell(Cell cell, DataFormatter formatter, int rowIndex) {
        if (cell == null) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 collectedAt 不能为空");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
        }
        return parseDateTime(formatter.formatCellValue(cell), rowIndex, "collectedAt");
    }

    private record ImportRow(Long warehouseId,
                             LocalDateTime collectedAt,
                             String zoneCode,
                             Integer layerNo,
                             Integer pointNo,
                             Double temperatureValue,
                             String probeCode,
                             String remark) {
    }
}
