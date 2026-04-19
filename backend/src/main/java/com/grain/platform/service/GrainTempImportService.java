package com.grain.platform.service;

import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.common.ForbiddenException;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrainTempImportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_TEMPLATE = "warehouseCode,collectedAt,zoneCode,layerNo,pointNo,temperatureValue,probeCode,remark\n"
            + "WH-A01,2026-04-08 08:40:00,A,1,1,24.3,CABLE-A,第一层测点\n"
            + "WH-A01,2026-04-08 08:40:00,A,1,2,24.5,CABLE-A,第一层测点\n"
            + "WH-A01,2026-04-08 08:40:00,A,2,1,24.8,CABLE-A,第二层测点\n";
    private static final String UTF8_BOM = "\uFEFF";
    private static final int FIXED_TEMPLATE_POINT_COUNT = 4;
    private static final int FIXED_TEMPLATE_LAYER_COUNT = 4;
    private static final int RECORD_UPSERT_MAX_RETRIES = 3;
    private static final ConcurrentMap<String, ReentrantLock> IMPORT_LOCKS = new ConcurrentHashMap<>();

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

    public GrainTempImportResultDto importData(MultipartFile file, Long operatorUserId, Long allowedWarehouseId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String lowerFilename = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);
        List<ImportRow> rows;
        if (lowerFilename.contains("grain-temp-fixed-template")) {
            rows = retryFixedTemplateIfApplicable(file, new IllegalArgumentException("固定模板直接解析"));
        } else {
            try {
                rows = parse(file);
            } catch (IllegalArgumentException ex) {
                rows = retryFixedTemplateIfApplicable(file, ex);
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("粮温导入文件为空或没有有效数据");
        }

        Long warehouseId = rows.get(0).warehouseId();
        if (allowedWarehouseId != null && !allowedWarehouseId.equals(warehouseId)) {
            throw new ForbiddenException("仓库管理员仅可导入所属仓库粮温数据");
        }
        LocalDateTime collectedAt = rows.get(0).collectedAt();
        boolean sameHeader = rows.stream()
                .allMatch(item -> item.warehouseId().equals(warehouseId) && item.collectedAt().equals(collectedAt));
        if (!sameHeader) {
            throw new IllegalArgumentException("当前粮温 MVP 导入要求同一文件中的 warehouseCode/warehouseId 与 collectedAt 保持一致");
        }

        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库不存在：" + warehouseId);
        }

        String batchNo = "BATCH-GRAIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String importLockKey = String.valueOf(warehouseId);
        ReentrantLock lock = IMPORT_LOCKS.computeIfAbsent(importLockKey, key -> new ReentrantLock());
        lock.lock();
        try {
            List<ImportRow> effectiveRows = deduplicateRows(rows);
            List<ImportRow> orderedRows = effectiveRows.stream()
                    .sorted(Comparator.comparing(
                            ImportRow::zoneCode,
                            Comparator.nullsFirst(String::compareToIgnoreCase))
                            .thenComparing(ImportRow::layerNo, Comparator.nullsFirst(Integer::compareTo))
                            .thenComparing(ImportRow::pointNo, Comparator.nullsFirst(Integer::compareTo)))
                    .toList();
            List<GrainTempRecord> records = new ArrayList<>();

            for (ImportRow row : orderedRows) {
                GrainTempPoint point = grainTempPointMapper.selectByUniqueKey(row.warehouseId(), row.zoneCode(),
                        row.layerNo(), row.pointNo());
                if (point == null) {
                    point = new GrainTempPoint();
                    point.setWarehouseId(row.warehouseId());
                    point.setProbeCode(row.probeCode() == null || row.probeCode().isBlank() ? "AUTO-" + row.zoneCode()
                            : row.probeCode());
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
                record.setCreatedBy(operatorUserId);
                records.add(record);
            }

            upsertRecords(records);
            GrainTempSummary summary = buildSummary(warehouseId, collectedAt, orderedRows, metricService.getMetric("temperature"));
            grainTempSummaryMapper.upsert(summary);

            return new GrainTempImportResultDto(
                    batchNo,
                    warehouseId,
                    warehouse.getWarehouseName(),
                    collectedAt.format(DATE_TIME_FORMATTER),
                    orderedRows.size(),
                    true,
                    summary.getWarningLevel(),
                    summary.getWarningMessage());
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads() && !lock.isLocked()) {
                IMPORT_LOCKS.remove(importLockKey, lock);
            }
        }
    }

    public String getCsvTemplate() {
        return UTF8_BOM + CSV_TEMPLATE;
    }

    private List<ImportRow> retryFixedTemplateIfApplicable(MultipartFile file, IllegalArgumentException original) throws IOException {
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (!(lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls"))) {
            throw original;
        }
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return parseGeneratedFixedTemplate(workbook.getSheetAt(0), new DataFormatter());
        } catch (Exception fallbackEx) {
            throw new IllegalArgumentException("固定模板回退失败：" + fallbackEx.getMessage(), fallbackEx);
        }
    }

    private List<ImportRow> parseGeneratedFixedTemplate(Sheet sheet, DataFormatter formatter) {
        Long warehouseId = resolveWarehouseReference(getCellText(sheet.getRow(4), 1, formatter), 5);
        LocalDateTime collectedAt = parseDateTimeCell(sheet.getRow(5).getCell(1), formatter, 6);
        List<ImportRow> rows = new ArrayList<>();
        appendGeneratedFixedZone(sheet, formatter, 10, warehouseId, collectedAt, rows);
        appendGeneratedFixedZone(sheet, formatter, 17, warehouseId, collectedAt, rows);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("固定模板中未解析到任何测点温度数据");
        }
        return rows;
    }

    private void appendGeneratedFixedZone(Sheet sheet,
            DataFormatter formatter,
            int zoneRowIndex,
            Long warehouseId,
            LocalDateTime collectedAt,
            List<ImportRow> rows) {
        Row zoneRow = sheet.getRow(zoneRowIndex);
        if (zoneRow == null) {
            return;
        }
        String zoneCode = requireText(getCellText(zoneRow, 1, formatter), zoneRowIndex + 1, "zoneCode");
        String probeCode = getCellText(zoneRow, 3, formatter);
        Row headerRow = sheet.getRow(zoneRowIndex + 1);
        if (headerRow == null) {
            throw new IllegalArgumentException("固定模板缺少测点矩阵表头");
        }
        List<Integer> pointNumbers = new ArrayList<>();
        for (int cellIndex = 1; cellIndex <= FIXED_TEMPLATE_POINT_COUNT; cellIndex++) {
            pointNumbers.add(parseInteger(getCellText(headerRow, cellIndex, formatter), zoneRowIndex + 2, "pointNo"));
        }
        for (int rowOffset = 0; rowOffset < FIXED_TEMPLATE_LAYER_COUNT; rowOffset++) {
            int dataRowIndex = zoneRowIndex + 2 + rowOffset;
            Row dataRow = sheet.getRow(dataRowIndex);
            Integer layerNo = parseInteger(getCellText(dataRow, 0, formatter), dataRowIndex + 1, "layerNo");
            for (int pointIndex = 0; pointIndex < pointNumbers.size(); pointIndex++) {
                String valueText = getCellText(dataRow, pointIndex + 1, formatter);
                if (valueText.isBlank()) {
                    continue;
                }
                rows.add(new ImportRow(
                        warehouseId,
                        collectedAt,
                        zoneCode,
                        layerNo,
                        pointNumbers.get(pointIndex),
                        parseDouble(valueText, dataRowIndex + 1, "temperatureValue"),
                        probeCode,
                        "固定模板导入"));
            }
        }
    }

    public byte[] getExcelTemplate() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("粮温固定模板");

            CellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle sectionStyle = workbook.createCellStyle();
            XSSFFont sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 11);
            sectionStyle.setFont(sectionFont);
            sectionStyle.setAlignment(HorizontalAlignment.LEFT);
            sectionStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            int r = 0;
            addMergedRow(sheet, r++, 0, 5, "粮温固定导入模板（测点矩阵）", titleStyle);
            addMergedRow(sheet,
                    r++,
                    0,
                    5,
                    "填写说明：① 先完成「一、基础信息」中的仓库编码与采集时间，整表只能对应同一仓库、同一时间；② 在「二、测点温度矩阵」中按区域分块填写，"
                            + "每一块的「行」为层号、「列」为点位编号，单元格填摄氏温度数值；③ 可增加更多区域块：复制一块的结构并修改区域编码与缆号即可；④ 底部汇总区可留空，导入后由系统自动计算。",
                    wrapStyle);
            r++;
            addMergedRow(sheet, r++, 0, 5, "一、基础信息", sectionStyle);
            createRow(sheet, r++, "仓库编码（warehouseCode）", "WH-A01");
            createRow(sheet, r++, "采集时间（collectedAt）", "2026-04-08 08:40:00");
            addMergedRow(sheet,
                    r++,
                    0,
                    5,
                    "提示：仓库编码须与系统中仓库主数据一致；也兼容旧版仓库ID填写。采集时间格式为 yyyy-MM-dd HH:mm:ss（也可在 Excel 中按日期时间格式填写）。",
                    wrapStyle);
            r++;
            addMergedRow(sheet, r++, 0, 5, "二、测点温度矩阵（行=层号，列=点位编号）", sectionStyle);
            r++;
            r = createZoneBlock(sheet, r, "A", "CABLE-A");
            r++;
            r = createZoneBlock(sheet, r, "B", "CABLE-B");
            addMergedRow(sheet,
                    r,
                    0,
                    5,
                    "三、汇总分析（可留空）：平均温、最高温、预警等级等由系统导入后自动写入数据库，无需手填。",
                    wrapStyle);

            for (int column = 0; column <= 5; column++) {
                sheet.autoSizeColumn(column);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void addMergedRow(Sheet sheet, int rowIndex, int colFrom, int colTo, String text, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(colFrom);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        if (colTo > colFrom) {
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, colFrom, colTo));
        }
    }

    private void createRow(Sheet sheet, int rowIndex, String first, String second) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(first);
        row.createCell(1).setCellValue(second);
    }

    /**
     * @return 紧接矩阵块之后的首个空行索引（便于继续追加区域块或页脚说明）
     */
    private int createZoneBlock(Sheet sheet, int startRowIndex, String zoneCode, String probeCode) {
        Row metaRow = sheet.createRow(startRowIndex);
        metaRow.createCell(0).setCellValue("区域编码（zoneCode）");
        metaRow.createCell(1).setCellValue(zoneCode);
        metaRow.createCell(2).setCellValue("缆号/探头编码（probeCode）");
        metaRow.createCell(3).setCellValue(probeCode);

        Row headerRow = sheet.createRow(startRowIndex + 1);
        headerRow.createCell(0).setCellValue("层号 \\ 点位列");
        for (int pointNo = 1; pointNo <= FIXED_TEMPLATE_POINT_COUNT; pointNo++) {
            headerRow.createCell(pointNo).setCellValue(pointNo);
        }

        for (int layerNo = 1; layerNo <= FIXED_TEMPLATE_LAYER_COUNT; layerNo++) {
            Row dataRow = sheet.createRow(startRowIndex + 1 + layerNo);
            dataRow.createCell(0).setCellValue(layerNo);
            for (int pointNo = 1; pointNo <= FIXED_TEMPLATE_POINT_COUNT; pointNo++) {
                dataRow.createCell(pointNo).setCellValue(24.0 + layerNo * 0.4 + pointNo * 0.2);
            }
        }
        return startRowIndex + 1 + FIXED_TEMPLATE_LAYER_COUNT + 1;
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

        double threshold = temperatureMetric.getMaxThreshold() == null ? 28.0
                : temperatureMetric.getMaxThreshold().doubleValue();
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

    private List<ImportRow> deduplicateRows(List<ImportRow> rows) {
        Map<String, ImportRow> latestRows = new LinkedHashMap<>();
        for (ImportRow row : rows) {
            latestRows.put(buildRowKey(row), row);
        }
        return new ArrayList<>(latestRows.values());
    }

    private String buildRowKey(ImportRow row) {
        return row.warehouseId() + "#" + row.collectedAt() + "#" + row.zoneCode() + "#" + row.layerNo() + "#" + row.pointNo();
    }

    private void upsertRecords(List<GrainTempRecord> records) {
        for (GrainTempRecord record : records) {
            upsertRecordWithRetry(record);
        }
    }

    private void upsertRecordWithRetry(GrainTempRecord record) {
        int attempt = 0;
        while (true) {
            try {
                grainTempRecordMapper.upsert(record);
                return;
            } catch (RuntimeException ex) {
                attempt++;
                if (!isDeadlock(ex) || attempt >= RECORD_UPSERT_MAX_RETRIES) {
                    throw ex;
                }
                sleepBeforeRetry(attempt);
            }
        }
    }

    private boolean isDeadlock(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DeadlockLoserDataAccessException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("Deadlock found when trying to get lock")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            TimeUnit.MILLISECONDS.sleep(80L * attempt);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("粮温导入重试被中断", interruptedException);
        }
    }

    private List<ImportRow> parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lowerFilename.endsWith(".csv")) {
            return parseCsv(file);
        }
        if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
            return parseExcel(file, lowerFilename);
        }
        throw new IllegalArgumentException("仅支持 CSV、XLS、XLSX 文件");
    }

    private List<ImportRow> parseCsv(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                rowIndex++;
                if (rowIndex == 1 || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    throw new IllegalArgumentException("CSV 第 " + rowIndex
                            + " 行字段不足，至少需要 warehouseCode/warehouseId,collectedAt,zoneCode,layerNo,pointNo,temperatureValue");
                }
                rows.add(new ImportRow(
                        resolveWarehouseReference(parts[0], rowIndex),
                        parseDateTime(parts[1], rowIndex, "collectedAt"),
                        parts[2].trim(),
                        parseInteger(parts[3], rowIndex, "layerNo"),
                        parseInteger(parts[4], rowIndex, "pointNo"),
                        parseDouble(parts[5], rowIndex, "temperatureValue"),
                        parts.length > 6 ? parts[6].trim() : null,
                        parts.length > 7 ? parts[7].trim() : null));
            }
        }
        return rows;
    }

    private List<ImportRow> parseExcel(MultipartFile file, String lowerFilename) throws IOException {
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            String titleCell = getCellText(sheet.getRow(0), 0, formatter);
            boolean fixedTemplateHint = lowerFilename.contains("grain-temp-fixed-template")
                    || titleCell.contains("粮温固定导入模板")
                    || titleCell.contains("测点矩阵");
            if (fixedTemplateHint || isFixedTemplateSheet(sheet, formatter)) {
                return parseFixedTemplate(sheet, formatter);
            }
            return parseRowStyleSheet(sheet, formatter);
        } catch (Exception ex) {
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IllegalArgumentException("粮温 Excel 解析失败：" + ex.getMessage(), ex);
        }
    }

    private boolean isFixedTemplateSheet(Sheet sheet, DataFormatter formatter) {
        boolean hasWarehouseId = false;
        boolean hasZoneCode = false;
        int maxRow = Math.min(sheet.getLastRowNum(), 40);
        for (int index = 0; index <= maxRow; index++) {
            String firstCell = getCellText(sheet.getRow(index), 0, formatter);
            if (cellMatchesKey(firstCell, "warehouseId") || cellMatchesKey(firstCell, "warehouseCode")) {
                hasWarehouseId = true;
            }
            if (cellMatchesKey(firstCell, "zoneCode")) {
                hasZoneCode = true;
            }
        }
        return hasWarehouseId && hasZoneCode;
    }

    /**
     * 兼容旧版纯英文标签与新版「中文（英文键）」标签；导入时以英文键或固定中文关键词识别。
     */
    private boolean cellMatchesKey(String cellText, String englishKey) {
        if (cellText == null || cellText.isBlank()) {
            return false;
        }
        String t = cellText.trim();
        String k = englishKey.toLowerCase(Locale.ROOT);
        if (t.equalsIgnoreCase(englishKey) || t.toLowerCase(Locale.ROOT).contains(k)) {
            return true;
        }
        return switch (englishKey) {
            case "warehouseId" -> t.startsWith("仓库编号") || t.startsWith("仓库ID") || t.startsWith("仓库主键");
            case "warehouseCode" -> t.startsWith("仓库编码") || t.startsWith("仓库编号");
            case "collectedAt" -> t.startsWith("采集时间");
            case "zoneCode" -> t.startsWith("区域编码");
            case "probeCode" -> t.startsWith("缆号") || t.startsWith("探头编码") || t.startsWith("探头");
            default -> false;
        };
    }

    private List<ImportRow> parseFixedTemplate(Sheet sheet, DataFormatter formatter) {
        Long warehouseId = null;
        LocalDateTime collectedAt = null;
        for (int index = 0; index <= Math.min(sheet.getLastRowNum(), 40); index++) {
            Row row = sheet.getRow(index);
            String firstCell = getCellText(row, 0, formatter);
            if (cellMatchesKey(firstCell, "warehouseId") || cellMatchesKey(firstCell, "warehouseCode")) {
                warehouseId = resolveWarehouseReference(getCellText(row, 1, formatter), index + 1);
            }
            if (cellMatchesKey(firstCell, "collectedAt")) {
                collectedAt = parseDateTimeCell(row.getCell(1), formatter, index + 1);
            }
        }

        if (warehouseId == null || collectedAt == null) {
            throw new IllegalArgumentException("固定模板缺少 warehouseCode/warehouseId 或 collectedAt 基础信息");
        }

        List<ImportRow> rows = new ArrayList<>();
        int rowIndex = 0;
        while (rowIndex <= sheet.getLastRowNum()) {
            Row row = sheet.getRow(rowIndex);
            String firstCell = getCellText(row, 0, formatter);
            if (cellMatchesKey(firstCell, "zoneCode")) {
                rowIndex = parseZoneMatrixBlock(sheet, formatter, rowIndex, warehouseId, collectedAt, rows);
                continue;
            }
            if (firstCell.contains("汇总分析") || firstCell.contains("汇总区")) {
                break;
            }
            rowIndex++;
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("固定模板中未解析到任何测点温度数据");
        }
        return rows;
    }

    private int parseZoneMatrixBlock(Sheet sheet,
            DataFormatter formatter,
            int zoneRowIndex,
            Long warehouseId,
            LocalDateTime collectedAt,
            List<ImportRow> resultRows) {
        Row zoneRow = sheet.getRow(zoneRowIndex);
        String zoneCode = requireText(extractNamedValue(zoneRow, formatter, "zoneCode"), zoneRowIndex + 1, "zoneCode");
        String probeRaw = extractNamedValue(zoneRow, formatter, "probeCode");
        String probeCode = probeRaw == null || probeRaw.isBlank() ? null : probeRaw.trim();

        int headerRowIndex = zoneRowIndex + 1;
        while (headerRowIndex <= sheet.getLastRowNum() && isRowBlank(sheet.getRow(headerRowIndex), formatter, 6)) {
            headerRowIndex++;
        }

        Row headerRow = sheet.getRow(headerRowIndex);
        String headerLabel = getCellText(headerRow, 0, formatter);
        if (!(headerLabel.contains("层号") || headerLabel.contains("点位") || "layerNo".equalsIgnoreCase(headerLabel))) {
            throw new IllegalArgumentException("第 " + (headerRowIndex + 1) + " 行不是有效的测点矩阵表头，应包含层号/点位说明（如「层号 \\ 点位列」）");
        }

        List<Integer> pointNumbers = new ArrayList<>();
        int columnIndex = 1;
        while (true) {
            String pointNoText = getCellText(headerRow, columnIndex, formatter);
            if (pointNoText.isBlank()) {
                break;
            }
            pointNumbers.add(parseInteger(pointNoText, headerRowIndex + 1, "pointNo"));
            columnIndex++;
        }
        if (pointNumbers.isEmpty()) {
            throw new IllegalArgumentException("第 " + (headerRowIndex + 1) + " 行没有有效的点位表头");
        }

        int dataRowIndex = headerRowIndex + 1;
        while (dataRowIndex <= sheet.getLastRowNum()) {
            Row dataRow = sheet.getRow(dataRowIndex);
            String firstCell = getCellText(dataRow, 0, formatter);
            if (firstCell.isBlank()) {
                return dataRowIndex + 1;
            }
            if (cellMatchesKey(firstCell, "zoneCode") || firstCell.contains("汇总分析") || firstCell.contains("汇总区")) {
                return dataRowIndex;
            }

            Integer layerNo = parseInteger(firstCell, dataRowIndex + 1, "layerNo");
            for (int pointIndex = 0; pointIndex < pointNumbers.size(); pointIndex++) {
                String valueText = getCellText(dataRow, pointIndex + 1, formatter);
                if (valueText.isBlank()) {
                    continue;
                }
                resultRows.add(new ImportRow(
                        warehouseId,
                        collectedAt,
                        zoneCode,
                        layerNo,
                        pointNumbers.get(pointIndex),
                        parseDouble(valueText, dataRowIndex + 1, "temperatureValue"),
                        probeCode,
                        "固定模板导入"));
            }
            dataRowIndex++;
        }
        return dataRowIndex;
    }

    private List<ImportRow> parseRowStyleSheet(Sheet sheet, DataFormatter formatter) {
        List<ImportRow> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowBlank(row, formatter, 8)) {
                continue;
            }
            rows.add(new ImportRow(
                    resolveWarehouseReference(getCellText(row, 0, formatter), i + 1),
                    parseDateTimeCell(row.getCell(1), formatter, i + 1),
                    getCellText(row, 2, formatter),
                    parseInteger(getCellText(row, 3, formatter), i + 1, "layerNo"),
                    parseInteger(getCellText(row, 4, formatter), i + 1, "pointNo"),
                    parseDouble(getCellText(row, 5, formatter), i + 1, "temperatureValue"),
                    getCellText(row, 6, formatter),
                    getCellText(row, 7, formatter)));
        }
        return rows;
    }

    private String extractNamedValue(Row row, DataFormatter formatter, String fieldName) {
        if (row == null) {
            return "";
        }
        int lastCell = Math.max(row.getLastCellNum(), 0);
        for (int cellIndex = 0; cellIndex < lastCell - 1; cellIndex++) {
            if (cellMatchesKey(getCellText(row, cellIndex, formatter), fieldName)) {
                return getCellText(row, cellIndex + 1, formatter);
            }
        }
        return "";
    }

    private String requireText(String raw, int rowIndex, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 " + fieldName + " 不能为空");
        }
        return raw.trim();
    }

    private String getCellText(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null || row.getCell(cellIndex) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    private boolean isRowBlank(Row row, DataFormatter formatter, int scanLength) {
        if (row == null) {
            return true;
        }
        for (int index = 0; index < scanLength; index++) {
            Cell cell = row.getCell(index);
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

    private Long resolveWarehouseReference(String raw, int rowIndex) {
        String value = requireText(raw, rowIndex, "warehouseCode");
        Warehouse warehouse = warehouseMapper.selectByWarehouseCode(value.trim());
        if (warehouse != null) {
            return warehouse.getId();
        }
        try {
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {
            throw new IllegalArgumentException("第 " + rowIndex + " 行字段 warehouseCode/warehouseId 不是有效仓库编号");
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
            throw new IllegalArgumentException(
                    "第 " + rowIndex + " 行字段 " + fieldName + " 时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
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









