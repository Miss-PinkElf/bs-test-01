<script setup>
import { Search } from "@element-plus/icons-vue";
import { computed, onMounted, reactive, ref, watch } from "vue";
import {
  fetchDashboardAlertsPage,
  fetchDashboardGrainSummariesPage,
  fetchDashboardWarehouseHealthPage,
  fetchOverview
} from "../api/grain";

const overviewLoading = ref(false);
const overviewError = ref("");
const overview = ref({
  warehouseCount: 0,
  grainSummaryCount: 0,
  realAlertCount: 0,
  predictionAlertCount: 0,
  archivedPredictionCount: 0
});

function createPageSection(pageSize = 5) {
  return reactive({
    list: [],
    keyword: "",
    pageNum: 1,
    pageSize,
    total: 0,
    loading: false,
    error: ""
  });
}

const alertsSection = createPageSection(5);
const healthSection = createPageSection(5);
const summarySection = createPageSection(5);

watch(
  () => alertsSection.keyword,
  async () => {
    alertsSection.pageNum = 1;
    await loadAlertsPage();
  }
);

watch(
  () => healthSection.keyword,
  async () => {
    healthSection.pageNum = 1;
    await loadHealthPage();
  }
);

watch(
  () => summarySection.keyword,
  async () => {
    summarySection.pageNum = 1;
    await loadSummaryPage();
  }
);

const cards = computed(() => [
  { label: "在线粮仓", value: overview.value.warehouseCount, note: "当前纳入平台管理的仓库数量" },
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
  overviewLoading.value = true;
  overviewError.value = "";

  try {
    overview.value = await fetchOverview();
  } catch (error) {
    overviewError.value = error?.message || "概览加载失败，请重试";
  } finally {
    overviewLoading.value = false;
  }
}

async function loadSection(section, fetcher, fallbackMessage) {
  section.loading = true;
  section.error = "";

  try {
    const page = await fetcher({
      keyword: section.keyword.trim() || undefined,
      pageNum: section.pageNum,
      pageSize: section.pageSize
    });
    section.list = page.list;
    section.pageNum = page.pageNum;
    section.pageSize = page.pageSize;
    section.total = page.total;
  } catch (error) {
    section.error = error?.message || fallbackMessage;
  } finally {
    section.loading = false;
  }
}

async function loadAlertsPage() {
  await loadSection(alertsSection, fetchDashboardAlertsPage, "近期预警加载失败，请重试");
}

async function loadHealthPage() {
  await loadSection(healthSection, fetchDashboardWarehouseHealthPage, "仓库运行健康度加载失败，请重试");
}

async function loadSummaryPage() {
  await loadSection(summarySection, fetchDashboardGrainSummariesPage, "最新粮温汇总加载失败，请重试");
}

async function handleAlertsPageChange(pageNum) {
  alertsSection.pageNum = pageNum;
  await loadAlertsPage();
}

async function handleAlertsPageSizeChange(pageSize) {
  alertsSection.pageSize = pageSize;
  alertsSection.pageNum = 1;
  await loadAlertsPage();
}

async function handleHealthPageChange(pageNum) {
  healthSection.pageNum = pageNum;
  await loadHealthPage();
}

async function handleHealthPageSizeChange(pageSize) {
  healthSection.pageSize = pageSize;
  healthSection.pageNum = 1;
  await loadHealthPage();
}

async function handleSummaryPageChange(pageNum) {
  summarySection.pageNum = pageNum;
  await loadSummaryPage();
}

async function handleSummaryPageSizeChange(pageSize) {
  summarySection.pageSize = pageSize;
  summarySection.pageNum = 1;
  await loadSummaryPage();
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadAlertsPage(), loadHealthPage(), loadSummaryPage()]);
});
</script>

<template>
  <div class="page-stack">
    <el-alert
      v-if="overviewError"
      type="error"
      :closable="false"
      :title="overviewError"
      description="顶部概览来自真实接口，请检查后台服务状态后重试。"
    />

    <div class="metrics-grid" v-loading="overviewLoading">
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

          <div class="toolbar-row table-toolbar">
            <el-input
              v-model="alertsSection.keyword"
              class="table-search-input"
              clearable
              placeholder="搜索标题、仓库、说明、等级"
              :prefix-icon="Search"
            />
          </div>

          <el-alert
            v-if="alertsSection.error"
            type="error"
            :closable="false"
            :title="alertsSection.error"
            description="列表加载失败，请重试；若仍失败，请检查接口状态或缩小关键词后重新查询。"
          />

          <div v-else class="stack-list" v-loading="alertsSection.loading">
            <div
              v-for="item in alertsSection.list"
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

            <el-empty
              v-if="!alertsSection.loading && alertsSection.list.length === 0"
              description="暂无匹配结果"
            />
          </div>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="alertsSection.pageNum"
              :page-size="alertsSection.pageSize"
              :page-sizes="[5, 10, 20]"
              :total="alertsSection.total"
              @current-change="handleAlertsPageChange"
              @size-change="handleAlertsPageSizeChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">仓库运行健康度</div>
          </template>

          <div class="toolbar-row table-toolbar">
            <el-input
              v-model="healthSection.keyword"
              class="table-search-input"
              clearable
              placeholder="搜索仓库、健康分、风险等级"
              :prefix-icon="Search"
            />
          </div>

          <el-alert
            v-if="healthSection.error"
            type="error"
            :closable="false"
            :title="healthSection.error"
            description="列表加载失败，请重试；若仍失败，请检查接口状态或缩小关键词后重新查询。"
          />

          <template v-else>
            <el-table :data="healthSection.list" stripe v-loading="healthSection.loading">
              <el-table-column prop="warehouseName" label="仓库" min-width="120" />
              <el-table-column prop="healthScore" label="健康分" width="100" />
              <el-table-column prop="riskLevel" label="综合风险" width="110">
                <template #default="{ row }">
                  <el-tag :type="resolveLevelTagType(row.riskLevel)">{{ row.riskLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="实时 / 预测" min-width="160">
                <template #default="{ row }">
                  {{ row.realWarningLevel }} / {{ row.predictionWarningLevel }}
                </template>
              </el-table-column>
              <el-table-column label="均温 / 峰值" min-width="180">
                <template #default="{ row }">
                  {{ formatTemperature(row.latestAvgTemp) }} / {{ formatTemperature(row.latestForecastValue) }}
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="!healthSection.loading && healthSection.list.length === 0"
              description="暂无匹配结果"
            />
          </template>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="healthSection.pageNum"
              :page-size="healthSection.pageSize"
              :page-sizes="[5, 10, 20]"
              :total="healthSection.total"
              @current-change="handleHealthPageChange"
              @size-change="handleHealthPageSizeChange"
            />
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

          <div class="toolbar-row table-toolbar">
            <el-input
              v-model="summarySection.keyword"
              class="table-search-input"
              clearable
              placeholder="搜索仓库、均温、预警等级、时间"
              :prefix-icon="Search"
            />
          </div>

          <el-alert
            v-if="summarySection.error"
            type="error"
            :closable="false"
            :title="summarySection.error"
            description="列表加载失败，请重试；若仍失败，请检查接口状态或缩小关键词后重新查询。"
          />

          <template v-else>
            <el-table :data="summarySection.list" stripe v-loading="summarySection.loading">
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

            <el-empty
              v-if="!summarySection.loading && summarySection.list.length === 0"
              description="暂无匹配结果"
            />
          </template>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="summarySection.pageNum"
              :page-size="summarySection.pageSize"
              :page-sizes="[5, 10, 20]"
              :total="summarySection.total"
              @current-change="handleSummaryPageChange"
              @size-change="handleSummaryPageSizeChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">数据说明</div>
          </template>

          <div class="compact-lines">
            <div>顶部概览、近期预警、仓库健康度与最新粮温汇总均直接来自后端真实接口。</div>
            <div>某一模块请求失败时，仅该模块显示错误态，其余模块继续保留可见。</div>
            <div>搜索与翻页都会回源，不再以前端内存列表作为分页主链。</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
