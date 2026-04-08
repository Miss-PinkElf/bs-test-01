package com.grain.platform.mapper;

import com.grain.platform.entity.GrainTempPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GrainTempPointMapper {
    GrainTempPoint selectByUniqueKey(@Param("warehouseId") Long warehouseId,
                                     @Param("zoneCode") String zoneCode,
                                     @Param("layerNo") Integer layerNo,
                                     @Param("pointNo") Integer pointNo);

    void insert(GrainTempPoint point);
}
