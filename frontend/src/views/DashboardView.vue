<script setup>
import { computed, onMounted, ref } from "vue";
import { fetchOverview } from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";
import { useIncrementalList } from "../composables/useIncrementalList";

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
const summaryPagination = useClientPagination(() => overview.value.latestGrainSummaries, {
  initialPageSize: 5
});
const healthPagination = useClientPagination(() => overview.value.warehouseHealthList, {
  initialPageSize: 5
});
const alertList = useIncrementalList(() => overview.value.latestAlerts, {
  step: 4,
  initialCount: 4
});
const visibleAlerts = computed(() => alertList.visibleItems.filter(Boolean));

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

function handleAlertScroll({ scrollTop, clientHeight, scrollHeight }) {
  if (scrollTop + clientHeight >= scrollHeight - 24) {
    alertList.loadMore();
  }
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

            <el-scrollbar
              class="alert-scrollbar"
              @scroll="handleAlertScroll"
            >
              <div class="stack-list">
                <div
                  v-for="item in visibleAlerts"
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

                <div v-if="alertList.hasMore" class="scroll-load-hint">
                  下滑继续加载更多预警
                </div>
              </div>
            </el-scrollbar>

            <el-empty
              v-if="!loading && overview.latestAlerts.length === 0"
              description="暂无近期预警"
            />
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">仓库运行健康度</div>
          </template>

          <el-table :data="healthPagination.pagedItems" stripe v-loading="loading">
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

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="healthPagination.currentPage"
              :page-size="healthPagination.pageSize"
              :page-sizes="healthPagination.pageSizes"
              :total="healthPagination.total"
              @current-change="healthPagination.handleCurrentChange"
              @size-change="healthPagination.handleSizeChange"
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

          <el-table :data="summaryPagination.pagedItems" stripe v-loading="loading">
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

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="summaryPagination.currentPage"
              :page-size="summaryPagination.pageSize"
              :page-sizes="summaryPagination.pageSizes"
              :total="summaryPagination.total"
              @current-change="summaryPagination.handleCurrentChange"
              @size-change="summaryPagination.handleSizeChange"
            />
          </div>
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
