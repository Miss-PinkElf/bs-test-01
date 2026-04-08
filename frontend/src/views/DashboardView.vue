<script setup>
import { computed, onMounted, ref } from "vue";
import { fetchOverview } from "../api/grain";

const loading = ref(false);
const overview = ref({
  warehouseCount: 0,
  grainSummaryCount: 0,
  realAlertCount: 0,
  predictionAlertCount: 0,
  archivedPredictionCount: 0,
  latestAlerts: [],
  latestGrainSummaries: [],
  warehouseHealthList: []
});

const cards = computed(() => [
  { label: "在线粮仓", value: overview.value.warehouseCount, note: "当前纳入演示范围的仓库数量" },
  { label: "粮温汇总", value: overview.value.grainSummaryCount, note: "已归档的粮温主线汇总记录数" },
  { label: "真实预警", value: overview.value.realAlertCount, note: "各仓最新粮温汇总中的实时预警数量" },
  { label: "预测预警", value: overview.value.predictionAlertCount, note: "各仓最新预测任务中的未来预警点数量" },
  {
    label: "预测归档",
    value: overview.value.archivedPredictionCount,
    note: "已执行并形成留痕的预测任务"
  }
]);

function resolveLevelTagType(level) {
  if (level === "WARNING") {
    return "danger";
  }

  if (level === "ATTENTION") {
    return "warning";
  }

  if (level === "MAINTENANCE") {
    return "info";
  }

  return "success";
}

function resolveSourceTagType(sourceType) {
  return sourceType === "PREDICTION" ? "warning" : "danger";
}

function formatSourceLabel(sourceType) {
  return sourceType === "PREDICTION" ? "预测预警" : "真实预警";
}

function formatTemperature(value) {
  return value == null ? "-" : `${Number(value).toFixed(2)}°C`;
}

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
                :key="`${item.sourceType}-${item.warehouseName}-${item.eventTime}-${item.title}`"
                class="list-card"
              >
                <div class="list-card-header">
                  <strong>{{ item.title }}</strong>
                  <div class="tag-row">
                    <el-tag :type="resolveSourceTagType(item.sourceType)">{{ formatSourceLabel(item.sourceType) }}</el-tag>
                    <el-tag :type="resolveLevelTagType(item.level)">{{ item.level }}</el-tag>
                  </div>
                </div>
                <div class="list-card-meta">{{ item.warehouseName }} · {{ item.eventTime || "时间待补充" }}</div>
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
                <div class="score-note">综合风险：{{ item.riskLevel }}</div>
                <div class="score-note">实时：{{ item.realWarningLevel }} / 预测：{{ item.predictionWarningLevel }}</div>
                <div class="score-note">
                  最新均温：{{ formatTemperature(item.latestAvgTemp) }} / 预测峰值：{{ formatTemperature(item.latestForecastValue) }}
                </div>
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
            <div class="panel-title">最新粮温汇总</div>
          </template>

          <el-table :data="overview.latestGrainSummaries" stripe>
            <el-table-column prop="warehouseName" label="仓库" />
            <el-table-column prop="avgTemp" label="均温">
              <template #default="{ row }">
                {{ formatTemperature(row.avgTemp) }}
              </template>
            </el-table-column>
            <el-table-column prop="maxTemp" label="最高温">
              <template #default="{ row }">
                {{ formatTemperature(row.maxTemp) }}
              </template>
            </el-table-column>
            <el-table-column prop="warningLevel" label="预警等级">
              <template #default="{ row }">
                <el-tag :type="resolveLevelTagType(row.warningLevel)">{{ row.warningLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="collectedAt" label="汇总时间" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">正式开发提醒</div>
          </template>

          <div class="compact-lines">
            <div>当前首页优先展示最新真实预警、预测预警、仓库风险与粮温汇总。</div>
            <div>真实预警来自 grain_temp_summary，预测预警来自 prediction_result。</div>
            <div>下一轮再继续细化固定 XLS 导入模板与回归验收清单。</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
