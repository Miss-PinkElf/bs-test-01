<script setup>
import { onMounted, ref } from "vue";
import { fetchOverview } from "../api/grain";

const overview = ref({
  warehouseCount: 0,
  todayDataCount: 0,
  alertCount: 0,
  latestAlerts: []
});

onMounted(async () => {
  overview.value = await fetchOverview();
});
</script>

<template>
  <div class="stack">
    <div class="card-grid">
      <div class="stat-card">
        <div class="stat-label">粮仓数量</div>
        <div class="stat-value">{{ overview.warehouseCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日数据量</div>
        <div class="stat-value">{{ overview.todayDataCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">预警数量</div>
        <div class="stat-value">{{ overview.alertCount }}</div>
      </div>
    </div>

    <div class="panel">
      <div class="section-title">最新预警</div>
      <ul class="simple-list">
        <li v-for="item in overview.latestAlerts" :key="item">{{ item }}</li>
      </ul>
    </div>
  </div>
</template>
