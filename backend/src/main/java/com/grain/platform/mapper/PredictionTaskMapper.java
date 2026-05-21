package com.grain.platform.mapper;

import com.grain.platform.entity.PredictionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PredictionTaskMapper {
    void insert(PredictionTask predictionTask);

    List<PredictionTask> selectAll();

    long countPage(@Param("keyword") String keyword);

    List<PredictionTask> selectPage(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

    PredictionTask selectById(Long id);

    List<Long> selectIdsByScope(@Param("warehouseId") Long warehouseId,
                                @Param("metricCode") String metricCode,
                                @Param("targetType") String targetType);

    int deleteById(Long id);
}
