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
import java.util.Locale;
import java.util.stream.Stream;

@Service
// 首页和大屏都走这里做聚合，页面层只关心“展示什么”，不直接拼装底层数据。
public class DashboardService {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardOverviewResponse getOverview(Long warehouseId) {
        if (warehouseId != null) {
            return buildOverview(
                    dashboardMapper.countScreenWarehouses(warehouseId),
                    dashboardMapper.countScreenGrainSummaryCount(warehouseId, null, null),
                    dashboardMapper.countScreenRealAlertCount(warehouseId, null, null),
                    dashboardMapper.countScreenPredictionAlertCount(warehouseId, null, null),
                    dashboardMapper.countScreenArchivedPredictionCount(warehouseId, null, null),
                    dashboardMapper.selectScreenLatestRealAlerts(warehouseId, null, null),
                    dashboardMapper.selectScreenLatestPredictionAlerts(warehouseId, null, null),
                    dashboardMapper.selectScreenLatestGrainSummaries(warehouseId, null, null),
                    dashboardMapper.selectScreenWarehouseHealthList(warehouseId, null, null)
            );
        }
        // overview 只返回顶部聚合卡片，列表模块各自分页，避免首页初次加载就拉全量列表。
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

    public PageResult<DashboardAlertItemResponse> getAlertPage(Long warehouseId, String keyword, Integer pageNum, Integer pageSize) {
        if (warehouseId != null) {
            List<DashboardAlertItemResponse> alerts = Stream.concat(
                            dashboardMapper.selectScreenLatestRealAlerts(warehouseId, null, null).stream(),
                            dashboardMapper.selectScreenLatestPredictionAlerts(warehouseId, null, null).stream()
                    )
                    .sorted(Comparator.comparing(DashboardAlertItemResponse::eventTime,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .filter(item -> matchesAlertKeyword(item, keyword))
                    .toList();
            return paginateList(alerts, pageNum, pageSize);
        }
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

    public PageResult<DashboardWarehouseHealthResponse> getWarehouseHealthPage(Long warehouseId, String keyword, Integer pageNum, Integer pageSize) {
        if (warehouseId != null) {
            List<DashboardWarehouseHealthResponse> rows = dashboardMapper.selectScreenWarehouseHealthList(warehouseId, null, null).stream()
                    .filter(item -> matchesWarehouseHealthKeyword(item, keyword))
                    .toList();
            return paginateList(rows, pageNum, pageSize);
        }
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

    public PageResult<DashboardLatestSummaryResponse> getGrainSummaryPage(Long warehouseId, String keyword, Integer pageNum, Integer pageSize) {
        if (warehouseId != null) {
            List<DashboardLatestSummaryResponse> rows = dashboardMapper.selectScreenLatestGrainSummaries(warehouseId, null, null).stream()
                    .filter(item -> matchesGrainSummaryKeyword(item, keyword))
                    .toList();
            return paginateList(rows, pageNum, pageSize);
        }
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
        // 大屏不是单独维护一套口径，而是复用首页这套真实统计和预警聚合。
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
        // 首页只展示最近几条重点预警，因此这里先合并真实/预测预警，再统一排序截断。
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

    private boolean matchesAlertKeyword(DashboardAlertItemResponse item, String keyword) {
        return matchesKeyword(keyword,
                item.title(),
                item.level(),
                item.sourceType(),
                item.warehouseName(),
                item.eventTime(),
                item.description());
    }

    private boolean matchesWarehouseHealthKeyword(DashboardWarehouseHealthResponse item, String keyword) {
        return matchesKeyword(keyword,
                item.warehouseName(),
                item.healthScore(),
                item.riskLevel(),
                item.realWarningLevel(),
                item.predictionWarningLevel(),
                item.latestAvgTemp(),
                item.latestForecastValue());
    }

    private boolean matchesGrainSummaryKeyword(DashboardLatestSummaryResponse item, String keyword) {
        return matchesKeyword(keyword,
                item.warehouseName(),
                item.warningLevel(),
                item.avgTemp(),
                item.maxTemp(),
                item.minTemp(),
                item.collectedAt(),
                item.warningMessage());
    }

    private boolean matchesKeyword(String keyword, Object... values) {
        String normalized = normalizeKeyword(keyword);
        if (normalized == null) {
            return true;
        }
        String haystack = Stream.of(values)
                .map(value -> value == null ? "" : String.valueOf(value))
                .reduce((left, right) -> left + "|" + right)
                .orElse("")
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalized.toLowerCase(Locale.ROOT));
    }

    private <T> PageResult<T> paginateList(List<T> source, Integer pageNum, Integer pageSize) {
        int finalPageNum = normalizePageNum(pageNum);
        int finalPageSize = normalizePageSize(pageSize);
        long total = source.size();
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int fromIndex = (finalPageNum - 1) * finalPageSize;
        int toIndex = Math.min(fromIndex + finalPageSize, source.size());
        List<T> list = fromIndex >= source.size() ? List.of() : source.subList(fromIndex, toIndex);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }
}
