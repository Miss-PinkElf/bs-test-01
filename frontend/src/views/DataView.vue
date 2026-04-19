<script setup>
import { Search } from "@element-plus/icons-vue";
import * as echarts from "echarts";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import {
  createGrainTempRecord,
  createSensorData,
  deleteGrainTempRecord,
  deleteSensorData,
  downloadGrainTempTemplate,
  downloadSensorTemplate,
  fetchGrainTempRecordFilterOptions,
  fetchGrainTempRecords,
  fetchGrainTempSummaries,
  fetchGrainTempSummaryPage,
  fetchMetricOptions,
  fetchSensorData,
  fetchSensorTrend,
  fetchWarehouses,
  importGrainTemp,
  importSensorData,
  updateGrainTempRecord,
  updateSensorData
} from "../api/grain";
import { useAuthStore } from "../stores/auth";

const GRAIN_SUMMARY_TARGET_OPTIONS = [
  { value: "AVG_TEMP", label: "整仓均温" },
  { value: "LAYER_1_AVG", label: "一层均温" },
  { value: "LAYER_2_AVG", label: "二层均温" },
  { value: "LAYER_3_AVG", label: "三层均温" },
  { value: "LAYER_4_AVG", label: "四层均温" }
];
const GRAIN_WARNING_LEVEL_OPTIONS = ["NORMAL", "ATTENTION", "WARNING"];
const DAY_IN_MS = 24 * 60 * 60 * 1000;
const DEFAULT_GRAIN_SUMMARY_DAYS = 30;
const MAX_CHART_AXIS_LABELS = 8;

const chartRef = ref();
const chartLoading = ref(false);
const summaryLoading = ref(false);
const recordLoading = ref(false);
const importLoadingCount = ref(0);
const importLoading = computed(() => importLoadingCount.value > 0);
const dialogVisible = ref(false);
const mode = ref("grain");
const warehouses = ref([]);
const rows = ref([]);
const grainSummarySeriesRows = ref([]);
const grainSummaryTableRows = ref([]);
const metricOptions = ref([]);
const editingId = ref(null);
const envTrendRows = ref([]);
const grainSummaryRange = ref(null);
const grainSummaryTarget = ref("AVG_TEMP");
const grainSummaryDefaultInitialized = ref(false);
const grainRecordKeyword = ref("");
const envRecordKeyword = ref("");
const grainFilterOptions = ref({ zoneCodes: [], layerNos: [], pointNos: [] });
const grainRecordCollectedRange = ref(null);
const grainSummaryFilters = reactive({ keyword: "", warningLevel: "", tempMin: null, tempMax: null });
const grainRecordFilters = reactive({ zoneCode: "", layerNo: null, pointNo: null, tempMin: null, tempMax: null });
const grainSummaryPageState = reactive({ pageNum: 1, pageSize: 10, total: 0 });
const grainRecordPageState = reactive({ pageNum: 1, pageSize: 10, total: 0 });
const envRecordPageState = reactive({ pageNum: 1, pageSize: 10, total: 0 });

let chart;
let recordKeywordTimer;

const authStore = useAuthStore();
const filters = reactive({ warehouseId: "", metricCode: "humidity" });
const grainForm = reactive({ warehouseId: "", zoneCode: "A", layerNo: 1, pointNo: 1, collectedAt: "", temperatureValue: 24.5, probeCode: "", remark: "" });
const envForm = reactive({ warehouseId: "", metricCode: "humidity", metricValue: 58.2, collectedAt: "" });

const envMetricOptions = computed(() => metricOptions.value.filter((item) => item.value !== "temperature"));
const isWarehouseManager = computed(() => authStore.isWarehouseManager);
const isViewer = computed(() => authStore.isViewer);
const managedWarehouseId = computed(() => authStore.managedWarehouseId ?? null);
const canWriteData = computed(() => !isViewer.value);
const visibleWarehouses = computed(() => {
  if (!isWarehouseManager.value || managedWarehouseId.value == null) {
    return warehouses.value;
  }

  return warehouses.value.filter((item) => item.id === managedWarehouseId.value);
});
const isEditing = computed(() => editingId.value !== null);
const dialogTitle = computed(() => (mode.value === "grain"
  ? (isEditing.value ? "编辑粮温原始记录" : "新增粮温原始记录")
  : (isEditing.value ? "编辑普通环境数据" : "手工录入普通环境数据")));
const latestGrainSummary = computed(() => grainSummarySeriesRows.value.at(-1) || null);
const grainSummaryTargetLabel = computed(() => GRAIN_SUMMARY_TARGET_OPTIONS.find((item) => item.value === grainSummaryTarget.value)?.label || "整仓均温");
// 不同模式共用分页组件时按当前主线切换状态源，避免 grain/env 两套页码互相污染。
const currentRecordPageState = computed(() => (mode.value === "grain" ? grainRecordPageState : envRecordPageState));

function formatDateTime(value) { return value ? String(value).replace("T", " ") : "-"; }
function formatTemperatureValue(value) { return value == null || value === "" ? "-" : Number(value).toFixed(2); }
function getInitialEnvMetricCode() { return envMetricOptions.value[0]?.value || "humidity"; }
function resolveWarehouseScope(warehouseId) {
  if (isWarehouseManager.value) {
    return managedWarehouseId.value || undefined;
  }
  return warehouseId || undefined;
}
function applyManagedWarehouseScope() {
  if (!isWarehouseManager.value || managedWarehouseId.value == null) {
    return;
  }

  filters.warehouseId = managedWarehouseId.value;
  grainForm.warehouseId = managedWarehouseId.value;
  envForm.warehouseId = managedWarehouseId.value;
}
function formatDatePart(value) { return String(value).padStart(2, "0"); }
function formatDateTimeForPicker(date) {
  return `${date.getFullYear()}-${formatDatePart(date.getMonth() + 1)}-${formatDatePart(date.getDate())} ${formatDatePart(date.getHours())}:${formatDatePart(date.getMinutes())}:${formatDatePart(date.getSeconds())}`;
}
function createRecentDateRange(days) {
  const end = new Date();
  const start = new Date(end.getTime() - days * DAY_IN_MS);
  return [formatDateTimeForPicker(start), formatDateTimeForPicker(end)];
}
function getChartAxisLabelInterval(pointCount) {
  if (pointCount <= MAX_CHART_AXIS_LABELS) {
    return 0;
  }
  return Math.max(0, Math.ceil(pointCount / MAX_CHART_AXIS_LABELS) - 1);
}
function formatChartAxisLabel(value) {
  const normalized = formatDateTime(value);
  return normalized === "-" ? "" : normalized.slice(5, 16);
}
function ensureDefaultGrainSummaryRange() {
  if (mode.value !== "grain" || grainSummaryDefaultInitialized.value) {
    return;
  }
  grainSummaryDefaultInitialized.value = true;
  if (Array.isArray(grainSummaryRange.value) && grainSummaryRange.value.length === 2) {
    return;
  }
  grainSummaryRange.value = createRecentDateRange(DEFAULT_GRAIN_SUMMARY_DAYS);
}
function resolveDateRange(rangeValue) {
  return Array.isArray(rangeValue) && rangeValue.length === 2
    ? { startTime: rangeValue[0], endTime: rangeValue[1] }
    : { startTime: undefined, endTime: undefined };
}
// 温度范围允许用户倒序输入，前端先纠正成合法区间，避免把无意义筛选条件传回后端。
function normalizeNumericRange(minValue, maxValue) {
  const parsedMin = minValue !== null && minValue !== "" && !Number.isNaN(Number(minValue)) ? Number(minValue) : null;
  const parsedMax = maxValue !== null && maxValue !== "" && !Number.isNaN(Number(maxValue)) ? Number(maxValue) : null;
  if (parsedMin != null && parsedMax != null && parsedMin > parsedMax) {
    return { min: parsedMax, max: parsedMin };
  }
  return { min: parsedMin, max: parsedMax };
}
function getGrainSummaryValue(row, target) {
  switch (target) {
    case "LAYER_1_AVG": return row.layer1Avg;
    case "LAYER_2_AVG": return row.layer2Avg;
    case "LAYER_3_AVG": return row.layer3Avg;
    case "LAYER_4_AVG": return row.layer4Avg;
    default: return row.avgTemp;
  }
}
function resetGrainForm(row = null) {
  grainForm.warehouseId = resolveWarehouseScope(row?.warehouseId || visibleWarehouses.value[0]?.id || "");
  grainForm.zoneCode = row?.zoneCode || "A";
  grainForm.layerNo = row?.layerNo || 1;
  grainForm.pointNo = row?.pointNo || 1;
  grainForm.collectedAt = row?.collectedAt || "";
  grainForm.temperatureValue = row?.temperatureValue ?? 24.5;
  grainForm.probeCode = row?.probeCode || "";
  grainForm.remark = row?.remark || "";
}
function resetEnvForm(row = null) {
  envForm.warehouseId = resolveWarehouseScope(row?.warehouseId || visibleWarehouses.value[0]?.id || "");
  envForm.metricCode = row?.metricCode || getInitialEnvMetricCode();
  envForm.metricValue = row?.metricValue ?? 58.2;
  envForm.collectedAt = row?.collectedAt || "";
}
function openManualDialog() {
  editingId.value = null;
  if (mode.value === "grain") { resetGrainForm(); } else { resetEnvForm(); }
  dialogVisible.value = true;
}
function openEditDialog(row) {
  editingId.value = row.id;
  if (mode.value === "grain") { resetGrainForm(row); } else { resetEnvForm(row); }
  dialogVisible.value = true;
}
async function loadMetricOptions() {
  metricOptions.value = await fetchMetricOptions();
  filters.metricCode = getInitialEnvMetricCode();
  envForm.metricCode = getInitialEnvMetricCode();
}
async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();
  applyManagedWarehouseScope();
  if (!filters.warehouseId && visibleWarehouses.value.length > 0) { filters.warehouseId = visibleWarehouses.value[0].id; }
  if (!grainForm.warehouseId && visibleWarehouses.value.length > 0) { grainForm.warehouseId = visibleWarehouses.value[0].id; }
  if (!envForm.warehouseId && visibleWarehouses.value.length > 0) { envForm.warehouseId = visibleWarehouses.value[0].id; }
}
async function loadGrainRecordFilterOptions() {
  grainFilterOptions.value = await fetchGrainTempRecordFilterOptions({ warehouseId: resolveWarehouseScope(filters.warehouseId) });
}
async function loadGrainSummarySeries() {
  chartLoading.value = true;
  try {
    const { startTime, endTime } = resolveDateRange(grainSummaryRange.value);
    grainSummarySeriesRows.value = await fetchGrainTempSummaries({ warehouseId: resolveWarehouseScope(filters.warehouseId), startTime, endTime });
  } finally { chartLoading.value = false; }
  await nextTick();
  renderChart();
}
async function loadGrainSummaryTable() {
  summaryLoading.value = true;
  try {
    const { startTime, endTime } = resolveDateRange(grainSummaryRange.value);
    // 图表查询态和表格细筛态分开维护，汇总表会叠加 warning/keyword/温度区间这些更细的筛选条件。
    const { min, max } = normalizeNumericRange(grainSummaryFilters.tempMin, grainSummaryFilters.tempMax);
    const summaryPage = await fetchGrainTempSummaryPage({
      warehouseId: resolveWarehouseScope(filters.warehouseId),
      startTime,
      endTime,
      keyword: grainSummaryFilters.keyword.trim() || undefined,
      warningLevel: grainSummaryFilters.warningLevel || undefined,
      tempMin: min ?? undefined,
      tempMax: max ?? undefined,
      pageNum: grainSummaryPageState.pageNum,
      pageSize: grainSummaryPageState.pageSize
    });
    grainSummaryTableRows.value = summaryPage.list;
    grainSummaryPageState.total = summaryPage.total;
    grainSummaryPageState.pageNum = summaryPage.pageNum;
    grainSummaryPageState.pageSize = summaryPage.pageSize;
  } finally { summaryLoading.value = false; }
}
async function loadGrainRecords() {
  recordLoading.value = true;
  try {
    const { startTime, endTime } = resolveDateRange(grainRecordCollectedRange.value);
    const { min, max } = normalizeNumericRange(grainRecordFilters.tempMin, grainRecordFilters.tempMax);
    const recordList = await fetchGrainTempRecords({
      warehouseId: resolveWarehouseScope(filters.warehouseId),
      startTime,
      endTime,
      zoneCode: grainRecordFilters.zoneCode || undefined,
      layerNo: grainRecordFilters.layerNo ?? undefined,
      pointNo: grainRecordFilters.pointNo ?? undefined,
      tempMin: min ?? undefined,
      tempMax: max ?? undefined,
      keyword: grainRecordKeyword.value.trim() || undefined,
      pageNum: grainRecordPageState.pageNum,
      pageSize: grainRecordPageState.pageSize
    });
    rows.value = recordList.list;
    grainRecordPageState.total = recordList.total;
    grainRecordPageState.pageNum = recordList.pageNum;
    grainRecordPageState.pageSize = recordList.pageSize;
    envTrendRows.value = [];
  } finally { recordLoading.value = false; }
}
async function loadEnvData() {
  recordLoading.value = true;
  chartLoading.value = true;
  try {
    const [sensorPage, trendList] = await Promise.all([
      fetchSensorData({ warehouseId: resolveWarehouseScope(filters.warehouseId), metricCode: filters.metricCode, pageNum: envRecordPageState.pageNum, pageSize: envRecordPageState.pageSize, keyword: envRecordKeyword.value.trim() || undefined }),
      fetchSensorTrend({ warehouseId: resolveWarehouseScope(filters.warehouseId), metricCode: filters.metricCode })
    ]);
    rows.value = sensorPage.list;
    envRecordPageState.total = sensorPage.total;
    envRecordPageState.pageNum = sensorPage.pageNum;
    envRecordPageState.pageSize = sensorPage.pageSize;
    envTrendRows.value = trendList;
    grainSummarySeriesRows.value = [];
    grainSummaryTableRows.value = [];
  } finally {
    recordLoading.value = false;
    chartLoading.value = false;
  }
  await nextTick();
  renderChart();
}
async function reloadCurrentModeData() {
  if (mode.value === "grain") {
    await Promise.all([loadGrainSummarySeries(), loadGrainSummaryTable(), loadGrainRecords()]);
    return;
  }
  await loadEnvData();
}
async function submit() {
  if (mode.value === "grain") {
    const payload = { warehouseId: resolveWarehouseScope(grainForm.warehouseId), zoneCode: grainForm.zoneCode, layerNo: grainForm.layerNo, pointNo: grainForm.pointNo, collectedAt: grainForm.collectedAt, temperatureValue: grainForm.temperatureValue, probeCode: grainForm.probeCode, remark: grainForm.remark };
    if (isEditing.value) { await updateGrainTempRecord(editingId.value, payload); ElMessage.success("粮温原始记录已更新，汇总与真实预警已联动重算"); }
    else { await createGrainTempRecord(payload); ElMessage.success("粮温原始记录已写入数据库"); }
  } else {
    const payload = { warehouseId: resolveWarehouseScope(envForm.warehouseId), metricCode: envForm.metricCode, metricValue: envForm.metricValue, collectedAt: envForm.collectedAt };
    if (isEditing.value) { await updateSensorData(editingId.value, payload); ElMessage.success("环境数据已更新"); }
    else { await createSensorData(payload); ElMessage.success("环境数据已写入数据库"); }
  }
  dialogVisible.value = false;
  editingId.value = null;
  await reloadCurrentModeData();
}
async function handleDelete(row) {
  const label = mode.value === "grain" ? "粮温原始记录" : "环境数据";
  await ElMessageBox.confirm(`确定删除这条${label}吗？`, "确认删除", { type: "warning" });
  if (mode.value === "grain") { await deleteGrainTempRecord(row.id); ElMessage.success("粮温原始记录已删除，汇总与真实预警已联动重算"); }
  else { await deleteSensorData(row.id); ElMessage.success("环境数据已删除"); }
  await reloadCurrentModeData();
}
async function handleImportUpload(uploadFile) {
  importLoadingCount.value += 1;
  try {
    if (mode.value === "grain") { const result = await importGrainTemp(uploadFile); ElMessage.success(`粮温导入成功，批次号：${result.batchNo}`); }
    else {
      const result = await importSensorData(uploadFile);
      if (result.failedCount > 0) { throw new Error(result.errorMessages?.[0] || "导入失败"); }
      ElMessage.success(`环境数据导入成功，批次号：${result.batchNo}`);
    }
    // 导入成功后统一刷新图表与表格，保证最新批次同时反映在主图、汇总表和明细列表里。
    await reloadCurrentModeData();
  } catch (error) {
    ElMessage.error(error.message || "导入失败");
  } finally {
    importLoadingCount.value = Math.max(importLoadingCount.value - 1, 0);
  }
  return false;
}
async function handleTemplateDownload() {
  try {
    if (mode.value === "grain") {
      await downloadGrainTempTemplate();
      ElMessage.success("粮温模板已开始下载");
      return;
    }
    await downloadSensorTemplate();
    ElMessage.success("环境数据模板已开始下载");
  } catch (error) {
    ElMessage.error(error.message || "模板下载失败");
  }
}
async function handleRecordPageChange(page) { currentRecordPageState.value.pageNum = page; if (mode.value === "grain") { await loadGrainRecords(); return; } await loadEnvData(); }
async function handleRecordPageSizeChange(size) { currentRecordPageState.value.pageSize = size; currentRecordPageState.value.pageNum = 1; if (mode.value === "grain") { await loadGrainRecords(); return; } await loadEnvData(); }
async function handleSummaryPageChange(page) { grainSummaryPageState.pageNum = page; await loadGrainSummaryTable(); }
async function handleSummaryPageSizeChange(size) { grainSummaryPageState.pageSize = size; grainSummaryPageState.pageNum = 1; await loadGrainSummaryTable(); }
async function handleSearch() {
  if (mode.value === "grain") {
    grainSummaryPageState.pageNum = 1;
    grainRecordPageState.pageNum = 1;
    await loadGrainRecordFilterOptions();
    await Promise.all([loadGrainSummarySeries(), loadGrainSummaryTable(), loadGrainRecords()]);
    return;
  }
  envRecordPageState.pageNum = 1;
  await loadEnvData();
}
async function applyGrainSummaryFilters() { grainSummaryPageState.pageNum = 1; await loadGrainSummaryTable(); }
async function resetGrainSummaryFilters() { grainSummaryFilters.keyword = ""; grainSummaryFilters.warningLevel = ""; grainSummaryFilters.tempMin = null; grainSummaryFilters.tempMax = null; grainSummaryPageState.pageNum = 1; await loadGrainSummaryTable(); }
async function applyGrainRecordFilters() { grainRecordPageState.pageNum = 1; await loadGrainRecords(); }
async function resetGrainRecordFilters() { grainRecordFilters.zoneCode = ""; grainRecordFilters.layerNo = null; grainRecordFilters.pointNo = null; grainRecordFilters.tempMin = null; grainRecordFilters.tempMax = null; grainRecordCollectedRange.value = null; grainRecordKeyword.value = ""; grainRecordPageState.pageNum = 1; await loadGrainRecords(); }
async function handleModeChange() {
  editingId.value = null;
  applyManagedWarehouseScope();
  if (mode.value === "env") {
    filters.metricCode = getInitialEnvMetricCode();
    resetEnvForm();
    envRecordPageState.pageNum = 1;
    await loadEnvData();
    return;
  }
  resetGrainForm();
  ensureDefaultGrainSummaryRange();
  grainRecordPageState.pageNum = 1;
  grainSummaryPageState.pageNum = 1;
  await loadGrainRecordFilterOptions();
  await Promise.all([loadGrainSummarySeries(), loadGrainSummaryTable(), loadGrainRecords()]);
}
function scheduleRecordKeywordReload() {
  clearTimeout(recordKeywordTimer);
  recordKeywordTimer = setTimeout(() => {
    if (mode.value === "grain") { grainRecordPageState.pageNum = 1; loadGrainRecords(); return; }
    envRecordPageState.pageNum = 1; loadEnvData();
  }, 400);
}
watch(grainRecordKeyword, scheduleRecordKeywordReload);
watch(envRecordKeyword, scheduleRecordKeywordReload);
watch(grainSummaryTarget, async () => { if (mode.value !== "grain") return; await nextTick(); renderChart(); });
watch(() => filters.warehouseId, async () => { if (mode.value !== "grain") return; await loadGrainRecordFilterOptions(); });
watch(managedWarehouseId, () => { applyManagedWarehouseScope(); });
function renderChart() {
  if (!chartRef.value) return;
  if (!chart) chart = echarts.init(chartRef.value);
  if (mode.value === "grain") {
    const grainChartRows = grainSummarySeriesRows.value;
    const grainXAxisData = grainChartRows.map((item) => formatDateTime(item.collectedAt));
    const denseAxis = grainXAxisData.length > MAX_CHART_AXIS_LABELS;
    chart.setOption({
      tooltip: { trigger: "axis" },
      legend: { data: [grainSummaryTargetLabel.value, "最高温"] },
      grid: { left: 40, right: 24, top: 36, bottom: denseAxis ? 88 : 56 },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: grainXAxisData,
        axisTick: { alignWithLabel: true },
        axisLabel: {
          interval: getChartAxisLabelInterval(grainXAxisData.length),
          rotate: denseAxis ? 32 : 0,
          hideOverlap: true,
          formatter: (value) => formatChartAxisLabel(value)
        }
      },
      yAxis: { type: "value" },
      dataZoom: [
        { type: "inside", filterMode: "none" },
        {
          type: "slider",
          filterMode: "none",
          height: 18,
          bottom: 16,
          show: denseAxis
        }
      ],
      series: [
        { name: grainSummaryTargetLabel.value, type: "line", smooth: true, data: grainChartRows.map((item) => getGrainSummaryValue(item, grainSummaryTarget.value)), lineStyle: { color: "#0b7a75" }, itemStyle: { color: "#0b7a75" } },
        { name: "最高温", type: "line", smooth: true, data: grainChartRows.map((item) => item.maxTemp), lineStyle: { color: "#ea580c" }, itemStyle: { color: "#ea580c" } }
      ]
    }, true);
    return;
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 32, right: 18, top: 30, bottom: 28 },
    xAxis: { type: "category", data: envTrendRows.value.map((item) => formatDateTime(item.time)) },
    yAxis: { type: "value" },
    series: [{ name: "环境值", type: "line", smooth: true, data: envTrendRows.value.map((item) => item.value), lineStyle: { color: "#0b7a75" }, itemStyle: { color: "#0b7a75" }, areaStyle: { color: "rgba(11, 122, 117, 0.12)" } }]
  });
}
onMounted(async () => {
  await loadMetricOptions();
  await loadWarehouses();
  resetGrainForm();
  resetEnvForm();
  ensureDefaultGrainSummaryRange();
  await loadGrainRecordFilterOptions();
  await Promise.all([loadGrainSummarySeries(), loadGrainSummaryTable(), loadGrainRecords()]);
});
onBeforeUnmount(() => { clearTimeout(recordKeywordTimer); if (chart) chart.dispose(); });
</script>
<template>
  <div class="page-stack">
    <el-row :gutter="16">
      <el-col :xs="24" :xl="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">数据主线选择</div>
          </template>

          <el-radio-group v-model="mode" @change="handleModeChange">
            <el-radio-button label="grain">粮温主线</el-radio-button>
            <el-radio-button label="env">普通环境数据</el-radio-button>
          </el-radio-group>

          <div class="compact-lines data-hint-block">
            <div v-if="mode === 'grain'">粮温模式当前维护原始测点记录，系统会自动联动重算层温、整仓汇总和真实预警。</div>
            <div v-else>普通环境模式维护湿度 / 二氧化碳单值记录，支持手工录入、导入、编辑、删除和趋势查询。</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">查询条件</div>
          </template>

          <el-form inline>
            <el-form-item label="仓库">
              <el-select v-model="filters.warehouseId" :clearable="!isWarehouseManager" :disabled="isWarehouseManager" style="width: 180px">
                <el-option v-for="item in visibleWarehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="mode === 'grain'" label="时间范围">
              <el-date-picker
                v-model="grainSummaryRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="grain-filter-daterange"
              />
            </el-form-item>

            <el-form-item v-if="mode === 'env'" label="指标">
              <el-select v-model="filters.metricCode" style="width: 160px">
                <el-option v-for="item in envMetricOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="canWriteData" class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">数据录入与导入</div>
      </template>

      <div class="toolbar-row">
        <el-button type="primary" @click="openManualDialog">{{ mode === "grain" ? "新增粮温记录" : "手工录入" }}</el-button>
        <el-upload :show-file-list="false" :before-upload="handleImportUpload" accept=".csv,.xls,.xlsx" multiple>
          <el-button :loading="importLoading">{{ mode === "grain" ? "导入粮温模板" : "导入环境数据" }}</el-button>
        </el-upload>
        <el-button plain @click="handleTemplateDownload">下载导入模板</el-button>
      </div>

      <div class="compact-lines">
        <div v-if="mode === 'grain'">粮温固定 XLSX：含分区标题与合并单元格说明。「一、基础信息」填写仓库编码（可兼容旧版仓库ID）、采集时间（括号内英文键与程序识别一致）。「二、测点温度矩阵」按区域分块：块首行填区域编码与缆号/探头编码；表头行含「层号」与「点位」，数据行左侧为层号、向右为各点位列温度。底部「三、汇总分析」可留空，导入后由系统汇总。</div>
        <div v-if="mode === 'grain'">后端仍兼容旧版纯英文标签的固定模板、CSV 与行式 Excel；同一测点新值会覆盖旧值，手工维护会直接影响原始测点记录。</div>
        <div v-else>普通环境模板字段：warehouseId、metricCode、metricValue、collectedAt、remark。</div>
      </div>
    </el-card>

    <el-card class="panel-card" shadow="never" v-loading="chartLoading">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">{{ mode === "grain" ? "粮温汇总趋势图" : "环境趋势图" }}</div>
            <div v-if="mode === 'grain'" class="panel-subtitle">共享查询条件为仓库与时间范围；首次进入默认最近 30 天，图表固定保留“最高温”参考线。</div>
          </div>
          <div v-if="mode === 'grain'" class="toolbar-row">
            <el-select v-model="grainSummaryTarget" class="prediction-select-md">
              <el-option v-for="item in GRAIN_SUMMARY_TARGET_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
        </div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-card v-if="mode === 'grain'" class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">粮温汇总结果</div>
      </template>

      <div class="toolbar-row table-toolbar">
        <el-input v-model="grainSummaryFilters.keyword" class="table-search-input" clearable placeholder="搜索仓库、预警等级、温度、时间" :prefix-icon="Search" />
        <el-select v-model="grainSummaryFilters.warningLevel" clearable placeholder="预警等级" style="width: 140px">
          <el-option v-for="item in GRAIN_WARNING_LEVEL_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <div class="grain-temp-range">
          <el-input-number v-model="grainSummaryFilters.tempMin" :step="0.1" :controls="false" placeholder="均温最低" />
          <span class="grain-temp-range-sep">~</span>
          <el-input-number v-model="grainSummaryFilters.tempMax" :step="0.1" :controls="false" placeholder="均温最高" />
        </div>
        <el-button type="primary" @click="applyGrainSummaryFilters">应用筛选</el-button>
        <el-button plain @click="resetGrainSummaryFilters">重置</el-button>
      </div>

      <div v-if="latestGrainSummary" class="compact-lines summary-latest-hint">
        <div>最新汇总：{{ latestGrainSummary.warehouseName }} / {{ formatDateTime(latestGrainSummary.collectedAt) }}</div>
        <div>当前主线：{{ grainSummaryTargetLabel }}，预警等级：{{ latestGrainSummary.warningLevel }}，说明：{{ latestGrainSummary.warningMessage || "暂无" }}</div>
      </div>

      <el-table :data="grainSummaryTableRows" stripe v-loading="summaryLoading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column label="整仓均温"><template #default="{ row }">{{ formatTemperatureValue(row.avgTemp) }}</template></el-table-column>
        <el-table-column label="最高温"><template #default="{ row }">{{ formatTemperatureValue(row.maxTemp) }}</template></el-table-column>
        <el-table-column label="最低温"><template #default="{ row }">{{ formatTemperatureValue(row.minTemp) }}</template></el-table-column>
        <el-table-column label="一层均温"><template #default="{ row }">{{ formatTemperatureValue(row.layer1Avg) }}</template></el-table-column>
        <el-table-column label="二层均温"><template #default="{ row }">{{ formatTemperatureValue(row.layer2Avg) }}</template></el-table-column>
        <el-table-column label="三层均温"><template #default="{ row }">{{ formatTemperatureValue(row.layer3Avg) }}</template></el-table-column>
        <el-table-column label="四层均温"><template #default="{ row }">{{ formatTemperatureValue(row.layer4Avg) }}</template></el-table-column>
        <el-table-column prop="warningLevel" label="预警等级" />
        <el-table-column label="检测时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.collectedAt) }}</template></el-table-column>
        <el-table-column prop="warningMessage" label="预警说明" min-width="180" />
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="grainSummaryPageState.pageNum"
          :page-size="grainSummaryPageState.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="grainSummaryPageState.total"
          @current-change="handleSummaryPageChange"
          @size-change="handleSummaryPageSizeChange"
        />
      </div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">{{ mode === "grain" ? "粮温原始测点记录" : "环境数据记录" }}</div>
      </template>

      <template v-if="mode === 'grain'">
        <el-form class="grain-record-filter-form table-toolbar" label-width="72px" inline>
          <el-form-item label="区域"><el-select v-model="grainRecordFilters.zoneCode" clearable placeholder="全部" filterable><el-option v-for="z in grainFilterOptions.zoneCodes" :key="z" :label="z" :value="z" /></el-select></el-form-item>
          <el-form-item label="层号"><el-select v-model="grainRecordFilters.layerNo" clearable placeholder="全部"><el-option v-for="n in grainFilterOptions.layerNos" :key="n" :label="String(n)" :value="n" /></el-select></el-form-item>
          <el-form-item label="点位"><el-select v-model="grainRecordFilters.pointNo" clearable placeholder="全部"><el-option v-for="n in grainFilterOptions.pointNos" :key="n" :label="String(n)" :value="n" /></el-select></el-form-item>
          <el-form-item label="温度℃">
            <div class="grain-temp-range">
              <el-input-number v-model="grainRecordFilters.tempMin" :step="0.1" :controls="false" placeholder="最低" />
              <span class="grain-temp-range-sep">~</span>
              <el-input-number v-model="grainRecordFilters.tempMax" :step="0.1" :controls="false" placeholder="最高" />
            </div>
          </el-form-item>
          <el-form-item label="采集时间">
            <el-date-picker
              v-model="grainRecordCollectedRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="grain-filter-daterange"
            />
          </el-form-item>
          <el-form-item class="grain-filter-actions">
            <el-button type="primary" @click="applyGrainRecordFilters">应用筛选</el-button>
            <el-button plain @click="resetGrainRecordFilters">重置</el-button>
          </el-form-item>
          <el-form-item label="关键词" class="grain-filter-keyword-row">
            <el-input v-model="grainRecordKeyword" class="table-search-input grain-keyword-input" clearable placeholder="模糊匹配仓库名、区域、层号、点位等（可选）" :prefix-icon="Search" />
          </el-form-item>
        </el-form>
        <div class="grain-filter-hint compact-lines">仓库以右上方「查询条件」为准；这里的采集时间仅作用于原始测点记录，不会改变上方粮温汇总图表与汇总表。关键词支持防抖自动查询。</div>
      </template>

      <div v-else class="toolbar-row table-toolbar">
        <el-input v-model="envRecordKeyword" class="table-search-input" clearable placeholder="搜索仓库、指标名称、指标编码（服务端模糊匹配）" :prefix-icon="Search" />
      </div>

      <el-table v-if="mode === 'grain'" :data="rows" stripe v-loading="recordLoading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="zoneCode" label="区域" width="90" />
        <el-table-column prop="layerNo" label="层号" width="90" />
        <el-table-column prop="pointNo" label="点位" width="90" />
        <el-table-column prop="temperatureValue" label="温度值" />
        <el-table-column label="采集时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.collectedAt) }}</template></el-table-column>
        <el-table-column prop="sourceType" label="来源" width="100" />
        <el-table-column v-if="canWriteData" label="操作" width="160" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEditDialog(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column>
      </el-table>

      <el-table v-else :data="rows" stripe v-loading="recordLoading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="metricName" label="指标" />
        <el-table-column prop="metricValue" label="数值" />
        <el-table-column label="采集时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.collectedAt) }}</template></el-table-column>
        <el-table-column prop="sourceType" label="来源" />
        <el-table-column prop="qualityFlag" label="质量标记" />
        <el-table-column v-if="canWriteData" label="操作" width="160" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEditDialog(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="currentRecordPageState.pageNum"
          :page-size="currentRecordPageState.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="currentRecordPageState.total"
          @current-change="handleRecordPageChange"
          @size-change="handleRecordPageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form v-if="mode === 'grain'" label-position="top" class="form-grid-2">
        <el-form-item label="仓库"><el-select v-model="grainForm.warehouseId" :disabled="isWarehouseManager"><el-option v-for="item in visibleWarehouses" :key="item.id" :label="item.warehouseName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="区域"><el-input v-model="grainForm.zoneCode" placeholder="例如 A" /></el-form-item>
        <el-form-item label="层号"><el-input-number v-model="grainForm.layerNo" :min="1" :max="8" /></el-form-item>
        <el-form-item label="点位"><el-input-number v-model="grainForm.pointNo" :min="1" :max="16" /></el-form-item>
        <el-form-item label="温度值"><el-input-number v-model="grainForm.temperatureValue" :step="0.1" /></el-form-item>
        <el-form-item label="测温缆编号"><el-input v-model="grainForm.probeCode" placeholder="不填则自动生成" /></el-form-item>
        <el-form-item label="采集时间"><el-date-picker v-model="grainForm.collectedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择采集时间" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="grainForm.remark" placeholder="可选" /></el-form-item>
      </el-form>

      <el-form v-else label-position="top" class="form-grid-2">
        <el-form-item label="仓库"><el-select v-model="envForm.warehouseId" :disabled="isWarehouseManager"><el-option v-for="item in visibleWarehouses" :key="item.id" :label="item.warehouseName" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="指标"><el-select v-model="envForm.metricCode"><el-option v-for="item in envMetricOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="采样值"><el-input-number v-model="envForm.metricValue" :step="0.1" /></el-form-item>
        <el-form-item label="采集时间"><el-date-picker v-model="envForm.collectedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="不填则默认当前时间" /></el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">{{ isEditing ? "保存修改" : "提交数据" }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
