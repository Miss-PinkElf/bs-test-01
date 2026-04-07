<script setup>
import { computed, onMounted, ref } from "vue";
import { fetchOverview } from "../api/grain";

const loading = ref(false);
const overview = ref({
  warehouseCount: 0,
  todayDataCount: 0,
  alertCount: 0,
  archivedPredictionCount: 0,
  latestAlerts: [],
  recentSensorRecords: [],
  warehouseHealthList: []
});

const cards = computed(() => [
  { label: "在线粮仓", value: overview.value.warehouseCount, note: "当前纳入演示范围的仓库数量" },
  { label: "今日采样", value: overview.value.todayDataCount, note: "可用于查询、图表和预测的数据量" },
  { label: "重点预警", value: overview.value.alertCount, note: "需要优先关注的预警数量" },
  {
    label: "预测归档",
    value: overview.value.archivedPredictionCount,
    note: "已执行并形成留痕的预测任务"
  }
]);

async function loadOverview() {
  loading.value = true;

  try {
    overview.value = await fetchOverview();
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<template>
  <div class="page-stack">
    <div class="metrics-grid">
      <el-card
        v-for="item in cards"
        :key="item.label"
        class="metric-card"
        shadow="never"
      >
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-note">{{ item.note }}</div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">近期预警</div>
          </template>

          <el-skeleton :loading="loading" animated>
            <template #template>
              <el-skeleton-item
                v-for="idx in 3"
                :key="idx"
                variant="text"
                style="width: 100%; height: 22px; margin-bottom: 16px"
              />
            </template>

            <div class="stack-list">
              <div
                v-for="item in overview.latestAlerts"
                :key="item.title"
                class="list-card"
              >
                <div class="list-card-header">
                  <strong>{{ item.title }}</strong>
                  <el-tag type="warning">{{ item.level }}</el-tag>
                </div>
                <div class="list-card-desc">{{ item.description }}</div>
              </div>
            </div>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">仓库运行健康度</div>
          </template>

          <div class="stack-list">
            <div
              v-for="item in overview.warehouseHealthList"
              :key="item.warehouseId"
              class="score-row"
            >
              <div>
                <div class="score-name">{{ item.warehouseName }}</div>
                <div class="score-note">风险等级：{{ item.riskLevel }}</div>
              </div>
              <div class="score-badge">{{ item.healthScore }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">最近采样记录</div>
          </template>

          <el-table :data="overview.recentSensorRecords" stripe>
            <el-table-column prop="warehouseName" label="仓库" />
            <el-table-column prop="metricName" label="指标" />
            <el-table-column prop="metricValue" label="数值" />
            <el-table-column prop="collectedAt" label="采集时间" />
            <el-table-column prop="qualityFlag" label="质量标记" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">正式开发提醒</div>
          </template>

          <div class="compact-lines">
            <div>当前仪表盘已切到真实首页概览、近期预警、仓库健康度和最近采样记录。</div>
            <div>后续可继续补趋势图、更多聚合统计和大屏真实化展示。</div>
            <div>正式前端已切到 Pinia + Element Plus + Axios 路线。</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
