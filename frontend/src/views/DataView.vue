<script setup>
import * as echarts from "echarts";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import {
  createGrainTempRecord,
  createSensorData,
  deleteGrainTempRecord,
  deleteSensorData,
  downloadGrainTempTemplate,
  downloadSensorTemplate,
  fetchGrainTempRecords,
  fetchGrainTempSummaries,
  fetchMetricOptions,
  fetchSensorData,
  fetchWarehouses,
  importGrainTemp,
  importSensorData,
  updateGrainTempRecord,
  updateSensorData
} from "../api/grain";

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
let chart;

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

const latestGrainSummary = computed(() => grainSummaryRows.value.at(-1) || null);

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

async function loadData() {
  loading.value = true;

  try {
    if (mode.value === "grain") {
      const [summaryList, recordList] = await Promise.all([
        fetchGrainTempSummaries({ warehouseId: filters.warehouseId }),
        fetchGrainTempRecords({ warehouseId: filters.warehouseId })
      ]);
      grainSummaryRows.value = summaryList;
      rows.value = recordList;
    } else {
      grainSummaryRows.value = [];
      rows.value = await fetchSensorData({
        warehouseId: filters.warehouseId,
        metricCode: filters.metricCode
      });
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

async function handleModeChange() {
  editingId.value = null;

  if (mode.value === "env") {
    filters.metricCode = getInitialEnvMetricCode();
    resetEnvForm();
  } else {
    resetGrainForm();
  }

  await loadData();
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
        data: grainSummaryRows.value.map((item) => formatDateTime(item.collectedAt))
      },
      yAxis: { type: "value" },
      series: [
        {
          name: "整仓平均温度",
          type: "line",
          smooth: true,
          data: grainSummaryRows.value.map((item) => item.avgTemp),
          lineStyle: { color: "#0b7a75" },
          itemStyle: { color: "#0b7a75" }
        },
        {
          name: "最高温",
          type: "line",
          smooth: true,
          data: grainSummaryRows.value.map((item) => item.maxTemp),
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
  resetGrainForm();
  resetEnvForm();
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

      <div v-if="latestGrainSummary" class="compact-lines" style="margin-bottom: 12px">
        <div>最新汇总：{{ latestGrainSummary.warehouseName }} / {{ formatDateTime(latestGrainSummary.collectedAt) }}</div>
        <div>预警等级：{{ latestGrainSummary.warningLevel }}，说明：{{ latestGrainSummary.warningMessage || "暂无" }}</div>
      </div>

      <el-table :data="grainSummaryRows" stripe v-loading="loading">
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
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">{{ mode === "grain" ? "粮温原始测点记录" : "环境数据记录" }}</div>
      </template>

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
