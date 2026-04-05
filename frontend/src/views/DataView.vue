<script setup>
import * as echarts from "echarts";
import { nextTick, onMounted, reactive, ref } from "vue";
import { createSensorData, fetchSensorData, fetchWarehouses } from "../api/grain";

const chartRef = ref();
let chart;

const warehouses = ref([]);
const rows = ref([]);
const filters = reactive({
  warehouseId: "",
  metricType: "temperature"
});
const form = reactive({
  warehouseId: 1,
  metricType: "temperature",
  metricValue: 24.8
});

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();
}

async function loadData() {
  rows.value = await fetchSensorData(filters);
  await nextTick();
  renderChart();
}

async function submit() {
  await createSensorData(form);
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
    xAxis: {
      type: "category",
      data: rows.value.map((item) => item.collectedAt.replace("T", " "))
    },
    yAxis: { type: "value" },
    series: [
      {
        type: "line",
        smooth: true,
        data: rows.value.map((item) => item.metricValue)
      }
    ]
  });
}

onMounted(async () => {
  await loadWarehouses();
  await loadData();
});
</script>

<template>
  <div class="stack">
    <div class="panel">
      <div class="section-title">录入环境数据</div>
      <div class="form-grid three-columns">
        <label>
          <span>仓库</span>
          <select v-model="form.warehouseId" class="input">
            <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label>
          <span>指标</span>
          <select v-model="form.metricType" class="input">
            <option value="temperature">温度</option>
            <option value="humidity">湿度</option>
          </select>
        </label>
        <label>
          <span>值</span>
          <input v-model="form.metricValue" class="input" type="number" step="0.1" />
        </label>
      </div>
      <button class="primary-btn" @click="submit">提交数据</button>
    </div>

    <div class="panel">
      <div class="section-title">历史数据查询</div>
      <div class="form-grid three-columns">
        <label>
          <span>仓库</span>
          <select v-model="filters.warehouseId" class="input">
            <option value="">全部</option>
            <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label>
          <span>指标</span>
          <select v-model="filters.metricType" class="input">
            <option value="temperature">温度</option>
            <option value="humidity">湿度</option>
          </select>
        </label>
        <div class="filter-actions">
          <button class="primary-btn" @click="loadData">查询</button>
        </div>
      </div>
      <div ref="chartRef" class="chart-box"></div>
      <table class="table">
        <thead>
          <tr>
            <th>仓库</th>
            <th>指标</th>
            <th>值</th>
            <th>采集时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rows" :key="item.id">
            <td>{{ item.warehouseName }}</td>
            <td>{{ item.metricType }}</td>
            <td>{{ item.metricValue }}</td>
            <td>{{ item.collectedAt.replace("T", " ") }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
