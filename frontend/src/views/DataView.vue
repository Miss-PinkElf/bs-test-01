<script setup>
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import {
  createSensorData,
  downloadGrainTempTemplate,
  downloadSensorTemplate,
  fetchGrainTempSummaries,
  fetchMetricOptions,
  fetchSensorData,
  fetchWarehouses,
  importGrainTemp,
  importSensorData
} from "../api/grain";

const chartRef = ref();
const loading = ref(false);
const importLoading = ref(false);
const dialogVisible = ref(false);
const mode = ref("grain");
const warehouses = ref([]);
const rows = ref([]);
const metricOptions = ref([]);
let chart;

const filters = reactive({
  warehouseId: "",
  metricCode: "humidity"
});

const form = reactive({
  warehouseId: "",
  metricCode: "humidity",
  metricValue: 58.2,
  collectedAt: ""
});

const envMetricOptions = computed(() =>
  metricOptions.value.filter((item) => item.value !== "temperature")
);

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getInitialEnvMetricCode() {
  return envMetricOptions.value[0]?.value || "humidity";
}

function resetForm() {
  form.metricCode = getInitialEnvMetricCode();
  form.metricValue = 58.2;
  form.collectedAt = "";

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

function openManualDialog() {
  if (mode.value !== "env") {
    ElMessage.info("粮温主线当前走固定模板导入，不走此处手工单值录入");
    return;
  }

  resetForm();
  dialogVisible.value = true;
}

async function loadMetricOptions() {
  metricOptions.value = await fetchMetricOptions();
  filters.metricCode = getInitialEnvMetricCode();
  form.metricCode = getInitialEnvMetricCode();
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!filters.warehouseId && warehouses.value.length > 0) {
    filters.warehouseId = warehouses.value[0].id;
  }

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

async function loadData() {
  loading.value = true;

  try {
    rows.value = mode.value === "grain"
      ? await fetchGrainTempSummaries({ warehouseId: filters.warehouseId })
      : await fetchSensorData({
          warehouseId: filters.warehouseId,
          metricCode: filters.metricCode
        });
    await nextTick();
    renderChart();
  } finally {
    loading.value = false;
  }
}

async function submit() {
  await createSensorData(form);
  ElMessage.success("环境数据已写入数据库");
  dialogVisible.value = false;
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

function handleModeChange() {
  if (mode.value === "env") {
    filters.metricCode = getInitialEnvMetricCode();
  }
  loadData();
}

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
        data: rows.value.map((item) => formatDateTime(item.collectedAt))
      },
      yAxis: { type: "value" },
      series: [
        {
          name: "整仓平均温度",
          type: "line",
          smooth: true,
          data: rows.value.map((item) => item.avgTemp),
          lineStyle: { color: "#0b7a75" },
          itemStyle: { color: "#0b7a75" }
        },
        {
          name: "最高温",
          type: "line",
          smooth: true,
          data: rows.value.map((item) => item.maxTemp),
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
      data: rows.value.map((item) => formatDateTime(item.collectedAt))
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        name: "环境值",
        type: "line",
        smooth: true,
        data: rows.value.map((item) => item.metricValue),
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
  await loadData();
});

onBeforeUnmount(() => {
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

          <div class="compact-lines" style="margin-top: 12px">
            <div v-if="mode === 'grain'">当前展示粮温汇总结果，支持固定模板导入并自动生成层温与整仓汇总。</div>
            <div v-else>当前展示湿度 / 二氧化碳等普通环境数据，支持手工录入、导入、查询与图表。</div>
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
              <el-button type="primary" @click="loadData">查询</el-button>
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
        <el-button type="primary" @click="openManualDialog">手工录入</el-button>
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
          粮温模板字段：warehouseId、collectedAt、zoneCode、layerNo、pointNo、temperatureValue、probeCode、remark。
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

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">{{ mode === "grain" ? "粮温汇总记录" : "环境数据记录" }}</div>
      </template>

      <el-table v-if="mode === 'grain'" :data="rows" stripe v-loading="loading">
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

      <el-table v-else :data="rows" stripe v-loading="loading">
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="metricName" label="指标" />
        <el-table-column prop="metricValue" label="数值" />
        <el-table-column label="采集时间">
          <template #default="{ row }">
            {{ formatDateTime(row.collectedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源" />
        <el-table-column prop="qualityFlag" label="质量标记" />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="手工录入普通环境数据" width="720px">
      <el-form label-position="top" class="form-grid-2">
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId">
            <el-option
              v-for="item in warehouses"
              :key="item.id"
              :label="item.warehouseName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="指标">
          <el-select v-model="form.metricCode">
            <el-option
              v-for="item in envMetricOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="采样值">
          <el-input-number v-model="form.metricValue" :step="0.1" />
        </el-form-item>

        <el-form-item label="采集时间">
          <el-date-picker
            v-model="form.collectedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="不填则默认当前时间"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">提交数据</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
