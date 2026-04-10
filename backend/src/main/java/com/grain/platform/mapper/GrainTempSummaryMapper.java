package com.grain.platform.mapper;

import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.entity.GrainTempSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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

    List<GrainTempSummaryItemDto> selectPageByCondition(@Param("warehouseId") Long warehouseId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime,
                                                        @Param("warningLevel") String warningLevel,
                                                        @Param("tempMin") BigDecimal tempMin,
                                                        @Param("tempMax") BigDecimal tempMax,
                                                        @Param("keyword") String keyword,
                                                        @Param("offset") int offset,
                                                        @Param("pageSize") int pageSize);

    long countByCondition(@Param("warehouseId") Long warehouseId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("warningLevel") String warningLevel,
                          @Param("tempMin") BigDecimal tempMin,
                          @Param("tempMax") BigDecimal tempMax,
                          @Param("keyword") String keyword);

    List<GrainTempSummary> selectSeriesByTarget(@Param("warehouseId") Long warehouseId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("targetType") String targetType);
}
