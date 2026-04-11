package com.grain.platform.service;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardLatestSummaryResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import com.grain.platform.dto.dashboard.ScreenDashboardResponse;
import com.grain.platform.dto.dashboard.ScreenPredictionTaskItemResponse;
import com.grain.platform.dto.dashboard.ScreenTrendPointResponse;
import com.grain.platform.dto.dashboard.ScreenWarehouseCompareResponse;
import com.grain.platform.mapper.DashboardMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardOverviewResponse getOverview() {
        return buildOverview(
                dashboardMapper.countWarehouses(),
                dashboardMapper.countGrainTempSummaryCount(),
                dashboardMapper.countLatestRealAlertCount(),
                dashboardMapper.countLatestPredictionAlertCount(),
                dashboardMapper.countArchivedPredictionCount(),
                dashboardMapper.selectLatestRealAlerts(),
                dashboardMapper.selectLatestPredictionAlerts(),
                dashboardMapper.selectLatestGrainSummaries(),
                dashboardMapper.selectWarehouseHealthList()
        );
    }

    public ScreenDashboardResponse getScreenDashboard(Long warehouseId,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime) {
        DashboardOverviewResponse overview = buildOverview(
                dashboardMapper.countScreenWarehouses(warehouseId),
                dashboardMapper.countScreenGrainSummaryCount(warehouseId, startTime, endTime),
                dashboardMapper.countScreenRealAlertCount(warehouseId, startTime, endTime),
                dashboardMapper.countScreenPredictionAlertCount(warehouseId, startTime, endTime),
                dashboardMapper.countScreenArchivedPredictionCount(warehouseId, startTime, endTime),
                dashboardMapper.selectScreenLatestRealAlerts(warehouseId, startTime, endTime),
                dashboardMapper.selectScreenLatestPredictionAlerts(warehouseId, startTime, endTime),
                dashboardMapper.selectScreenLatestGrainSummaries(warehouseId, startTime, endTime),
                dashboardMapper.selectScreenWarehouseHealthList(warehouseId, startTime, endTime)
        );

        List<ScreenTrendPointResponse> grainTrend = dashboardMapper.selectScreenGrainTrend(warehouseId, startTime, endTime);
        List<ScreenTrendPointResponse> alertTrend = dashboardMapper.selectScreenAlertTrend(warehouseId, startTime, endTime);
        List<ScreenTrendPointResponse> predictionTrend = dashboardMapper.selectScreenPredictionTrend(warehouseId, startTime, endTime);
        List<ScreenWarehouseCompareResponse> warehouseComparison =
                dashboardMapper.selectScreenWarehouseComparison(warehouseId, startTime, endTime);
        List<ScreenPredictionTaskItemResponse> latestPredictionTasks =
                dashboardMapper.selectRecentPredictionTasks(warehouseId, startTime, endTime);

        return new ScreenDashboardResponse(
                overview,
                grainTrend,
                alertTrend,
                predictionTrend,
                warehouseComparison,
                latestPredictionTasks
        );
    }

    private DashboardOverviewResponse buildOverview(int warehouseCount,
                                                    int grainSummaryCount,
                                                    int realAlertCount,
                                                    int predictionAlertCount,
                                                    int archivedPredictionCount,
                                                    List<DashboardAlertItemResponse> realAlerts,
                                                    List<DashboardAlertItemResponse> predictionAlerts,
                                                    List<DashboardLatestSummaryResponse> latestGrainSummaries,
                                                    List<DashboardWarehouseHealthResponse> warehouseHealthList) {
        List<DashboardAlertItemResponse> alerts = Stream.concat(
                        realAlerts.stream(),
                        predictionAlerts.stream()
                )
                .sorted(Comparator.comparing(DashboardAlertItemResponse::eventTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();

        return new DashboardOverviewResponse(
                warehouseCount,
                grainSummaryCount,
                realAlertCount,
                predictionAlertCount,
                archivedPredictionCount,
                alerts,
                latestGrainSummaries,
                warehouseHealthList
        );
    }
}
