package com.grain.platform.mapper;

import com.grain.platform.entity.PredictionTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PredictionTaskMapper {
    void insert(PredictionTask predictionTask);

    List<PredictionTask> selectAll();

    PredictionTask selectById(Long id);
}
