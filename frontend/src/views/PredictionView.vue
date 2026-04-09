<script setup>
import * as echarts from "echarts";
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { fetchPredictionTasks, fetchWarehouses, predictMetric } from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";

const chartRef = ref();
const loading = ref(false);
const historyLoading = ref(false);
const warehouses = ref([]);
const predictionHistory = ref([]);
const selectedTaskId = ref(null);
const resultPagination = useClientPagination(() => prediction.value.resultList, {
  initialPageSize: 10
});
const historyPagination = useClientPagination(predictionHistory);
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

const form = reactive({
  warehouseId: "",
  targetType: "AVG_TEMP",
  forecastDays: 7
});

const targetOptions = [
  { value: "AVG_TEMP", label: "整仓平均温度" },
  { value: "LAYER_1_AVG", label: "第一层平均温度" },
  { value: "LAYER_2_AVG", label: "第二层平均温度" },
  { value: "LAYER_3_AVG", label: "第三层平均温度" },
  { value: "LAYER_4_AVG", label: "第四层平均温度" }
];

let chart;

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getTargetLabel(value) {
  return targetOptions.find((item) => item.value === value)?.label || value;
}

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

async function runPrediction() {
  loading.value = true;

  try {
    prediction.value = await predictMetric({
      warehouseId: form.warehouseId,
      metricCode: "temperature",
      targetType: form.targetType,
      forecastDays: form.forecastDays
    });
    selectedTaskId.value = prediction.value.taskId;
    await loadPredictionHistory();
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

async function selectPredictionTask(item) {
  selectedTaskId.value = item.taskId;
  prediction.value = item;
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
}

onMounted(async () => {
  await loadWarehouses();
  await loadPredictionHistory();

  if (form.warehouseId) {
    await runPrediction();
  }
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
            <div class="panel-title">滚动预测参数</div>
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

            <el-form-item label="预测对象">
              <el-select v-model="form.targetType" style="width: 180px">
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
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">任务摘要</div>
          </template>

          <div class="detail-grid">
            <div><strong>任务号：</strong>{{ prediction.taskNo }}</div>
            <div><strong>仓库：</strong>{{ prediction.warehouseName || "-" }}</div>
            <div><strong>预测对象：</strong>{{ getTargetLabel(prediction.targetType) }}</div>
            <div><strong>算法：</strong>{{ prediction.algorithmName }}</div>
            <div><strong>风险等级：</strong>{{ prediction.riskLevel }}</div>
            <div><strong>预测天数：</strong>{{ prediction.forecastDays }}</div>
            <div><strong>训练区间：</strong>{{ formatDateTime(prediction.trainStartTime) }} ~ {{ formatDateTime(prediction.trainEndTime) }}</div>
            <div><strong>预测区间：</strong>{{ formatDateTime(prediction.forecastStartTime) }} ~ {{ formatDateTime(prediction.forecastEndTime) }}</div>
            <div><strong>执行时间：</strong>{{ formatDateTime(prediction.requestedAt) }}</div>
            <div><strong>任务摘要：</strong>{{ prediction.summary || "预测执行完成" }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">实际值 / 预测值双线图</div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">预测结果列表</div>
          </template>

          <el-table :data="resultPagination.pagedItems" stripe v-loading="loading">
            <el-table-column prop="phaseType" label="阶段" width="90" />
            <el-table-column prop="stepIndex" label="序号" width="90" />
            <el-table-column label="时间">
              <template #default="{ row }">
                {{ formatDateTime(row.resultTime) }}
              </template>
            </el-table-column>
            <el-table-column label="实际值">
              <template #default="{ row }">
                {{ row.actualValue ?? "--" }}
              </template>
            </el-table-column>
            <el-table-column label="预测值">
              <template #default="{ row }">
                {{ row.predictedValue ?? "--" }}
              </template>
            </el-table-column>
            <el-table-column prop="warningLevel" label="预警等级" width="110" />
            <el-table-column prop="warningMessage" label="预警说明" min-width="180" />
          </el-table>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="resultPagination.currentPage"
              :page-size="resultPagination.pageSize"
              :page-sizes="resultPagination.pageSizes"
              :total="resultPagination.total"
              @current-change="resultPagination.handleCurrentChange"
              @size-change="resultPagination.handleSizeChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">历史归档记录</div>
          </template>

          <el-table
            :data="historyPagination.pagedItems"
            stripe
            v-loading="historyLoading"
            :row-class-name="resolveHistoryRowClassName"
            @row-click="selectPredictionTask"
          >
            <el-table-column prop="taskNo" label="任务号" min-width="160" />
            <el-table-column label="预测对象" min-width="130">
              <template #default="{ row }">
                {{ getTargetLabel(row.targetType) }}
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" min-width="120" />
            <el-table-column prop="riskLevel" label="风险等级" width="110" />
            <el-table-column prop="forecastDays" label="预测天数" width="110" />
            <el-table-column label="执行时间" min-width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.requestedAt) }}
              </template>
            </el-table-column>
          </el-table>

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
