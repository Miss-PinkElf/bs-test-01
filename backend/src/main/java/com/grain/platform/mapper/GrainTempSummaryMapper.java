package com.grain.platform.mapper;

import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.entity.GrainTempSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GrainTempSummaryMapper {
    void upsert(GrainTempSummary summary);

    void deleteByWarehouseAndCollectedAt(@Param("warehouseId") Long warehouseId,
                                         @Param("collectedAt") LocalDateTime collectedAt);

    List<GrainTempSummaryItemDto> selectByCondition(@Param("warehouseId") Long warehouseId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    List<GrainTempSummary> selectSeriesByTarget(@Param("warehouseId") Long warehouseId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("targetType") String targetType);
}
