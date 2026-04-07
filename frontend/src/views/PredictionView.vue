<script setup>
import * as echarts from "echarts";
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import {
  fetchMetricOptions,
  fetchPredictionTasks,
  fetchWarehouses,
  predictMetric
} from "../api/grain";

const DEFAULT_METRIC_CODE = "temperature";

const chartRef = ref();
const loading = ref(false);
const historyLoading = ref(false);
const warehouses = ref([]);
const metricOptions = ref([]);
const predictionHistory = ref([]);
const selectedTaskId = ref(null);
const prediction = ref({
  taskNo: "",
  metricCode: DEFAULT_METRIC_CODE,
  metricName: "温度",
  unit: "",
  algorithmName: "",
  riskLevel: "NORMAL",
  requestedAt: "",
  resultList: []
});
let chart;

const form = reactive({
  warehouseId: "",
  metricCode: DEFAULT_METRIC_CODE,
  futureSteps: 6
});

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getInitialMetricCode() {
  return metricOptions.value[0]?.value || DEFAULT_METRIC_CODE;
}

async function loadMetricOptions() {
  metricOptions.value = await fetchMetricOptions();
  form.metricCode = getInitialMetricCode();
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

// 加载真实预测归档列表，供右侧历史记录区展示。
async function loadPredictionHistory() {
  historyLoading.value = true;

  try {
    predictionHistory.value = await fetchPredictionTasks();
  } finally {
    historyLoading.value = false;
  }
}

// 执行新预测后，刷新归档列表并回显最新结果。
async function runPrediction() {
  loading.value = true;

  try {
    prediction.value = await predictMetric(form);
    selectedTaskId.value = prediction.value.taskId;
    await loadPredictionHistory();
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

// 右侧点击历史任务时，回显对应图表与结果列表。
async function selectPredictionTask(item) {
  selectedTaskId.value = item.taskId;
  prediction.value = item;
  console.info("[Prediction] 选中历史归档记录", {
    taskId: item.taskId,
    taskNo: item.taskNo,
    resultCount: item.resultList.length
  });
  await nextTick();
  renderChart();
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
      data: prediction.value.resultList.map((item) => formatDateTime(item.predictedTime))
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
}

onMounted(async () => {
  await loadMetricOptions();
  await loadWarehouses();
  await loadPredictionHistory();
  await runPrediction();
});

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose();
  }
});
</script>

<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">预测参数</div>
          </template>

          <el-form inline>
            <el-form-item label="仓库">
              <el-select v-model="form.warehouseId" style="width: 180px">
                <el-option
                  v-for="item in warehouses"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="指标">
              <el-select v-model="form.metricCode" style="width: 160px">
                <el-option
                  v-for="item in metricOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="未来步数">
              <el-input-number v-model="form.futureSteps" :min="1" :max="24" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="runPrediction">
                执行预测
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">任务摘要</div>
          </template>

          <div class="detail-grid">
            <div><strong>任务号：</strong>{{ prediction.taskNo }}</div>
            <div><strong>指标：</strong>{{ prediction.metricName }}</div>
            <div><strong>单位：</strong>{{ prediction.unit || "-" }}</div>
            <div><strong>算法：</strong>{{ prediction.algorithmName }}</div>
            <div><strong>风险等级：</strong>{{ prediction.riskLevel }}</div>
            <div><strong>执行时间：</strong>{{ formatDateTime(prediction.requestedAt) }}</div>
            <div><strong>结果点数：</strong>{{ prediction.resultList.length }}</div>
            <div><strong>任务摘要：</strong>{{ prediction.summary || "预测执行完成" }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">历史指标与预测曲线</div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">预测结果列表</div>
          </template>

          <el-table :data="prediction.resultList" stripe v-loading="loading">
            <el-table-column prop="stepIndex" label="步数" width="90" />
            <el-table-column label="预测时间">
              <template #default="{ row }">
                {{ formatDateTime(row.predictedTime) }}
              </template>
            </el-table-column>
            <el-table-column label="实际值">
              <template #default="{ row }">
                {{ row.actualValue ?? "--" }}
              </template>
            </el-table-column>
            <el-table-column prop="predictedValue" label="预测值" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">历史归档记录</div>
          </template>

          <div class="stack-list" v-loading="historyLoading">
            <div
              v-for="item in predictionHistory"
              :key="item.taskId"
              class="list-card prediction-history-card"
              :class="{ 'is-active': item.taskId === selectedTaskId }"
              @click="selectPredictionTask(item)"
            >
              <div class="list-card-header">
                <strong>{{ item.taskNo }}</strong>
                <el-tag type="info">{{ item.metricName }}</el-tag>
              </div>
              <div class="list-card-desc">指标：{{ item.metricName }}</div>
              <div class="list-card-desc">风险等级：{{ item.riskLevel }}</div>
              <div class="list-card-desc">{{ formatDateTime(item.requestedAt) }}</div>
              <div class="list-card-desc">{{ item.summary }}</div>
              <div class="list-card-desc">结果点数：{{ item.resultList.length }}</div>
            </div>
            <el-empty v-if="!historyLoading && predictionHistory.length === 0" description="暂无预测归档记录" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
