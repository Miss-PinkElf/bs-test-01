<script setup>
import { Search } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import * as echarts from "echarts";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import {
  batchDeletePredictionTasks,
  deletePredictionTask,
  fetchPredictionTasksPage,
  fetchWarehouses,
  predictMetric
} from "../api/grain";
import { useAuthStore } from "../stores/auth";

const DAY_IN_MS = 24 * 60 * 60 * 1000;
const DEFAULT_PREDICTION_CHART_DAYS = 7;
const MAX_CHART_AXIS_LABELS = 8;

const chartRef = ref();
const chartAnchorRef = ref();
const taskSummaryCardRef = ref();

let summaryFlashTimer = null;
let chart;

const authStore = useAuthStore();
// 当前页同时维护三块状态：预测表单、当前选中的任务摘要、历史任务分页表格。
const loading = ref(false);
const historyLoading = ref(false);
const warehouses = ref([]);
const predictionHistory = ref([]);
const selectedTaskId = ref(null);
const historyTableKeyword = ref("");
const historyTableRef = ref();
const historySelection = ref([]);
const predictionChartRange = ref(null);
const historyPageState = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

watch(historyTableKeyword, async () => {
  historyPageState.pageNum = 1;
  await loadPredictionHistoryPage();
});

const prediction = ref({
  taskNo: "",
  warehouseName: "",
  targetType: "AVG_TEMP",
  algorithmName: "",
  riskLevel: "NORMAL",
  forecastDays: 0,
  trainStartTime: "",
  trainEndTime: "",
  forecastStartTime: "",
  forecastEndTime: "",
  requestedAt: "",
  summary: "",
  resultList: []
});

const targetOptions = [
  { value: "AVG_TEMP", label: "整仓平均温度" },
  { value: "LAYER_1_AVG", label: "第一层平均温度" },
  { value: "LAYER_2_AVG", label: "第二层平均温度" },
  { value: "LAYER_3_AVG", label: "第三层平均温度" },
  { value: "LAYER_4_AVG", label: "第四层平均温度" }
];

const form = reactive({
  warehouseId: "",
  targetType: "AVG_TEMP",
  forecastDays: 7
});
const trainRange = ref(null);
const trainCollapse = ref([]);
const isWarehouseManager = computed(() => authStore.isWarehouseManager);
const isViewer = computed(() => authStore.isViewer);
const managedWarehouseId = computed(() => authStore.managedWarehouseId ?? null);
const canExecutePrediction = computed(() => !isViewer.value);
const canDeletePrediction = computed(() => !isViewer.value);
const visibleWarehouses = computed(() => {
  if (!isWarehouseManager.value || managedWarehouseId.value == null) {
    return warehouses.value;
  }

  return warehouses.value.filter((item) => item.id === managedWarehouseId.value);
});
const filteredPredictionResultList = computed(() => {
  const resultList = Array.isArray(prediction.value.resultList) ? prediction.value.resultList : [];

  if (!Array.isArray(predictionChartRange.value) || predictionChartRange.value.length !== 2) {
    return resultList;
  }

  const [start, end] = predictionChartRange.value;
  const startTime = parseDateTimeValue(start);
  const endTime = parseDateTimeValue(end);

  if (!startTime || !endTime) {
    return resultList;
  }

  return resultList.filter((item) => {
    const resultTime = parseDateTimeValue(item.resultTime);
    return resultTime && resultTime >= startTime && resultTime <= endTime;
  });
});

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function formatDatePart(value) {
  return String(value).padStart(2, "0");
}

function formatDateTimeForPicker(date) {
  return `${date.getFullYear()}-${formatDatePart(date.getMonth() + 1)}-${formatDatePart(date.getDate())} ${formatDatePart(date.getHours())}:${formatDatePart(date.getMinutes())}:${formatDatePart(date.getSeconds())}`;
}

function parseDateTimeValue(value) {
  if (!value) {
    return null;
  }

  const normalized = String(value).includes("T") ? String(value) : String(value).replace(" ", "T");
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
}

function formatChartAxisLabel(value) {
  const normalized = formatDateTime(value);
  return normalized === "-" ? "" : normalized.slice(5, 16);
}

function resolveWarehouseScope(warehouseId) {
  if (isWarehouseManager.value) {
    return managedWarehouseId.value || warehouseId;
  }
  return warehouseId;
}

function applyManagedWarehouseScope() {
  if (!isWarehouseManager.value || managedWarehouseId.value == null) {
    return;
  }

  form.warehouseId = managedWarehouseId.value;
}

function getChartAxisLabelInterval(pointCount) {
  if (pointCount <= MAX_CHART_AXIS_LABELS) {
    return 0;
  }
  return Math.max(0, Math.ceil(pointCount / MAX_CHART_AXIS_LABELS) - 1);
}

function getTargetLabel(value) {
  return targetOptions.find((item) => item.value === value)?.label || value;
}

function resolveHistoryRowClassName({ row }) {
  return row.taskId === selectedTaskId.value ? "interactive-table-row is-active-row" : "interactive-table-row";
}

function createEmptyPrediction() {
  return {
    taskNo: "",
    warehouseName: "",
    targetType: "AVG_TEMP",
    algorithmName: "",
    riskLevel: "NORMAL",
    forecastDays: 0,
    trainStartTime: "",
    trainEndTime: "",
    forecastStartTime: "",
    forecastEndTime: "",
    requestedAt: "",
    summary: "",
    resultList: []
  };
}

function clearPredictionVisual() {
  prediction.value = createEmptyPrediction();
  predictionChartRange.value = null;
  selectedTaskId.value = null;
  if (chart) {
    chart.dispose();
    chart = null;
  }
}

function onHistorySelectionChange(rows) {
  historySelection.value = rows ?? [];
}

// 训练区间校验只拦截明显无效的前端输入，空区间仍交给后端按默认训练窗口处理。
function validateTrainRange() {
  if (!trainRange.value || trainRange.value.length !== 2) {
    return true;
  }

  const [start, end] = trainRange.value;

  if (!start || !end) {
    return true;
  }

  if (start >= end) {
    ElMessage.warning("训练区间结束时间应晚于开始时间");
    return false;
  }

  return true;
}

function clearTrainRange() {
  trainRange.value = null;
}

function buildDefaultPredictionChartRange(resultList) {
  const validTimes = resultList
    .map((item) => parseDateTimeValue(item.resultTime))
    .filter((item) => item instanceof Date)
    .sort((left, right) => left.getTime() - right.getTime());

  if (validTimes.length === 0) {
    return null;
  }

  const firstTime = validTimes[0];
  const lastTime = validTimes.at(-1);

  if (!lastTime || lastTime.getTime() - firstTime.getTime() <= DEFAULT_PREDICTION_CHART_DAYS * DAY_IN_MS) {
    return [formatDateTimeForPicker(firstTime), formatDateTimeForPicker(lastTime || firstTime)];
  }

  const start = new Date(lastTime.getTime() - DEFAULT_PREDICTION_CHART_DAYS * DAY_IN_MS);
  return [formatDateTimeForPicker(start), formatDateTimeForPicker(lastTime)];
}

function syncPredictionChartRange(resultList = prediction.value.resultList) {
  predictionChartRange.value = Array.isArray(resultList) && resultList.length > 0
    ? buildDefaultPredictionChartRange(resultList)
    : null;
}

function clearPredictionChartRange() {
  predictionChartRange.value = null;
}

async function loadWarehouses() {
  try {
    warehouses.value = await fetchWarehouses();
    applyManagedWarehouseScope();
    if (!form.warehouseId && visibleWarehouses.value.length > 0) {
      form.warehouseId = visibleWarehouses.value[0].id;
    }
  } catch (error) {
    ElMessage.error(error?.message || "仓库列表加载失败");
  }
}

async function loadPredictionHistoryPage() {
  historyLoading.value = true;

  try {
    const page = await fetchPredictionTasksPage({
      keyword: historyTableKeyword.value.trim() || undefined,
      pageNum: historyPageState.pageNum,
      pageSize: historyPageState.pageSize
    });
    // 历史分页刷新后尽量维持当前任务选中态，真正的摘要与图表切换仍由 selectedTaskId 单独控制。
    predictionHistory.value = page.list;
    historyPageState.pageNum = page.pageNum;
    historyPageState.pageSize = page.pageSize;
    historyPageState.total = page.total;
  } catch (error) {
    ElMessage.error(error?.message || "预测记录加载失败");
  } finally {
    historyLoading.value = false;
  }
}

async function handleHistoryPageChange(pageNum) {
  historyPageState.pageNum = pageNum;
  await loadPredictionHistoryPage();
}

async function handleHistoryPageSizeChange(pageSize) {
  historyPageState.pageSize = pageSize;
  historyPageState.pageNum = 1;
  await loadPredictionHistoryPage();
}

async function runPrediction() {
  if (!canExecutePrediction.value) {
    return;
  }

  if (!validateTrainRange()) {
    return;
  }

  loading.value = true;

  try {
    const payload = {
      warehouseId: resolveWarehouseScope(form.warehouseId),
      metricCode: "temperature",
      targetType: form.targetType,
      forecastDays: form.forecastDays
    };

    if (trainRange.value?.length === 2 && trainRange.value[0] && trainRange.value[1]) {
      payload.trainStartTime = trainRange.value[0];
      payload.trainEndTime = trainRange.value[1];
    }

    // 执行预测后，页面主视觉立刻切到新任务，并刷新下方历史归档表。
    prediction.value = await predictMetric(payload);
    syncPredictionChartRange(prediction.value.resultList);
    selectedTaskId.value = prediction.value.taskId;
    historyPageState.pageNum = 1;
    await loadPredictionHistoryPage();
    await nextTick();
    renderChart();
    await nextTick();
    chartAnchorRef.value?.scrollIntoView?.({ behavior: "smooth", block: "start" });
  } catch (error) {
    ElMessage.error(error?.message || "预测失败");
  } finally {
    loading.value = false;
  }
}

// 当前任务详情和历史表格解耦，切换任务时只替换主视觉，不回写历史分页查询态。
async function selectPredictionTask(item) {
  selectedTaskId.value = item.taskId;
  prediction.value = item;
  syncPredictionChartRange(item.resultList);
  await nextTick();
  renderChart();
  await nextTick();
  chartAnchorRef.value?.scrollIntoView?.({ behavior: "smooth", block: "nearest" });
}

async function focusTaskSummary(row) {
  if (row.taskId !== selectedTaskId.value) {
    await selectPredictionTask(row);
  } else {
    await nextTick();
  }
  await nextTick();
  const cardInst = taskSummaryCardRef.value;
  const element = cardInst?.$el ?? cardInst;
  if (!element || typeof element.scrollIntoView !== "function") {
    return;
  }
  element.scrollIntoView({ behavior: "smooth", block: "center" });
  element.classList.remove("prediction-summary-flash");
  void element.offsetWidth;
  element.classList.add("prediction-summary-flash");
  if (summaryFlashTimer != null) {
    clearTimeout(summaryFlashTimer);
  }
  summaryFlashTimer = window.setTimeout(() => {
    element.classList.remove("prediction-summary-flash");
    summaryFlashTimer = null;
  }, 1500);
}

async function afterDeleteRefresh(deletedIds) {
  const deletedSet = new Set(deletedIds.map((id) => Number(id)));
  const touchedCurrent = selectedTaskId.value != null && deletedSet.has(Number(selectedTaskId.value));
  await loadPredictionHistoryPage();
  await nextTick();
  historyTableRef.value?.clearSelection?.();
  historySelection.value = [];
  if (touchedCurrent) {
    // 删除当前任务后同步清空摘要和图表主视觉，避免页面继续展示已失效的任务结果。
    clearPredictionVisual();
  }
}

async function handleDeleteRow(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除任务“${row.taskNo}”吗？删除后不可恢复。`,
      "删除确认",
      { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
  } catch {
    return;
  }

  try {
    await deletePredictionTask(row.taskId);
    ElMessage.success("已删除");
    await afterDeleteRefresh([row.taskId]);
  } catch (error) {
    ElMessage.error(error?.message || "删除失败");
  }
}

async function handleBatchDelete() {
  const rows = historySelection.value;
  if (rows.length === 0) {
    return;
  }

  const ids = rows.map((row) => row.taskId);

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${ids.length} 条预测记录吗？删除后不可恢复。`,
      "批量删除确认",
      { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
  } catch {
    return;
  }

  try {
    await batchDeletePredictionTasks(ids);
    ElMessage.success("已删除");
    await afterDeleteRefresh(ids);
  } catch (error) {
    ElMessage.error(error?.message || "删除失败");
  }
}

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  const chartRows = filteredPredictionResultList.value;
  const chartXAxisData = chartRows.map((item) => formatDateTime(item.resultTime));
  const denseAxis = chartXAxisData.length > MAX_CHART_AXIS_LABELS;

  // 图表只消费过滤后的结果时间线，让查看窗口变化不影响任务摘要和历史任务语义。
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["实际值", "预测值"] },
    grid: { left: 40, right: 24, top: 34, bottom: denseAxis ? 88 : 56 },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: chartXAxisData,
      axisTick: { alignWithLabel: true },
      axisLabel: {
        interval: getChartAxisLabelInterval(chartXAxisData.length),
        rotate: denseAxis ? 32 : 0,
        hideOverlap: true,
        formatter: (value) => formatChartAxisLabel(value)
      }
    },
    yAxis: { type: "value" },
    dataZoom: [
      { type: "inside", filterMode: "none" },
      {
        type: "slider",
        filterMode: "none",
        height: 18,
        bottom: 16,
        show: denseAxis
      }
    ],
    series: [
      {
        name: "实际值",
        type: "line",
        smooth: true,
        connectNulls: true,
        data: chartRows.map((item) => item.actualValue),
        lineStyle: { color: "#a855f7" },
        itemStyle: { color: "#a855f7" }
      },
      {
        name: "预测值",
        type: "line",
        smooth: true,
        connectNulls: true,
        data: chartRows.map((item) => item.predictedValue),
        lineStyle: { color: "#ea580c" },
        itemStyle: { color: "#ea580c" },
        areaStyle: {
          color: "rgba(234, 88, 12, 0.12)"
        }
      }
    ]
  }, true);

  requestAnimationFrame(() => {
    const element = chartRef.value;
    if (!element || !chart) {
      return;
    }

    chart.resize({
      width: element.clientWidth,
      height: element.clientHeight,
      animation: { duration: 0 }
    });
  });
}

onMounted(async () => {
  // 页面首次进入时优先回显已有归档；如果还没有历史任务，再自动跑一轮默认预测。
  await loadWarehouses();
  await loadPredictionHistoryPage();

  if (predictionHistory.value.length > 0) {
    await selectPredictionTask(predictionHistory.value[0]);
    return;
  }

  if (canExecutePrediction.value && form.warehouseId) {
    await runPrediction();
  }
});

watch(predictionChartRange, async () => {
  await nextTick();
  renderChart();
});
watch(managedWarehouseId, () => { applyManagedWarehouseScope(); });

onBeforeUnmount(() => {
  if (summaryFlashTimer != null) {
    clearTimeout(summaryFlashTimer);
  }
  if (chart) {
    chart.dispose();
  }
});
</script>

<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="24" :md="15" :lg="15" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">滚动预测参数</div>
          </template>

          <el-form inline>
            <el-form-item label="仓库">
              <el-select v-model="form.warehouseId" class="prediction-select-md" :disabled="isWarehouseManager">
                <el-option
                  v-for="item in visibleWarehouses"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="预测对象">
              <el-select v-model="form.targetType" class="prediction-select-md">
                <el-option
                  v-for="item in targetOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="预测天数">
              <el-input-number v-model="form.forecastDays" :min="1" :max="30" />
            </el-form-item>

            <el-form-item>
              <el-button v-if="canExecutePrediction" type="primary" :loading="loading" @click="runPrediction">
                执行预测
              </el-button>
            </el-form-item>
          </el-form>

          <el-collapse v-model="trainCollapse" class="prediction-train-collapse">
            <el-collapse-item title="高级：训练数据区间（可选）" name="train">
              <div class="prediction-advanced-train">
                <el-date-picker
                  v-model="trainRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="训练开始"
                  end-placeholder="训练结束"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  class="prediction-train-range-picker"
                />
                <el-button text type="primary" @click="clearTrainRange">清空（使用全部历史）</el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
        <el-card ref="taskSummaryCardRef" class="panel-card task-summary-card" shadow="never">
          <template #header>
            <div class="panel-title">任务摘要</div>
          </template>

          <div class="task-summary-table-wrap">
            <el-descriptions :column="2" border size="small" class="task-summary-descriptions">
              <el-descriptions-item label="任务号" :span="2">
                {{ prediction.taskNo || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="仓库">
                {{ prediction.warehouseName || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="预测对象">
                {{ getTargetLabel(prediction.targetType) }}
              </el-descriptions-item>
              <el-descriptions-item label="算法">
                {{ prediction.algorithmName || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                {{ prediction.riskLevel }}
              </el-descriptions-item>
              <el-descriptions-item label="预测天数">
                {{ prediction.forecastDays }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="task-summary-table-wrap">
            <el-descriptions :column="1" border size="small" class="task-summary-descriptions">
              <el-descriptions-item label="训练区间">
                {{ formatDateTime(prediction.trainStartTime) }} ~ {{ formatDateTime(prediction.trainEndTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="预测区间">
                {{ formatDateTime(prediction.forecastStartTime) }} ~ {{ formatDateTime(prediction.forecastEndTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="执行时间">
                {{ formatDateTime(prediction.requestedAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="任务摘要">
                {{ prediction.summary || "预测执行完成" }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div ref="chartAnchorRef" class="prediction-chart-anchor">
      <el-card class="panel-card" shadow="never">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">实际值 / 预测值双线图</div>
              <div class="panel-subtitle">对应上方「任务摘要」中的当前任务；预测区间内若已录入新的真实值，会自动回填到图中做对照。默认展示最近 7 天窗口，清空后可回看完整时间线</div>
            </div>
            <div class="chart-card-toolbar">
              <el-date-picker
                v-model="predictionChartRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="图表开始"
                end-placeholder="图表结束"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="chart-range-picker"
                :disabled="prediction.resultList.length === 0"
              />
              <el-button text type="primary" :disabled="prediction.resultList.length === 0" @click="clearPredictionChartRange">
                清空
              </el-button>
            </div>
          </div>
        </template>

        <div ref="chartRef" class="chart-box"></div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card class="panel-card prediction-records-card" shadow="never">
          <template #header>
            <div>
              <div class="panel-title">预测记录</div>
              <div class="panel-subtitle">
                每次预测一条记录。曲线仍对应当前在「操作」列切换的任务；可勾选多行后「批量删除」，或在操作列单条删除（均需确认）。
              </div>
            </div>
          </template>

          <div class="toolbar-row table-toolbar">
            <el-button v-if="canDeletePrediction" type="danger" :disabled="historySelection.length === 0" @click="handleBatchDelete">
              批量删除
            </el-button>
            <el-input
              v-model="historyTableKeyword"
              class="table-search-input"
              clearable
              placeholder="搜索任务号、仓库、风险、天数、预测区间、执行时间"
              :prefix-icon="Search"
            />
          </div>

          <div class="prediction-history-table-wrap">
            <el-table
              ref="historyTableRef"
              class="prediction-history-table"
              :data="predictionHistory"
              stripe
              v-loading="historyLoading"
              :row-class-name="resolveHistoryRowClassName"
              @selection-change="onHistorySelectionChange"
            >
              <el-table-column type="selection" width="48" />
              <el-table-column prop="taskNo" label="任务号" min-width="160" />
              <el-table-column label="预测对象" min-width="130">
                <template #default="{ row }">
                  {{ getTargetLabel(row.targetType) }}
                </template>
              </el-table-column>
              <el-table-column prop="warehouseName" label="仓库" min-width="120" />
              <el-table-column prop="riskLevel" label="风险等级" width="110" />
              <el-table-column prop="forecastDays" label="预测天数" width="110" />
              <el-table-column label="预测区间" min-width="200">
                <template #default="{ row }">
                  {{ formatDateTime(row.forecastStartTime) }} ~ {{ formatDateTime(row.forecastEndTime) }}
                </template>
              </el-table-column>
              <el-table-column label="执行时间" min-width="160">
                <template #default="{ row }">
                  {{ formatDateTime(row.requestedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="248">
                <template #default="{ row }">
                  <el-button link type="primary" @click.stop="focusTaskSummary(row)">查看摘要</el-button>
                  <el-button link type="primary" @click.stop="selectPredictionTask(row)">切换任务</el-button>
                  <el-button v-if="canDeletePrediction" link type="danger" @click.stop="handleDeleteRow(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="historyPageState.pageNum"
              :page-size="historyPageState.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="historyPageState.total"
              @current-change="handleHistoryPageChange"
              @size-change="handleHistoryPageSizeChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
