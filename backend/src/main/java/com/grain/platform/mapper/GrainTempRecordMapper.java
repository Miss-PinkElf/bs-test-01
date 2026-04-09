package com.grain.platform.mapper;

import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.entity.GrainTempRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GrainTempRecordMapper {
    void insert(GrainTempRecord record);

    void update(GrainTempRecord record);

    void deleteById(@Param("id") Long id);

    GrainTempRecord selectEntityById(@Param("id") Long id);

    void upsertBatch(@Param("list") List<GrainTempRecord> list);

    List<GrainTempRecordItemDto> selectByCondition(@Param("warehouseId") Long warehouseId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("zoneCode") String zoneCode,
                                                   @Param("layerNo") Integer layerNo);

    List<GrainTempRecordItemDto> selectPageByCondition(@Param("warehouseId") Long warehouseId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("zoneCode") String zoneCode,
                                                       @Param("layerNo") Integer layerNo,
                                                       @Param("offset") int offset,
                                                       @Param("pageSize") int pageSize);

    long countByCondition(@Param("warehouseId") Long warehouseId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("zoneCode") String zoneCode,
                          @Param("layerNo") Integer layerNo);

    List<GrainTempRecordItemDto> selectByWarehouseAndCollectedAt(@Param("warehouseId") Long warehouseId,
                                                                 @Param("collectedAt") LocalDateTime collectedAt);
}
