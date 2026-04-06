<script setup>
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import {
  createSensorData,
  fetchSensorData,
  fetchWarehouses,
  getMetricOptions
} from "../api/grain";

const chartRef = ref();
const loading = ref(false);
const warehouses = ref([]);
const rows = ref([]);
const metricOptions = getMetricOptions();
let chart;

const filters = reactive({
  warehouseId: "",
  metricCode: "temperature"
});

const form = reactive({
  warehouseId: "",
  metricCode: "temperature",
  metricValue: 24.8
});

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

async function loadData() {
  loading.value = true;

  try {
    rows.value = await fetchSensorData(filters);
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

async function submit() {
  await createSensorData(form);
  ElMessage.success("环境数据已写入演示接口");
  await loadData();
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
    grid: { left: 32, right: 18, top: 30, bottom: 28 },
    xAxis: {
      type: "category",
      data: rows.value.map((item) => formatDateTime(item.collectedAt))
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: "环境值",
        type: "line",
        smooth: true,
        data: rows.value.map((item) => item.metricValue),
        lineStyle: {
          color: "#0b7a75"
        },
        itemStyle: {
          color: "#0b7a75"
        },
        areaStyle: {
          color: "rgba(11, 122, 117, 0.12)"
        }
      }
    ]
  });
}

onMounted(async () => {
  await loadWarehouses();
  await loadData();
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
      <el-col :xs="24" :xl="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">查询条件</div>
          </template>

          <el-form inline>
            <el-form-item label="仓库">
              <el-select v-model="filters.warehouseId" clearable style="width: 180px">
                <el-option
                  v-for="item in warehouses"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="指标">
              <el-select v-model="filters.metricCode" style="width: 160px">
                <el-option
                  v-for="item in metricOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="loadData">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">手工录入</div>
          </template>

          <el-form label-position="top" class="form-grid-3">
            <el-form-item label="仓库">
              <el-select v-model="form.warehouseId">
                <el-option
                  v-for="item in warehouses"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="指标">
              <el-select v-model="form.metricCode">
                <el-option
                  v-for="item in metricOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="采样值">
              <el-input-number v-model="form.metricValue" :step="0.1" />
            </el-form-item>
          </el-form>

          <el-button type="primary" @click="submit">提交数据</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">环境趋势图</div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">环境数据记录</div>
      </template>

      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="metricName" label="指标" />
        <el-table-column prop="metricValue" label="数值" />
        <el-table-column label="采集时间">
          <template #default="{ row }">
            {{ formatDateTime(row.collectedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" />
        <el-table-column prop="qualityFlag" label="质量标记" />
      </el-table>
    </el-card>
  </div>
</template>
