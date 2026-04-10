<script setup>
import { Search } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import * as echarts from "echarts";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import {
  batchDeletePredictionTasks,
  deletePredictionTask,
  fetchPredictionTasks,
  fetchWarehouses,
  predictMetric
} from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";
import { filterRows } from "../utils/fuzzyText";

const chartRef = ref();
/** 预测后滚动定位，避免主视觉在视口外 */
const chartAnchorRef = ref();
/** 任务摘要卡片：查看摘要时滚动定位与短暂强调 */
const taskSummaryCardRef = ref();

let summaryFlashTimer = null;
const loading = ref(false);
const historyLoading = ref(false);
const warehouses = ref([]);
const predictionHistory = ref([]);
const selectedTaskId = ref(null);
const historyTableKeyword = ref("");
const historyTableRef = ref();
const historySelection = ref([]);

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

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getTargetLabel(value) {
  return targetOptions.find((item) => item.value === value)?.label || value;
}

const filteredPredictionHistory = computed(() =>
  filterRows(predictionHistory.value, historyTableKeyword.value, (row) => {
    const forecastStart = formatDateTime(row.forecastStartTime);
    const forecastEnd = formatDateTime(row.forecastEndTime);
    return [
      row.taskNo,
      getTargetLabel(row.targetType),
      row.warehouseName,
      row.riskLevel,
      String(row.forecastDays ?? ""),
      `${forecastStart} ~ ${forecastEnd}`,
      formatDateTime(row.requestedAt)
    ];
  })
);

const historyPagination = useClientPagination(filteredPredictionHistory);

const batchDeleteDisabled = computed(() => historySelection.value.length === 0);

watch(historyTableKeyword, () => {
  historyPagination.resetPagination();
});

const form = reactive({
  warehouseId: "",
  targetType: "AVG_TEMP",
  forecastDays: 7
});

/** 可选训练窗口，提交时映射为 trainStartTime / trainEndTime；清空则不传（全量历史） */
const trainRange = ref(null);
/** 默认折叠「高级：训练数据区间」 */
const trainCollapse = ref([]);

let chart;

function resolveHistoryRowClassName({ row }) {
  return row.taskId === selectedTaskId.value ? "interactive-table-row is-active-row" : "interactive-table-row";
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

async function loadPredictionHistory() {
  historyLoading.value = true;

  try {
    predictionHistory.value = await fetchPredictionTasks();
  } finally {
    historyLoading.value = false;
  }
}

function onHistorySelectionChange(rows) {
  historySelection.value = rows ?? [];
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
  selectedTaskId.value = null;
  if (chart) {
    chart.dispose();
    chart = null;
  }
}

async function afterDeleteRefresh(deletedIds) {
  const idSet = new Set(deletedIds.map((id) => Number(id)));
  const cur = selectedTaskId.value;
  const touchedCurrent = cur != null && idSet.has(Number(cur));
  await loadPredictionHistory();
  await nextTick();
  historyTableRef.value?.clearSelection?.();
  historySelection.value = [];
  if (touchedCurrent) {
    clearPredictionVisual();
  }
}

async function handleDeleteRow(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除任务「${row.taskNo}」吗？删除后不可恢复。`,
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
  } catch (err) {
    ElMessage.error(err?.message || "删除失败");
  }
}

async function handleBatchDelete() {
  const rows = historySelection.value;
  if (rows.length === 0) {
    return;
  }

  const ids = rows.map((r) => r.taskId);

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
  } catch (err) {
    ElMessage.error(err?.message || "删除失败");
  }
}

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

async function runPrediction() {
  if (!validateTrainRange()) {
    return;
  }

  loading.value = true;

  try {
    const payload = {
      warehouseId: form.warehouseId,
      metricCode: "temperature",
      targetType: form.targetType,
      forecastDays: form.forecastDays
    };

    if (trainRange.value?.length === 2 && trainRange.value[0] && trainRange.value[1]) {
      payload.trainStartTime = trainRange.value[0];
      payload.trainEndTime = trainRange.value[1];
    }

    prediction.value = await predictMetric(payload);
    selectedTaskId.value = prediction.value.taskId;
    await loadPredictionHistory();
    await nextTick();
    renderChart();
    await nextTick();
    chartAnchorRef.value?.scrollIntoView?.({ behavior: "smooth", block: "start" });
  } catch (err) {
    ElMessage.error(err?.message || "预测失败");
  } finally {
    loading.value = false;
  }
}

async function selectPredictionTask(item) {
  selectedTaskId.value = item.taskId;
  prediction.value = item;
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
  const el = cardInst?.$el ?? cardInst;
  if (!el || typeof el.scrollIntoView !== "function") {
    return;
  }
  el.scrollIntoView({ behavior: "smooth", block: "center" });
  el.classList.remove("prediction-summary-flash");
  void el.offsetWidth;
  el.classList.add("prediction-summary-flash");
  if (summaryFlashTimer != null) {
    clearTimeout(summaryFlashTimer);
  }
  summaryFlashTimer = window.setTimeout(() => {
    el.classList.remove("prediction-summary-flash");
    summaryFlashTimer = null;
  }, 1500);
}

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["实际值", "预测值"] },
    grid: { left: 32, right: 18, top: 34, bottom: 28 },
    xAxis: {
      type: "category",
      data: prediction.value.resultList.map((item) => formatDateTime(item.resultTime))
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "实际值",
        type: "line",
        smooth: true,
        data: prediction.value.resultList.map((item) => item.actualValue),
        lineStyle: { color: "#a855f7" },
        itemStyle: { color: "#a855f7" }
      },
      {
        name: "预测值",
        type: "line",
        smooth: true,
        data: prediction.value.resultList.map((item) => item.predictedValue),
        lineStyle: { color: "#ea580c" },
        itemStyle: { color: "#ea580c" },
        areaStyle: {
          color: "rgba(234, 88, 12, 0.12)"
        }
      }
    ]
  });

  requestAnimationFrame(() => {
    const el = chartRef.value;
    if (!el || !chart) {
      return;
    }

    chart.resize({
      width: el.clientWidth,
      height: el.clientHeight,
      animation: { duration: 0 }
    });
  });
}

onMounted(async () => {
  await loadWarehouses();
  await loadPredictionHistory();

  if (form.warehouseId) {
    await runPrediction();
  }
});

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
      <!-- md 起并排：仅写 xl 时 EP 默认 xl≥1920 才生效，常见笔记本会上下堆叠 -->
      <el-col :xs="24" :sm="24" :md="15" :lg="15" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">滚动预测参数</div>
          </template>

          <el-form inline>
            <el-form-item label="仓库">
              <el-select v-model="form.warehouseId" class="prediction-select-md">
                <el-option
                  v-for="item in warehouses"
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
              <el-button type="primary" :loading="loading" @click="runPrediction">
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
          <div>
            <div class="panel-title">实际值 / 预测值双线图</div>
            <div class="panel-subtitle">对应上方「任务摘要」中的当前任务；执行预测后会自动滚到此处</div>
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
                每次预测一条记录（原「预测结果列表」与「历史归档」合并为一表）。曲线仍对应当前在「操作」列切换的任务；可勾选多行后「批量删除」，或在操作列单条删除（均需确认）。
              </div>
            </div>
          </template>

          <div class="toolbar-row table-toolbar">
            <el-button type="danger" :disabled="batchDeleteDisabled" @click="handleBatchDelete">
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
              :data="historyPagination.pagedItems"
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
                  <el-button link type="danger" @click.stop="handleDeleteRow(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="historyPagination.currentPage"
              :page-size="historyPagination.pageSize"
              :page-sizes="historyPagination.pageSizes"
              :total="historyPagination.total"
              @current-change="historyPagination.handleCurrentChange"
              @size-change="historyPagination.handleSizeChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
