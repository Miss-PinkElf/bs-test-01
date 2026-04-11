package com.grain.platform.service;

import com.grain.platform.dto.sensor.SensorDataImportRowDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SensorDataImportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_TEMPLATE = "warehouseId,metricCode,metricValue,collectedAt,remark\n"
            + "1,temperature,24.8,2026-04-07 08:00:00,早间巡检录入\n"
            + "1,humidity,58.2,2026-04-07 08:00:00,早间巡检录入\n"
            + "2,co2,640,2026-04-07 08:00:00,通风后复测\n";
    private static final String UTF8_BOM = "\uFEFF";

    public List<SensorDataImportRowDto> parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String lowerFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);

        // CSV 和 Excel 走不同解析入口，但统一产出导入行结构，后续导入校验只处理一种 DTO。
        if (lowerFilename.endsWith(".csv")) {
            return parseCsv(file);
        }

        if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
            return parseExcel(file);
        }

        throw new IllegalArgumentException("仅支持 CSV、XLS、XLSX 文件");
    }

    public String getCsvTemplate() {
        return UTF8_BOM + CSV_TEMPLATE;
    }

    private List<SensorDataImportRowDto> parseCsv(MultipartFile file) throws IOException {
        List<SensorDataImportRowDto> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowIndex = 0;

            while ((line = reader.readLine()) != null) {
                rowIndex++;
                if (rowIndex == 1 || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 4) {
                    throw new IllegalArgumentException("CSV 第 " + rowIndex + " 行字段不足，至少需要 warehouseId, metricCode, metricValue, collectedAt");
                }

                // 行级字段不足在解析阶段就失败，前端提示才能精确对应到模板里的出错行号。
                rows.add(new SensorDataImportRowDto(
                        parseLong(parts[0], rowIndex, "warehouseId"),
                        parts[1].trim(),
                        parseDouble(parts[2], rowIndex, "metricValue"),
                        parseDateTime(parts[3], rowIndex, "collectedAt"),
                        parts.length > 4 ? parts[4].trim() : null
                ));
            }
        }

        return rows;
    }

    private List<SensorDataImportRowDto> parseExcel(MultipartFile file) throws IOException {
        List<SensorDataImportRowDto> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row)) {
                    continue;
                }

                rows.add(new SensorDataImportRowDto(
                        parseLong(formatter.formatCellValue(row.getCell(0)), i + 1, "warehouseId"),
                        formatter.formatCellValue(row.getCell(1)).trim(),
                        parseDouble(formatter.formatCellValue(row.getCell(2)), i + 1, "metricValue"),
                        parseDateTimeCell(row.getCell(3), formatter, i + 1),
                        formatter.formatCellValue(row.getCell(4)).trim()
                ));
            }
        }

        return rows;
    }

    private boolean isRowBlank(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !new DataFormatter().formatCellValue(cell).isBlank()) {
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

        // Excel 日期单元格优先按真实日期解析，文本格式再走统一的 yyyy-MM-dd HH:mm:ss 校验。
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
        }

        return parseDateTime(formatter.formatCellValue(cell), rowIndex, "collectedAt");
    }
}
