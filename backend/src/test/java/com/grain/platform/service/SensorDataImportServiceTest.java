package com.grain.platform.service;

import com.grain.platform.dto.sensor.SensorDataImportRowDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorDataImportServiceTest {

    private final SensorDataImportService importService = new SensorDataImportService();

    @Test
    void parseCsvTemplateKeepsDefaultDateTimeFormat() throws Exception {
        MockMultipartFile file = csvFile(importService.getCsvTemplate());

        List<SensorDataImportRowDto> rows = importService.parse(file);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).metricCode()).isEqualTo("humidity");
        assertThat(rows.get(0).collectedAt()).isEqualTo(LocalDateTime.of(2026, 4, 7, 8, 0, 0));
        assertThat(rows.get(1).metricCode()).isEqualTo("co2");
    }

    @Test
    void parseCsvAcceptsExcelCommonDateTimeText() throws Exception {
        MockMultipartFile file = csvFile("""
                warehouseId,metricCode,metricValue,collectedAt,remark
                1,humidity,58.2,2026/4/7 8:00,Excel打开CSV后常见格式
                2,co2,640,2026-4-7 8:00:00,非补零格式
                """);

        List<SensorDataImportRowDto> rows = importService.parse(file);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).collectedAt()).isEqualTo(LocalDateTime.of(2026, 4, 7, 8, 0, 0));
        assertThat(rows.get(1).collectedAt()).isEqualTo(LocalDateTime.of(2026, 4, 7, 8, 0, 0));
    }

    @Test
    void parseCsvRejectsInvalidDateTimeText() {
        MockMultipartFile file = csvFile("""
                warehouseId,metricCode,metricValue,collectedAt,remark
                1,humidity,58.2,not-a-date,错误时间
                """);

        assertThatThrownBy(() -> importService.parse(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("第 2 行字段 collectedAt 时间格式错误");
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file",
                "sensor-data-template.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
