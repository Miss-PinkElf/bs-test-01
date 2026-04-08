package com.grain.platform.mapper;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardLatestSummaryResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {
    int countWarehouses();

    int countGrainTempSummaryCount();

    int countLatestRealAlertCount();

    int countLatestPredictionAlertCount();

    int countArchivedPredictionCount();

    List<DashboardAlertItemResponse> selectLatestRealAlerts();

    List<DashboardAlertItemResponse> selectLatestPredictionAlerts();

    List<DashboardLatestSummaryResponse> selectLatestGrainSummaries();

    List<DashboardWarehouseHealthResponse> selectWarehouseHealthList();
}
