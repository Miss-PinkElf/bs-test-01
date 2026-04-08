<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { fetchOverview } from "../api/grain";
import { mockScreenAlerts, mockScreenMetrics } from "../mock/platform";

const router = useRouter();
const metrics = ref(mockScreenMetrics);
const alerts = ref(mockScreenAlerts);

onMounted(async () => {
  try {
    const overview = await fetchOverview();

    metrics.value = [
      {
        ...mockScreenMetrics[0],
        label: "在线粮仓",
        value: String(overview.warehouseCount).padStart(2, "0"),
        note: "当前纳入演示范围的仓库数量"
      },
      {
        ...mockScreenMetrics[1],
        label: "粮温汇总",
        value: String(overview.grainSummaryCount || 0),
        note: "粮温主线已归档的汇总记录数"
      },
      {
        ...mockScreenMetrics[2],
        label: "重点预警",
        value: String((overview.realAlertCount || 0) + (overview.predictionAlertCount || 0)).padStart(2, "0"),
        note: "真实预警与预测预警合计数量"
      },
      {
        ...mockScreenMetrics[3],
        label: "已归档预测",
        value: String(overview.archivedPredictionCount || 0),
        note: "已留痕的预测任务数量"
      }
    ];

    if (overview.latestAlerts.length > 0) {
      alerts.value = overview.latestAlerts.map((item) => item.title);
    }
  } catch (error) {
    console.warn(error);
  }
});
</script>

<template>
  <div class="screen-page">
    <div class="screen-topbar">
      <div>
        <div class="screen-title">粮仓环境数据预测管理平台</div>
        <div class="screen-subtitle">答辩展示大屏</div>
      </div>
      <div class="screen-actions">
        <el-button plain @click="router.push('/dashboard')">进入后台</el-button>
        <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
      </div>
    </div>

    <div class="screen-metrics">
      <div
        v-for="item in metrics"
        :key="item.key"
        class="screen-metric-card"
      >
        <div class="screen-metric-label">{{ item.label }}</div>
        <div class="screen-metric-value">{{ item.value }}</div>
        <div class="screen-metric-note">{{ item.note }}</div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="screen-panel" shadow="never">
          <template #header>
            <div class="panel-title">展示说明</div>
          </template>

          <div class="compact-lines">
            <div>当前大屏用于答辩演示，不额外扩展机器人、硬件接入或复杂算法。</div>
            <div>正式系统主流程聚焦粮温导入、真实预警、温度预测和结果归档。</div>
            <div>首页和大屏都已优先切到真实预警与预测预警口径。</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="screen-panel" shadow="never">
          <template #header>
            <div class="panel-title">重点预警</div>
          </template>

          <div class="stack-list">
            <div
              v-for="item in alerts"
              :key="item"
              class="screen-alert-item"
            >
              {{ item }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
