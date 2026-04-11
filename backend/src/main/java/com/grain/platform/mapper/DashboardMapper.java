package com.grain.platform.mapper;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardLatestSummaryResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import com.grain.platform.dto.dashboard.ScreenPredictionTaskItemResponse;
import com.grain.platform.dto.dashboard.ScreenTrendPointResponse;
import com.grain.platform.dto.dashboard.ScreenWarehouseCompareResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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

    int countScreenWarehouses(@Param("warehouseId") Long warehouseId);

    int countScreenGrainSummaryCount(@Param("warehouseId") Long warehouseId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    int countScreenRealAlertCount(@Param("warehouseId") Long warehouseId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    int countScreenPredictionAlertCount(@Param("warehouseId") Long warehouseId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    int countScreenArchivedPredictionCount(@Param("warehouseId") Long warehouseId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    List<DashboardAlertItemResponse> selectScreenLatestRealAlerts(@Param("warehouseId") Long warehouseId,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime);

    List<DashboardAlertItemResponse> selectScreenLatestPredictionAlerts(@Param("warehouseId") Long warehouseId,
                                                                        @Param("startTime") LocalDateTime startTime,
                                                                        @Param("endTime") LocalDateTime endTime);

    List<DashboardLatestSummaryResponse> selectScreenLatestGrainSummaries(@Param("warehouseId") Long warehouseId,
                                                                          @Param("startTime") LocalDateTime startTime,
                                                                          @Param("endTime") LocalDateTime endTime);

    List<DashboardWarehouseHealthResponse> selectScreenWarehouseHealthList(@Param("warehouseId") Long warehouseId,
                                                                           @Param("startTime") LocalDateTime startTime,
                                                                           @Param("endTime") LocalDateTime endTime);

    List<ScreenTrendPointResponse> selectScreenGrainTrend(@Param("warehouseId") Long warehouseId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    List<ScreenTrendPointResponse> selectScreenAlertTrend(@Param("warehouseId") Long warehouseId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    List<ScreenTrendPointResponse> selectScreenPredictionTrend(@Param("warehouseId") Long warehouseId,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    List<ScreenWarehouseCompareResponse> selectScreenWarehouseComparison(@Param("warehouseId") Long warehouseId,
                                                                         @Param("startTime") LocalDateTime startTime,
                                                                         @Param("endTime") LocalDateTime endTime);

    List<ScreenPredictionTaskItemResponse> selectRecentPredictionTasks(@Param("warehouseId") Long warehouseId,
                                                                       @Param("startTime") LocalDateTime startTime,
                                                                       @Param("endTime") LocalDateTime endTime);
}
