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
  fetchMetricOptions,
  fetchSensorData,
  fetchSensorTrend,
  fetchWarehouses,
  importGrainTemp,
  importSensorData,
  updateGrainTempRecord,
  updateSensorData
} from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";
import { filterRows } from "../utils/fuzzyText";

const chartRef = ref();
const loading = ref(false);
const importLoading = ref(false);
const dialogVisible = ref(false);
const mode = ref("grain");
const warehouses = ref([]);
const rows = ref([]);
const grainSummaryRows = ref([]);
const metricOptions = ref([]);
const editingId = ref(null);
const envTrendRows = ref([]);
const grainSummaryKeyword = ref("");
const grainRecordKeyword = ref("");
const envRecordKeyword = ref("");
const grainFilterOptions = ref({ zoneCodes: [], layerNos: [], pointNos: [] });
const grainCollectedRange = ref(null);
const grainRecordFilters = reactive({
  zoneCode: "",
  layerNo: null,
  pointNo: null,
  tempMin: null,
  tempMax: null
});

const filteredGrainSummaries = computed(() =>
  filterRows(grainSummaryRows.value, grainSummaryKeyword.value, (row) => [
    row.warehouseName,
    row.warningLevel,
    row.warningMessage,
    String(row.avgTemp ?? ""),
    String(row.maxTemp ?? ""),
    String(row.minTemp ?? ""),
    formatDateTime(row.collectedAt)
  ])
);

const grainSummaryPagination = useClientPagination(filteredGrainSummaries);

watch(grainSummaryKeyword, () => {
  grainSummaryPagination.resetPagination();
});

let chart;
let recordKeywordTimer;

const filters = reactive({
  warehouseId: "",
  metricCode: "humidity"
});

const grainForm = reactive({
  warehouseId: "",
  zoneCode: "A",
  layerNo: 1,
  pointNo: 1,
  collectedAt: "",
  temperatureValue: 24.5,
  probeCode: "",
  remark: ""
});

const envForm = reactive({
  warehouseId: "",
  metricCode: "humidity",
  metricValue: 58.2,
  collectedAt: ""
});

const envMetricOptions = computed(() =>
  metricOptions.value.filter((item) => item.value !== "temperature")
);

const isEditing = computed(() => editingId.value !== null);
const dialogTitle = computed(() => {
  if (mode.value === "grain") {
    return isEditing.value ? "编辑粮温原始记录" : "新增粮温原始记录";
  }

  return isEditing.value ? "编辑普通环境数据" : "手工录入普通环境数据";
});

const latestGrainSummary = computed(() => filteredGrainSummaries.value.at(-1) || null);
const grainRecordPageState = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});
const envRecordPageState = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});
const currentRecordPageState = computed(() =>
  mode.value === "grain" ? grainRecordPageState : envRecordPageState
);

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getInitialEnvMetricCode() {
  return envMetricOptions.value[0]?.value || "humidity";
}

function resetGrainForm(row = null) {
  grainForm.warehouseId = row?.warehouseId || warehouses.value[0]?.id || "";
  grainForm.zoneCode = row?.zoneCode || "A";
  grainForm.layerNo = row?.layerNo || 1;
  grainForm.pointNo = row?.pointNo || 1;
  grainForm.collectedAt = row?.collectedAt || "";
  grainForm.temperatureValue = row?.temperatureValue ?? 24.5;
  grainForm.probeCode = row?.probeCode || "";
  grainForm.remark = row?.remark || "";
}

function resetEnvForm(row = null) {
  envForm.warehouseId = row?.warehouseId || warehouses.value[0]?.id || "";
  envForm.metricCode = row?.metricCode || getInitialEnvMetricCode();
  envForm.metricValue = row?.metricValue ?? 58.2;
  envForm.collectedAt = row?.collectedAt || "";
}

function openManualDialog() {
  editingId.value = null;

  if (mode.value === "grain") {
    resetGrainForm();
  } else {
    resetEnvForm();
  }

  dialogVisible.value = true;
}

function openEditDialog(row) {
  editingId.value = row.id;

  if (mode.value === "grain") {
    resetGrainForm(row);
  } else {
    resetEnvForm(row);
  }

  dialogVisible.value = true;
}

async function loadMetricOptions() {
  metricOptions.value = await fetchMetricOptions();
  filters.metricCode = getInitialEnvMetricCode();
  envForm.metricCode = getInitialEnvMetricCode();
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!filters.warehouseId && warehouses.value.length > 0) {
    filters.warehouseId = warehouses.value[0].id;
  }

  if (!grainForm.warehouseId && warehouses.value.length > 0) {
    grainForm.warehouseId = warehouses.value[0].id;
  }

  if (!envForm.warehouseId && warehouses.value.length > 0) {
    envForm.warehouseId = warehouses.value[0].id;
  }
}

async function loadGrainRecordFilterOptions() {
  grainFilterOptions.value = await fetchGrainTempRecordFilterOptions({
    warehouseId: filters.warehouseId || undefined
  });
}

async function loadData() {
  loading.value = true;

  try {
    if (mode.value === "grain") {
      let startTime;
      let endTime;
      if (Array.isArray(grainCollectedRange.value) && grainCollectedRange.value.length === 2) {
        [startTime, endTime] = grainCollectedRange.value;
      }

      let tempMin = grainRecordFilters.tempMin;
      let tempMax = grainRecordFilters.tempMax;
      const nMin = tempMin !== null && tempMin !== "" && !Number.isNaN(Number(tempMin)) ? Number(tempMin) : null;
      const nMax = tempMax !== null && tempMax !== "" && !Number.isNaN(Number(tempMax)) ? Number(tempMax) : null;
      let outMin = nMin;
      let outMax = nMax;
      if (nMin != null && nMax != null && nMin > nMax) {
        outMin = nMax;
        outMax = nMin;
      }

      const [summaryList, recordList] = await Promise.all([
        fetchGrainTempSummaries({ warehouseId: filters.warehouseId }),
        fetchGrainTempRecords({
          warehouseId: filters.warehouseId,
          startTime,
          endTime,
          zoneCode: grainRecordFilters.zoneCode || undefined,
          layerNo: grainRecordFilters.layerNo ?? undefined,
          pointNo: grainRecordFilters.pointNo ?? undefined,
          tempMin: outMin ?? undefined,
          tempMax: outMax ?? undefined,
          keyword: grainRecordKeyword.value.trim() || undefined,
          pageNum: grainRecordPageState.pageNum,
          pageSize: grainRecordPageState.pageSize
        })
      ]);
      grainSummaryRows.value = summaryList;
      rows.value = recordList.list;
      grainRecordPageState.total = recordList.total;
      grainRecordPageState.pageNum = recordList.pageNum;
      grainRecordPageState.pageSize = recordList.pageSize;
      envTrendRows.value = [];
    } else {
      grainSummaryRows.value = [];
      const [sensorPage, trendList] = await Promise.all([
        fetchSensorData({
          warehouseId: filters.warehouseId,
          metricCode: filters.metricCode,
          pageNum: envRecordPageState.pageNum,
          pageSize: envRecordPageState.pageSize,
          keyword: envRecordKeyword.value.trim() || undefined
        }),
        fetchSensorTrend({
          warehouseId: filters.warehouseId,
          metricCode: filters.metricCode
        })
      ]);
      rows.value = sensorPage.list;
      envRecordPageState.total = sensorPage.total;
      envRecordPageState.pageNum = sensorPage.pageNum;
      envRecordPageState.pageSize = sensorPage.pageSize;
      envTrendRows.value = trendList;
    }

    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

async function submit() {
  if (mode.value === "grain") {
    const payload = {
      warehouseId: grainForm.warehouseId,
      zoneCode: grainForm.zoneCode,
      layerNo: grainForm.layerNo,
      pointNo: grainForm.pointNo,
      collectedAt: grainForm.collectedAt,
      temperatureValue: grainForm.temperatureValue,
      probeCode: grainForm.probeCode,
      remark: grainForm.remark
    };

    if (isEditing.value) {
      await updateGrainTempRecord(editingId.value, payload);
      ElMessage.success("粮温原始记录已更新，汇总与真实预警已联动重算");
    } else {
      await createGrainTempRecord(payload);
      ElMessage.success("粮温原始记录已写入数据库");
    }
  } else {
    const payload = {
      warehouseId: envForm.warehouseId,
      metricCode: envForm.metricCode,
      metricValue: envForm.metricValue,
      collectedAt: envForm.collectedAt
    };

    if (isEditing.value) {
      await updateSensorData(editingId.value, payload);
      ElMessage.success("环境数据已更新");
    } else {
      await createSensorData(payload);
      ElMessage.success("环境数据已写入数据库");
    }
  }

  dialogVisible.value = false;
  editingId.value = null;
  await loadData();
}

async function handleDelete(row) {
  const label = mode.value === "grain" ? "粮温原始记录" : "环境数据";
  await ElMessageBox.confirm(`确定删除这条${label}吗？`, "确认删除", {
    type: "warning"
  });

  if (mode.value === "grain") {
    await deleteGrainTempRecord(row.id);
    ElMessage.success("粮温原始记录已删除，汇总与真实预警已联动重算");
  } else {
    await deleteSensorData(row.id);
    ElMessage.success("环境数据已删除");
  }

  await loadData();
}

async function handleImportUpload(uploadFile) {
  importLoading.value = true;

  try {
    if (mode.value === "grain") {
      const result = await importGrainTemp(uploadFile.raw);
      ElMessage.success(`粮温导入成功，批次号：${result.batchNo}`);
    } else {
      const result = await importSensorData(uploadFile.raw);
      if (result.failedCount > 0) {
        throw new Error(result.errorMessages?.[0] || "导入失败");
      }
      ElMessage.success(`环境数据导入成功，批次号：${result.batchNo}`);
    }

    await loadData();
  } catch (error) {
    ElMessage.error(error.message || "导入失败");
  } finally {
    importLoading.value = false;
  }

  return false;
}

function handleTemplateDownload() {
  if (mode.value === "grain") {
    downloadGrainTempTemplate();
    return;
  }

  downloadSensorTemplate();
}

function handleRecordPageChange(page) {
  currentRecordPageState.value.pageNum = page;
  loadData();
}

function handleRecordPageSizeChange(size) {
  currentRecordPageState.value.pageSize = size;
  currentRecordPageState.value.pageNum = 1;
  loadData();
}

async function handleSearch() {
  currentRecordPageState.value.pageNum = 1;
  if (mode.value === "grain") {
    await loadGrainRecordFilterOptions();
  }

  await loadData();
}

function applyGrainRecordFilters() {
  grainRecordPageState.pageNum = 1;
  loadData();
}

function resetGrainRecordFilters() {
  grainRecordFilters.zoneCode = "";
  grainRecordFilters.layerNo = null;
  grainRecordFilters.pointNo = null;
  grainRecordFilters.tempMin = null;
  grainRecordFilters.tempMax = null;
  grainCollectedRange.value = null;
  grainRecordKeyword.value = "";
  grainRecordPageState.pageNum = 1;
  loadData();
}

async function handleModeChange() {
  editingId.value = null;

  if (mode.value === "env") {
    filters.metricCode = getInitialEnvMetricCode();
    resetEnvForm();
    envRecordPageState.pageNum = 1;
  } else {
    resetGrainForm();
    grainRecordPageState.pageNum = 1;
    await loadGrainRecordFilterOptions();
  }

  await loadData();
}

function scheduleRecordKeywordReload() {
  clearTimeout(recordKeywordTimer);
  recordKeywordTimer = setTimeout(() => {
    if (mode.value === "grain") {
      grainRecordPageState.pageNum = 1;
    } else {
      envRecordPageState.pageNum = 1;
    }

    loadData();
  }, 400);
}

watch(grainRecordKeyword, scheduleRecordKeywordReload);
watch(envRecordKeyword, scheduleRecordKeywordReload);

watch(
  () => filters.warehouseId,
  async () => {
    if (mode.value !== "grain") {
      return;
    }

    await loadGrainRecordFilterOptions();
  }
);

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  if (mode.value === "grain") {
    chart.setOption({
      tooltip: { trigger: "axis" },
      legend: { data: ["整仓平均温度", "最高温"] },
      grid: { left: 32, right: 18, top: 30, bottom: 28 },
      xAxis: {
        type: "category",
        data: filteredGrainSummaries.value.map((item) => formatDateTime(item.collectedAt))
      },
      yAxis: { type: "value" },
      series: [
        {
          name: "整仓平均温度",
          type: "line",
          smooth: true,
          data: filteredGrainSummaries.value.map((item) => item.avgTemp),
          lineStyle: { color: "#0b7a75" },
          itemStyle: { color: "#0b7a75" }
        },
        {
          name: "最高温",
          type: "line",
          smooth: true,
          data: filteredGrainSummaries.value.map((item) => item.maxTemp),
          lineStyle: { color: "#ea580c" },
          itemStyle: { color: "#ea580c" }
        }
      ]
    });
    return;
  }

  chart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 32, right: 18, top: 30, bottom: 28 },
      xAxis: {
        type: "category",
        data: envTrendRows.value.map((item) => formatDateTime(item.time))
      },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: "环境值",
        type: "line",
        smooth: true,
        data: envTrendRows.value.map((item) => item.value),
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
  await loadMetricOptions();
  await loadWarehouses();
  resetGrainForm();
  resetEnvForm();
  await loadGrainRecordFilterOptions();
  await loadData();
});

onBeforeUnmount(() => {
  clearTimeout(recordKeywordTimer);

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
              <el-select v-model="filters.warehouseId" clearable style="width: 180px">
                <el-option
                  v-for="item in warehouses"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item v-if="mode === 'env'" label="指标">
              <el-select v-model="filters.metricCode" style="width: 160px">
                <el-option
                  v-for="item in envMetricOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">数据录入与导入</div>
      </template>

      <div class="toolbar-row">
        <el-button type="primary" @click="openManualDialog">
          {{ mode === "grain" ? "新增粮温记录" : "手工录入" }}
        </el-button>
        <el-upload
          :show-file-list="false"
          :before-upload="handleImportUpload"
          accept=".csv,.xls,.xlsx"
        >
          <el-button :loading="importLoading">
            {{ mode === "grain" ? "导入粮温模板" : "导入环境数据" }}
          </el-button>
        </el-upload>
        <el-button plain @click="handleTemplateDownload">下载导入模板</el-button>
      </div>

      <div class="compact-lines">
        <div v-if="mode === 'grain'">
          粮温固定模板结构：上方填写 warehouseId、collectedAt；中间按 zoneCode + probeCode 分区块填写“层号/点位”矩阵；下方汇总区可留空。
        </div>
        <div v-if="mode === 'grain'">
          当前后端已兼容固定 XLSX 模板，也兼容旧 CSV / 行式 Excel；手工 CRUD 会直接作用于原始测点记录。
        </div>
        <div v-else>
          普通环境模板字段：warehouseId、metricCode、metricValue、collectedAt、remark。
        </div>
      </div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">{{ mode === "grain" ? "粮温汇总趋势图" : "环境趋势图" }}</div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-card v-if="mode === 'grain'" class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">粮温汇总结果</div>
      </template>

      <div class="toolbar-row table-toolbar">
        <el-input
          v-model="grainSummaryKeyword"
          class="table-search-input"
          clearable
          placeholder="搜索仓库、预警等级、温度、时间"
          :prefix-icon="Search"
        />
      </div>

      <div v-if="latestGrainSummary" class="compact-lines summary-latest-hint">
        <div>最新汇总：{{ latestGrainSummary.warehouseName }} / {{ formatDateTime(latestGrainSummary.collectedAt) }}</div>
        <div>预警等级：{{ latestGrainSummary.warningLevel }}，说明：{{ latestGrainSummary.warningMessage || "暂无" }}</div>
      </div>

      <el-table :data="grainSummaryPagination.pagedItems" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="avgTemp" label="整仓均温" />
        <el-table-column prop="maxTemp" label="最高温" />
        <el-table-column prop="minTemp" label="最低温" />
        <el-table-column prop="layer1Avg" label="一层均温" />
        <el-table-column prop="layer2Avg" label="二层均温" />
        <el-table-column prop="layer3Avg" label="三层均温" />
        <el-table-column prop="layer4Avg" label="四层均温" />
        <el-table-column prop="warningLevel" label="预警等级" />
        <el-table-column label="检测时间">
          <template #default="{ row }">
            {{ formatDateTime(row.collectedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="warningMessage" label="预警说明" min-width="180" />
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="grainSummaryPagination.currentPage"
          :page-size="grainSummaryPagination.pageSize"
          :page-sizes="grainSummaryPagination.pageSizes"
          :total="grainSummaryPagination.total"
          @current-change="grainSummaryPagination.handleCurrentChange"
          @size-change="grainSummaryPagination.handleSizeChange"
        />
      </div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">{{ mode === "grain" ? "粮温原始测点记录" : "环境数据记录" }}</div>
      </template>

      <template v-if="mode === 'grain'">
      <el-form class="grain-record-filter-form table-toolbar" label-width="72px" inline>
        <el-form-item label="区域">
          <el-select v-model="grainRecordFilters.zoneCode" clearable placeholder="全部" filterable>
            <el-option
              v-for="z in grainFilterOptions.zoneCodes"
              :key="z"
              :label="z"
              :value="z"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="层号">
          <el-select v-model="grainRecordFilters.layerNo" clearable placeholder="全部">
            <el-option
              v-for="n in grainFilterOptions.layerNos"
              :key="n"
              :label="String(n)"
              :value="n"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="点位">
          <el-select v-model="grainRecordFilters.pointNo" clearable placeholder="全部">
            <el-option
              v-for="n in grainFilterOptions.pointNos"
              :key="n"
              :label="String(n)"
              :value="n"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="温度℃">
          <div class="grain-temp-range">
            <el-input-number
              v-model="grainRecordFilters.tempMin"
              :step="0.1"
              :controls="false"
              placeholder="最低"
            />
            <span class="grain-temp-range-sep">~</span>
            <el-input-number
              v-model="grainRecordFilters.tempMax"
              :step="0.1"
              :controls="false"
              placeholder="最高"
            />
          </div>
        </el-form-item>
        <el-form-item label="采集时间">
          <el-date-picker
            v-model="grainCollectedRange"
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
          <el-input
            v-model="grainRecordKeyword"
            class="table-search-input grain-keyword-input"
            clearable
            placeholder="模糊匹配仓库名、区域、层号、点位等（可选）"
            :prefix-icon="Search"
          />
        </el-form-item>
      </el-form>
      <div class="grain-filter-hint compact-lines">
        仓库以右上方「查询条件」为准；修改区域/层号/点位/温度/时间后请点击「应用筛选」。关键词支持防抖自动查询。
      </div>
      </template>

      <div v-else class="toolbar-row table-toolbar">
        <el-input
          v-model="envRecordKeyword"
          class="table-search-input"
          clearable
          placeholder="搜索仓库、指标名称、指标编码（服务端模糊匹配）"
          :prefix-icon="Search"
        />
      </div>

      <el-table v-if="mode === 'grain'" :data="rows" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="zoneCode" label="区域" width="90" />
        <el-table-column prop="layerNo" label="层号" width="90" />
        <el-table-column prop="pointNo" label="点位" width="90" />
        <el-table-column prop="temperatureValue" label="温度值" />
        <el-table-column label="采集时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.collectedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="rows" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="metricName" label="指标" />
        <el-table-column prop="metricValue" label="数值" />
        <el-table-column label="采集时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.collectedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" />
        <el-table-column prop="qualityFlag" label="质量标记" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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
        <el-form-item label="仓库">
          <el-select v-model="grainForm.warehouseId">
            <el-option
              v-for="item in warehouses"
              :key="item.id"
              :label="item.warehouseName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="区域">
          <el-input v-model="grainForm.zoneCode" placeholder="例如 A" />
        </el-form-item>

        <el-form-item label="层号">
          <el-input-number v-model="grainForm.layerNo" :min="1" :max="8" />
        </el-form-item>

        <el-form-item label="点位">
          <el-input-number v-model="grainForm.pointNo" :min="1" :max="16" />
        </el-form-item>

        <el-form-item label="温度值">
          <el-input-number v-model="grainForm.temperatureValue" :step="0.1" />
        </el-form-item>

        <el-form-item label="测温缆编号">
          <el-input v-model="grainForm.probeCode" placeholder="不填则自动生成" />
        </el-form-item>

        <el-form-item label="采集时间">
          <el-date-picker
            v-model="grainForm.collectedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择采集时间"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="grainForm.remark" placeholder="可选" />
        </el-form-item>
      </el-form>

      <el-form v-else label-position="top" class="form-grid-2">
        <el-form-item label="仓库">
          <el-select v-model="envForm.warehouseId">
            <el-option
              v-for="item in warehouses"
              :key="item.id"
              :label="item.warehouseName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="指标">
          <el-select v-model="envForm.metricCode">
            <el-option
              v-for="item in envMetricOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="采样值">
          <el-input-number v-model="envForm.metricValue" :step="0.1" />
        </el-form-item>

        <el-form-item label="采集时间">
          <el-date-picker
            v-model="envForm.collectedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="不填则默认当前时间"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">
            {{ isEditing ? "保存修改" : "提交数据" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
