package com.grain.platform.mapper;

import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.entity.GrainTempRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GrainTempRecordMapper {
    void upsertBatch(@Param("list") List<GrainTempRecord> list);

    List<GrainTempRecordItemDto> selectByCondition(@Param("warehouseId") Long warehouseId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("zoneCode") String zoneCode,
                                                   @Param("layerNo") Integer layerNo);
}
