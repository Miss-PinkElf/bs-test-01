package com.grain.platform.dto.grain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GrainTempRecordUpsertRequest(
        @NotNull(message = "仓库不能为空")
        Long warehouseId,
        @NotBlank(message = "区域不能为空")
        String zoneCode,
        @NotNull(message = "层号不能为空")
        Integer layerNo,
        @NotNull(message = "点位不能为空")
        Integer pointNo,
        @NotNull(message = "采集时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime collectedAt,
        @NotNull(message = "温度值不能为空")
        Double temperatureValue,
        String probeCode,
        String remark
) {
}
