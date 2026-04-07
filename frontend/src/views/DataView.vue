<script setup>
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import {
  createSensorData,
  downloadSensorTemplate,
  fetchMetricOptions,
  fetchSensorData,
  fetchWarehouses,
  importSensorData
} from "../api/grain";

const DEFAULT_METRIC_CODE = "temperature";

const chartRef = ref();
const loading = ref(false);
const importLoading = ref(false);
const dialogVisible = ref(false);
const warehouses = ref([]);
const rows = ref([]);
const metricOptions = ref([]);
let chart;

const filters = reactive({
  warehouseId: "",
  metricCode: DEFAULT_METRIC_CODE
});

const form = reactive({
  warehouseId: "",
  metricCode: DEFAULT_METRIC_CODE,
  metricValue: 24.8,
  collectedAt: ""
});

function formatDateTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

function getInitialMetricCode() {
  return metricOptions.value[0]?.value || DEFAULT_METRIC_CODE;
}

function resetForm() {
  form.metricCode = getInitialMetricCode();
  form.metricValue = 24.8;
  form.collectedAt = "";

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

function openManualDialog() {
  resetForm();
  dialogVisible.value = true;
}

async function loadMetricOptions() {
  metricOptions.value = await fetchMetricOptions();
  filters.metricCode = getInitialMetricCode();
  form.metricCode = getInitialMetricCode();
}

async function loadWarehouses() {
  warehouses.value = await fetchWarehouses();

  if (!form.warehouseId && warehouses.value.length > 0) {
    form.warehouseId = warehouses.value[0].id;
  }
}

async function loadData() {
  loading.value = true;

  try {
    rows.value = await fetchSensorData(filters);
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
    const result = await importSensorData(uploadFile.raw);
    if (result.failedCount > 0) {
      throw new Error(result.errorMessages?.[0] || "导入失败");
    }

    ElMessage.success(`导入成功，批次号：${result.batchNo}`);
    await loadData();
  } catch (error) {
    ElMessage.error(error.message || "导入失败");
  } finally {
    importLoading.value = false;
  }

  return false;
}

function handleTemplateDownload() {
  downloadSensorTemplate();
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

            <el-form-item label="指标">
              <el-select v-model="filters.metricCode" style="width: 160px">
                <el-option
                  v-for="item in metricOptions"
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

      <el-col :xs="24" :xl="10">
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
              <el-button :loading="importLoading">导入 Excel / CSV</el-button>
            </el-upload>
            <el-button plain @click="handleTemplateDownload">下载 CSV 模板</el-button>
          </div>

          <div class="compact-lines">
            <div>支持上传 CSV、XLS、XLSX，导入成功后会自动刷新列表和趋势图。</div>
            <div>模板字段：warehouseId、metricCode、metricValue、collectedAt、remark。</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">环境趋势图</div>
      </template>

      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">环境数据记录</div>
      </template>

      <el-table :data="rows" stripe v-loading="loading">
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

    <el-dialog v-model="dialogVisible" title="手工录入环境数据" width="720px">
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
              v-for="item in metricOptions"
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
