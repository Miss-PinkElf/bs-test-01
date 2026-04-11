package com.grain.platform.service;

import com.grain.platform.common.PageResult;
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

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardOverviewResponse getOverview() {
        return new DashboardOverviewResponse(
                dashboardMapper.countWarehouses(),
                dashboardMapper.countGrainTempSummaryCount(),
                dashboardMapper.countLatestRealAlertCount(),
                dashboardMapper.countLatestPredictionAlertCount(),
                dashboardMapper.countArchivedPredictionCount(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public PageResult<DashboardAlertItemResponse> getAlertPage(String keyword, Integer pageNum, Integer pageSize) {
        String kw = normalizeKeyword(keyword);
        int finalPageNum = normalizePageNum(pageNum);
        int finalPageSize = normalizePageSize(pageSize);
        long total = dashboardMapper.countAlertPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<DashboardAlertItemResponse> list = dashboardMapper.selectAlertPage(kw, offset, finalPageSize);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public PageResult<DashboardWarehouseHealthResponse> getWarehouseHealthPage(String keyword, Integer pageNum, Integer pageSize) {
        String kw = normalizeKeyword(keyword);
        int finalPageNum = normalizePageNum(pageNum);
        int finalPageSize = normalizePageSize(pageSize);
        long total = dashboardMapper.countWarehouseHealthPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<DashboardWarehouseHealthResponse> list = dashboardMapper.selectWarehouseHealthPage(kw, offset, finalPageSize);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public PageResult<DashboardLatestSummaryResponse> getGrainSummaryPage(String keyword, Integer pageNum, Integer pageSize) {
        String kw = normalizeKeyword(keyword);
        int finalPageNum = normalizePageNum(pageNum);
        int finalPageSize = normalizePageSize(pageSize);
        long total = dashboardMapper.countGrainSummaryPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<DashboardLatestSummaryResponse> list = dashboardMapper.selectGrainSummaryPage(kw, offset, finalPageSize);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
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

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
