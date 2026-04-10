package com.grain.platform.dto.grain;

import java.util.List;

/**
 * 粮温原始记录表头筛选项：由当前库中已存在数据聚合，供前端下拉使用。
 */
public record GrainTempRecordFilterOptionsDto(
        List<String> zoneCodes,
        List<Integer> layerNos,
        List<Integer> pointNos
) {
}
