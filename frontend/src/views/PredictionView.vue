<script setup>
import * as echarts from "echarts";
import { nextTick, onMounted, reactive, ref } from "vue";
import { fetchWarehouses, predictTemperature } from "../api/grain";

const warehouses = ref([]);
const chartRef = ref();
const rows = ref([]);
let chart;

const form = reactive({
  warehouseId: 1,
  metricType: "temperature",
  futureSteps: 6
});

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();
}

async function runPrediction() {
  rows.value = await predictTemperature(form);
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
    xAxis: {
      type: "category",
      data: rows.value.map((item) => item.time.replace("T", " "))
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "实际值",
        type: "line",
        smooth: true,
        data: rows.value.map((item) => item.actualValue)
      },
      {
        name: "预测值",
        type: "line",
        smooth: true,
        data: rows.value.map((item) => item.predictedValue)
      }
    ]
  });
}

onMounted(async () => {
  await loadWarehouses();
  await runPrediction();
});
</script>

<template>
  <div class="stack">
    <div class="panel">
      <div class="section-title">温度预测</div>
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
          </select>
        </label>
        <label>
          <span>未来步数</span>
          <input v-model="form.futureSteps" class="input" type="number" min="1" max="24" />
        </label>
      </div>
      <button class="primary-btn" @click="runPrediction">执行预测</button>
    </div>

    <div class="panel">
      <div ref="chartRef" class="chart-box"></div>
    </div>
  </div>
</template>
