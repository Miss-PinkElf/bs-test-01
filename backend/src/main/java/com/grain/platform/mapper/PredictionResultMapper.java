package com.grain.platform.mapper;

import com.grain.platform.entity.PredictionResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PredictionResultMapper {
    void insertBatch(@Param("list") List<PredictionResult> list);

    List<PredictionResult> selectByTaskId(@Param("taskId") Long taskId);

    int deleteByTaskId(@Param("taskId") Long taskId);
}
