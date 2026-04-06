package com.grain.platform.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {
    int countWarehouses();

    int countTodaySensorData();

    int countArchivedPredictionCount();

    List<String> selectLatestAlerts();
}
