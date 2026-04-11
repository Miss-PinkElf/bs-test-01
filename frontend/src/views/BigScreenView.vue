<script setup>
import * as echarts from "echarts";
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchScreenDashboard, fetchWarehouses } from "../api/grain";

const DEFAULT_RANGE_DAYS = 7;
const SCREEN_NOTE_LINES = [
  "当前大屏优先展示粮温趋势、预警变化、预测任务与仓库对比。",
  "顶部指标、最新预警、重点仓库和最近预测任务均来自真实接口聚合结果。",
  "如需排查明细或回到业务链路，请使用“查看后台数据”进入管理台。"
];

const router = useRouter();
const warehouseOptions = ref([]);
const grainChartRef = ref(null);
const alertChartRef = ref(null);
const predictionChartRef = ref(null);
const warehouseChartRef = ref(null);

const chartInstances = new Map();
let resizeFrame = null;

function createEmptyOverview() {
  return {
    warehouseCount: 0,
    grainSummaryCount: 0,
    realAlertCount: 0,
    predictionAlertCount: 0,
    archivedPredictionCount: 0,
    latestAlerts: [],
    latestGrainSummaries: [],
    warehouseHealthList: []
  };
}

function formatApiDate(date) {
  const value = new Date(date);
  const pad = (num) => String(num).padStart(2, "0");

  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())} ${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`;
}

function createDefaultRange() {
  const end = new Date();
  const start = new Date(end.getTime() - (DEFAULT_RANGE_DAYS - 1) * 24 * 60 * 60 * 1000);
  return [formatApiDate(start), formatApiDate(end)];
}

const screenFilters = reactive({
  warehouseId: "",
  timeRange: createDefaultRange()
});

const screenState = reactive({
  pageReady: false,
  loading: false,
  errorMessage: "",
  lastUpdated: ""
});

const screenData = reactive({
  overview: createEmptyOverview(),
  grainTrend: [],
  alertTrend: [],
  predictionTrend: [],
  warehouseComparison: [],
  latestPredictionTasks: []
});

const screenCards = computed(() => {
  const overview = screenData.overview;
  const focusAlerts = Number(overview.realAlertCount || 0) + Number(overview.predictionAlertCount || 0);
  const placeholder = screenState.loading && !screenState.pageReady ? "--" : null;

  return [
    {
      key: "warehouse",
      label: "在线粮仓",
      value: placeholder ?? String(overview.warehouseCount || 0),
      note: "当前筛选范围内可展示的仓库数量"
    },
    {
      key: "summary",
      label: "粮温汇总",
      value: placeholder ?? String(overview.grainSummaryCount || 0),
      note: "当前时间窗内纳入大屏的粮温汇总记录数"
    },
    {
      key: "warning",
      label: "重点预警",
      value: placeholder ?? String(focusAlerts),
      note: "真实预警与预测预警合计数量"
    },
    {
      key: "prediction",
      label: "已归档预测",
      value: placeholder ?? String(overview.archivedPredictionCount || 0),
      note: "当前筛选范围内可追溯的预测任务数量"
    }
  ];
});

const latestAlerts = computed(() => screenData.overview.latestAlerts.slice(0, 5));
const latestPredictionTasks = computed(() => screenData.latestPredictionTasks.slice(0, 5));
const warehouseComparisonRows = computed(() => {
  if (screenData.warehouseComparison.length > 0) {
    return screenData.warehouseComparison.slice(0, 6);
  }

  return (screenData.overview.warehouseHealthList || []).slice(0, 6).map((item) => ({
    warehouseId: item.warehouseId,
    warehouseName: item.warehouseName,
    healthScore: item.healthScore,
    avgTemp: item.latestAvgTemp,
    latestForecastValue: item.latestForecastValue,
    riskLevel: item.riskLevel
  }));
});

function resolveRiskTagType(level) {
  if (level === "WARNING") {
    return "danger";
  }

  if (level === "ATTENTION") {
    return "warning";
  }

  if (level === "MAINTENANCE") {
    return "info";
  }

  return "success";
}

function resolveAlertSourceType(sourceType) {
  return sourceType === "PREDICTION" ? "warning" : "danger";
}

function formatAlertSource(sourceType) {
  return sourceType === "PREDICTION" ? "预测预警" : "真实预警";
}

function formatTemperature(value) {
  return value == null ? "-" : `${Number(value).toFixed(2)}°C`;
}

function openDashboard() {
  router.push("/dashboard");
}

function goToLogin() {
  router.push("/login");
}

function buildScreenParams() {
  const [startTime, endTime] = Array.isArray(screenFilters.timeRange) ? screenFilters.timeRange : [];

  return {
    warehouseId: screenFilters.warehouseId || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined
  };
}

function applyScreenData(result) {
  screenData.overview = result?.overview || createEmptyOverview();
  screenData.grainTrend = Array.isArray(result?.grainTrend) ? result.grainTrend : [];
  screenData.alertTrend = Array.isArray(result?.alertTrend) ? result.alertTrend : [];
  screenData.predictionTrend = Array.isArray(result?.predictionTrend) ? result.predictionTrend : [];
  screenData.warehouseComparison = Array.isArray(result?.warehouseComparison) ? result.warehouseComparison : [];
  screenData.latestPredictionTasks = Array.isArray(result?.latestPredictionTasks) ? result.latestPredictionTasks : [];
}

async function loadWarehouseOptions() {
  try {
    warehouseOptions.value = await fetchWarehouses();
  } catch (error) {
    console.warn(error);
    warehouseOptions.value = [];
  }
}

function disposeChart(key) {
  const instance = chartInstances.get(key);
  if (!instance) {
    return;
  }

  instance.dispose();
  chartInstances.delete(key);
}

function getChartInstance(key, element) {
  if (!element) {
    disposeChart(key);
    return null;
  }

  const current = chartInstances.get(key);
  if (current) {
    return current;
  }

  const instance = echarts.init(element);
  chartInstances.set(key, instance);
  return instance;
}

function getCommonChartStyle() {
  return {
    textStyle: {
      color: "rgba(238, 248, 243, 0.88)",
      fontFamily: '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif'
    },
    grid: {
      left: 28,
      right: 18,
      top: 38,
      bottom: 28,
      containLabel: true
    },
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(9, 33, 29, 0.92)",
      borderColor: "rgba(255,255,255,0.16)",
      textStyle: {
        color: "#eef8f3"
      }
    },
    legend: {
      textStyle: {
        color: "rgba(238, 248, 243, 0.76)"
      }
    }
  };
}

function buildGrainTrendOption() {
  const rows = screenData.grainTrend;
  const common = getCommonChartStyle();

  return {
    ...common,
    legend: {
      ...common.legend,
      data: ["粮温趋势", "最高温"]
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: rows.map((item) => item.timeLabel),
      axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
      axisLine: { lineStyle: { color: "rgba(255,255,255,0.18)" } }
    },
    yAxis: {
      type: "value",
      axisLabel: {
        color: "rgba(238, 248, 243, 0.72)",
        formatter: (value) => `${value}°C`
      },
      splitLine: { lineStyle: { color: "rgba(255,255,255,0.08)" } }
    },
    series: [
      {
        name: "粮温趋势",
        type: "line",
        smooth: true,
        data: rows.map((item) => item.primaryValue),
        lineStyle: { color: "#0b7a75", width: 3 },
        itemStyle: { color: "#0b7a75" },
        areaStyle: { color: "rgba(11, 122, 117, 0.18)" }
      },
      {
        name: "最高温",
        type: "line",
        smooth: true,
        data: rows.map((item) => item.secondaryValue),
        lineStyle: { color: "#d97706", width: 2 },
        itemStyle: { color: "#d97706" }
      }
    ]
  };
}

function buildAlertTrendOption() {
  const rows = screenData.alertTrend;
  const common = getCommonChartStyle();

  return {
    ...common,
    legend: {
      ...common.legend,
      data: ["真实预警", "预测预警"]
    },
    xAxis: {
      type: "category",
      data: rows.map((item) => item.timeLabel),
      axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
      axisLine: { lineStyle: { color: "rgba(255,255,255,0.18)" } }
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
      splitLine: { lineStyle: { color: "rgba(255,255,255,0.08)" } }
    },
    series: [
      {
        name: "真实预警",
        type: "bar",
        data: rows.map((item) => item.realAlertCount),
        itemStyle: { color: "#d97706" },
        barMaxWidth: 24
      },
      {
        name: "预测预警",
        type: "line",
        smooth: true,
        data: rows.map((item) => item.predictionAlertCount),
        lineStyle: { color: "#f59e0b", width: 2 },
        itemStyle: { color: "#f59e0b" }
      }
    ]
  };
}

function buildPredictionTrendOption() {
  const rows = screenData.predictionTrend;
  const common = getCommonChartStyle();

  return {
    ...common,
    legend: {
      ...common.legend,
      data: ["预测任务数", "预测预警点"]
    },
    xAxis: {
      type: "category",
      data: rows.map((item) => item.timeLabel),
      axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
      axisLine: { lineStyle: { color: "rgba(255,255,255,0.18)" } }
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
      splitLine: { lineStyle: { color: "rgba(255,255,255,0.08)" } }
    },
    series: [
      {
        name: "预测任务数",
        type: "bar",
        data: rows.map((item) => item.primaryValue),
        itemStyle: { color: "#7c9a92" },
        barMaxWidth: 22
      },
      {
        name: "预测预警点",
        type: "line",
        smooth: true,
        data: rows.map((item) => item.secondaryValue),
        lineStyle: { color: "#8b5cf6", width: 2 },
        itemStyle: { color: "#8b5cf6" }
      }
    ]
  };
}

function buildWarehouseComparisonOption() {
  const rows = warehouseComparisonRows.value;
  const common = getCommonChartStyle();

  return {
    ...common,
    legend: {
      ...common.legend,
      data: ["健康分", "预测峰值"]
    },
    xAxis: {
      type: "category",
      data: rows.map((item) => item.warehouseName),
      axisLabel: {
        color: "rgba(238, 248, 243, 0.72)",
        interval: 0,
        rotate: rows.length > 4 ? 18 : 0
      },
      axisLine: { lineStyle: { color: "rgba(255,255,255,0.18)" } }
    },
    yAxis: [
      {
        type: "value",
        name: "健康分",
        axisLabel: { color: "rgba(238, 248, 243, 0.72)" },
        splitLine: { lineStyle: { color: "rgba(255,255,255,0.08)" } }
      },
      {
        type: "value",
        name: "温度",
        axisLabel: {
          color: "rgba(238, 248, 243, 0.72)",
          formatter: (value) => `${value}°C`
        },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: "健康分",
        type: "bar",
        data: rows.map((item) => item.healthScore),
        itemStyle: { color: "#0b7a75" },
        barMaxWidth: 24
      },
      {
        name: "预测峰值",
        type: "line",
        yAxisIndex: 1,
        smooth: true,
        data: rows.map((item) => item.latestForecastValue ?? item.avgTemp),
        lineStyle: { color: "#d97706", width: 2 },
        itemStyle: { color: "#d97706" }
      }
    ]
  };
}

function renderChart(key, chartRef, hasData, optionBuilder) {
  if (!hasData) {
    disposeChart(key);
    return;
  }

  const instance = getChartInstance(key, chartRef?.value);
  if (!instance) {
    return;
  }

  instance.setOption(optionBuilder(), true);
}

function renderAllCharts() {
  renderChart("grain", grainChartRef, screenData.grainTrend.length > 0, buildGrainTrendOption);
  renderChart("alert", alertChartRef, screenData.alertTrend.length > 0, buildAlertTrendOption);
  renderChart("prediction", predictionChartRef, screenData.predictionTrend.length > 0, buildPredictionTrendOption);
  renderChart("warehouse", warehouseChartRef, warehouseComparisonRows.value.length > 0, buildWarehouseComparisonOption);
}

function scheduleResize() {
  if (resizeFrame) {
    cancelAnimationFrame(resizeFrame);
  }

  resizeFrame = requestAnimationFrame(() => {
    chartInstances.forEach((instance) => instance.resize());
  });
}

async function loadScreenData() {
  screenState.loading = true;
  screenState.errorMessage = "";

  try {
    const result = await fetchScreenDashboard(buildScreenParams());
    applyScreenData(result);
    screenState.lastUpdated = formatApiDate(new Date());
  } catch (error) {
    console.warn(error);
    screenState.errorMessage = "大屏数据加载失败，请先检查网络与接口状态；若仍无法恢复，使用“查看后台数据”进入管理台排查。";
    applyScreenData({ overview: createEmptyOverview() });
  } finally {
    screenState.pageReady = true;
    screenState.loading = false;
    await nextTick();
    requestAnimationFrame(() => {
      renderAllCharts();
      scheduleResize();
    });
  }
}

async function handleSearch() {
  await loadScreenData();
}

async function resetFilters() {
  screenFilters.warehouseId = "";
  screenFilters.timeRange = createDefaultRange();
  await loadScreenData();
}

onMounted(async () => {
  await Promise.allSettled([loadWarehouseOptions(), loadScreenData()]);
  window.addEventListener("resize", scheduleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", scheduleResize);
  if (resizeFrame) {
    cancelAnimationFrame(resizeFrame);
  }
  chartInstances.forEach((instance) => instance.dispose());
  chartInstances.clear();
});
</script>

<template>
  <div class="screen-page">
    <div class="screen-topbar">
      <div class="screen-topbar-main">
        <div class="screen-title">粮仓环境数据预测管理平台</div>
        <div class="screen-subtitle">答辩展示大屏</div>
        <div class="screen-last-updated" v-if="screenState.lastUpdated">最近刷新：{{ screenState.lastUpdated }}</div>
      </div>
      <div class="screen-actions">
        <el-button type="primary" @click="openDashboard">查看后台数据</el-button>
        <el-button plain @click="goToLogin">返回登录</el-button>
      </div>
    </div>

    <el-card class="screen-panel screen-filter-bar" shadow="never">
      <div class="screen-filter-content">
        <div class="screen-filter-title">共享查询</div>
        <div class="toolbar-row">
          <el-select v-model="screenFilters.warehouseId" clearable placeholder="全部仓库" class="screen-filter-select">
            <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-date-picker
            v-model="screenFilters.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            class="screen-filter-range"
          />
          <el-button type="primary" :loading="screenState.loading" @click="handleSearch">刷新大屏</el-button>
          <el-button plain @click="resetFilters">重置筛选</el-button>
        </div>
      </div>
      <div v-if="screenState.errorMessage" class="screen-module-state screen-page-error">
        {{ screenState.errorMessage }}
      </div>
    </el-card>

    <div class="screen-metrics">
      <div v-for="item in screenCards" :key="item.key" class="screen-metric-card">
        <div class="screen-metric-label">{{ item.label }}</div>
        <div class="screen-metric-value">{{ item.value }}</div>
        <div class="screen-metric-note">{{ item.note }}</div>
      </div>
    </div>

    <div class="screen-main-grid">
      <el-card class="screen-panel screen-chart-panel screen-chart-primary" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">粮温趋势</div>
              <div class="panel-subtitle">主图位，展示当前筛选窗口内的粮温主线与最高温参考线。</div>
            </div>
          </div>
        </template>
        <div v-if="screenData.grainTrend.length > 0" ref="grainChartRef" class="chart-box screen-chart-box"></div>
        <div v-else class="screen-module-state">暂无趋势数据</div>
      </el-card>

      <el-card class="screen-panel screen-chart-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">预警趋势</div>
              <div class="panel-subtitle">对比真实预警与预测预警在时间窗口内的变化。</div>
            </div>
          </div>
        </template>
        <div v-if="screenData.alertTrend.length > 0" ref="alertChartRef" class="chart-box screen-chart-box"></div>
        <div v-else class="screen-module-state">暂无趋势数据</div>
      </el-card>

      <el-card class="screen-panel screen-chart-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">预测趋势</div>
              <div class="panel-subtitle">展示预测任务量与预测预警点变化，补齐“分析预测”链路。</div>
            </div>
          </div>
        </template>
        <div v-if="screenData.predictionTrend.length > 0" ref="predictionChartRef" class="chart-box screen-chart-box"></div>
        <div v-else class="screen-module-state">暂无趋势数据</div>
      </el-card>

      <el-card class="screen-panel screen-chart-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">仓库对比</div>
              <div class="panel-subtitle">按仓库横向查看健康分与预测峰值差异。</div>
            </div>
          </div>
        </template>
        <div v-if="warehouseComparisonRows.length > 0" ref="warehouseChartRef" class="chart-box screen-chart-box"></div>
        <div v-else class="screen-module-state">暂无趋势数据</div>
      </el-card>
    </div>

    <div class="screen-detail-grid">
      <el-card class="screen-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">最新预警</div>
              <div class="panel-subtitle">按时间倒序展示当前窗口内最值得优先关注的预警摘要。</div>
            </div>
          </div>
        </template>
        <div v-if="latestAlerts.length > 0" class="screen-detail-list">
          <div v-for="item in latestAlerts" :key="`${item.sourceType}-${item.eventTime}-${item.title}`" class="screen-list-item">
            <div class="screen-list-head">
              <strong>{{ item.title }}</strong>
              <div class="tag-row">
                <el-tag size="small" :type="resolveAlertSourceType(item.sourceType)">{{ formatAlertSource(item.sourceType) }}</el-tag>
                <el-tag size="small" :type="resolveRiskTagType(item.level)">{{ item.level }}</el-tag>
              </div>
            </div>
            <div class="screen-list-meta">{{ item.warehouseName }} · {{ item.eventTime || "时间待补充" }}</div>
            <div class="screen-list-summary">{{ item.description }}</div>
          </div>
        </div>
        <div v-else class="screen-module-state">暂无记录</div>
      </el-card>

      <el-card class="screen-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">最近预测任务</div>
              <div class="panel-subtitle">展示最近可追溯的预测任务与风险级别。</div>
            </div>
          </div>
        </template>
        <div v-if="latestPredictionTasks.length > 0" class="screen-detail-list">
          <div v-for="item in latestPredictionTasks" :key="item.taskId || item.taskNo" class="screen-list-item">
            <div class="screen-list-head">
              <strong>{{ item.taskNo }}</strong>
              <el-tag size="small" :type="resolveRiskTagType(item.riskLevel)">{{ item.riskLevel }}</el-tag>
            </div>
            <div class="screen-list-meta">{{ item.warehouseName }} · {{ item.requestedAt || "时间待补充" }} · {{ item.forecastDays }} 天</div>
            <div class="screen-list-summary">{{ item.summary || "暂无任务摘要" }}</div>
          </div>
        </div>
        <div v-else class="screen-module-state">暂无记录</div>
      </el-card>

      <el-card class="screen-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">重点仓库</div>
              <div class="panel-subtitle">结合健康分、均温与预测峰值快速查看仓库差异。</div>
            </div>
          </div>
        </template>
        <div v-if="warehouseComparisonRows.length > 0" class="screen-detail-list">
          <div v-for="item in warehouseComparisonRows" :key="item.warehouseId || item.warehouseName" class="screen-list-item screen-warehouse-row">
            <div>
              <strong>{{ item.warehouseName }}</strong>
              <div class="screen-list-meta">均温 {{ formatTemperature(item.avgTemp) }} · 预测峰值 {{ formatTemperature(item.latestForecastValue) }}</div>
            </div>
            <div class="screen-warehouse-side">
              <div class="screen-warehouse-score">{{ item.healthScore ?? 0 }}</div>
              <el-tag size="small" :type="resolveRiskTagType(item.riskLevel)">{{ item.riskLevel }}</el-tag>
            </div>
          </div>
        </div>
        <div v-else class="screen-module-state">暂无记录</div>
      </el-card>

      <el-card class="screen-panel screen-note-card" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">说明</div>
              <div class="panel-subtitle">仅保留答辩讲解所需的轻量说明，不再占用主视觉。</div>
            </div>
          </div>
        </template>
        <div class="compact-lines">
          <div v-for="line in SCREEN_NOTE_LINES" :key="line">{{ line }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>
