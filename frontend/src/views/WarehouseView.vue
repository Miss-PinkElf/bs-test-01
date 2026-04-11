<script setup>
import { Search } from "@element-plus/icons-vue";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createWarehouse,
  deleteWarehouse,
  fetchWarehousePage,
  fetchWarehouseStats,
  updateWarehouse
} from "../api/grain";

const loading = ref(false);
const statsLoading = ref(false);
const dialogVisible = ref(false);
const warehouses = ref([]);
const warehouseStats = ref({ totalCount: 0, activeCount: 0, nonActiveCount: 0 });
const selectedWarehouseId = ref(null);
const selectedWarehouseSnapshot = ref(null);
const editingWarehouseId = ref(null);
const warehouseTableKeyword = ref("");
const warehousePageState = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

watch(warehouseTableKeyword, async () => {
  warehousePageState.pageNum = 1;
  await loadWarehousePage();
});

const form = reactive({
  warehouseCode: "",
  warehouseName: "",
  location: "",
  capacityTon: 500,
  managerName: "",
  status: "ACTIVE"
});

const summaryCards = computed(() => [
  { label: "仓库总数", value: warehouseStats.value.totalCount, note: "当前纳入平台管理的仓库数量" },
  {
    label: "运行中",
    value: warehouseStats.value.activeCount,
    note: "状态为 ACTIVE 的仓库"
  },
  {
    label: "维护/关注",
    value: warehouseStats.value.nonActiveCount,
    note: "需重点关注的仓库"
  }
]);

const selectedWarehouse = computed(() => {
  const matched = warehouses.value.find((item) => item.id === selectedWarehouseId.value);
  return matched || selectedWarehouseSnapshot.value || null;
});

const dialogTitle = computed(() => (editingWarehouseId.value == null ? "新增仓库" : "编辑仓库"));
const submitButtonText = computed(() => (editingWarehouseId.value == null ? "保存仓库" : "保存修改"));

function resetForm() {
  form.warehouseCode = "";
  form.warehouseName = "";
  form.location = "";
  form.capacityTon = 500;
  form.managerName = "";
  form.status = "ACTIVE";
  editingWarehouseId.value = null;
}

function openCreateDialog() {
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row) {
  editingWarehouseId.value = row.id;
  form.warehouseCode = row.warehouseCode;
  form.warehouseName = row.warehouseName;
  form.location = row.location;
  form.capacityTon = row.capacityTon;
  form.managerName = row.managerName;
  form.status = row.status;
  dialogVisible.value = true;
}

function setSelectedWarehouse(row) {
  selectedWarehouseId.value = row?.id ?? null;
  selectedWarehouseSnapshot.value = row || null;
}

function handleRowClick(row) {
  setSelectedWarehouse(row);
}

// 翻页后优先复用当前选中 id，当前页找不到时才退回快照或第一页，保证列表驱动详情不轻易跳变。
function syncSelectedWarehouse({ deletedId = null, preferredId = null } = {}) {
  if (warehouses.value.length === 0) {
    if (deletedId != null && deletedId === selectedWarehouseId.value) {
      selectedWarehouseId.value = null;
      selectedWarehouseSnapshot.value = null;
    }
    if (selectedWarehouseId.value == null) {
      selectedWarehouseSnapshot.value = null;
    }
    return;
  }

  const targetId = preferredId ?? selectedWarehouseId.value;
  const matched = targetId != null ? warehouses.value.find((item) => item.id === targetId) : null;
  if (matched) {
    setSelectedWarehouse(matched);
    return;
  }

  if (deletedId != null && deletedId === targetId) {
    setSelectedWarehouse(warehouses.value[0]);
    return;
  }

  if (targetId == null || !selectedWarehouseSnapshot.value) {
    setSelectedWarehouse(warehouses.value[0]);
  }
}

async function loadWarehouseStats() {
  statsLoading.value = true;

  try {
    warehouseStats.value = await fetchWarehouseStats();
  } catch (error) {
    ElMessage.error(error?.message || "仓库统计加载失败");
  } finally {
    statsLoading.value = false;
  }
}

async function loadWarehousePage(options = {}) {
  loading.value = true;

  try {
    const page = await fetchWarehousePage({
      keyword: warehouseTableKeyword.value.trim() || undefined,
      pageNum: warehousePageState.pageNum,
      pageSize: warehousePageState.pageSize
    });
    warehouses.value = page.list;
    warehousePageState.pageNum = page.pageNum;
    warehousePageState.pageSize = page.pageSize;
    warehousePageState.total = page.total;
    syncSelectedWarehouse(options);
  } catch (error) {
    ElMessage.error(error?.message || "仓库列表加载失败");
  } finally {
    loading.value = false;
  }
}

async function refreshWarehouseData(options = {}) {
  // 统计卡片和分页列表并行刷新，但选中态仍由列表返回结果统一兜底。
  await Promise.all([loadWarehouseStats(), loadWarehousePage(options)]);
}

async function handleWarehousePageChange(pageNum) {
  warehousePageState.pageNum = pageNum;
  await loadWarehousePage();
}

async function handleWarehousePageSizeChange(pageSize) {
  warehousePageState.pageSize = pageSize;
  warehousePageState.pageNum = 1;
  await loadWarehousePage();
}

async function submit() {
  try {
    if (editingWarehouseId.value == null) {
      await createWarehouse(form);
      ElMessage.success("仓库已写入数据库");
      dialogVisible.value = false;
      resetForm();
      await refreshWarehouseData();
      return;
    }

    await updateWarehouse(editingWarehouseId.value, form);
    ElMessage.success("仓库已更新");
    dialogVisible.value = false;
    const preferredId = editingWarehouseId.value;
    resetForm();
    await refreshWarehouseData({ preferredId });
  } catch (error) {
    ElMessage.error(error?.message || "仓库保存失败");
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除仓库“${row.warehouseName}”吗？`, "确认删除", {
      type: "warning"
    });
  } catch {
    return;
  }

  try {
    await deleteWarehouse(row.id);
    ElMessage.success("仓库已删除");
    await refreshWarehouseData({ deletedId: row.id });
  } catch (error) {
    ElMessage.error(error?.message || "仓库删除失败");
  }
}

onMounted(async () => {
  await refreshWarehouseData();
});
</script>

<template>
  <div class="page-stack">
    <div class="metrics-grid" v-loading="statsLoading">
      <el-card
        v-for="item in summaryCards"
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
      <el-col :xs="24" :xl="16">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div class="panel-title">仓库档案维护</div>
              <el-button type="primary" @click="openCreateDialog">新增仓库</el-button>
            </div>
          </template>

          <div class="compact-lines">
            <div>手动新增仓库已收口为弹出框，便于保持列表页更整洁。</div>
            <div>新增完成后会自动刷新当前仓库列表与概览卡片。</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="8">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">当前选中仓库</div>
          </template>

          <div v-if="selectedWarehouse" class="detail-grid">
            <div><strong>编码：</strong>{{ selectedWarehouse.warehouseCode }}</div>
            <div><strong>名称：</strong>{{ selectedWarehouse.warehouseName }}</div>
            <div><strong>位置：</strong>{{ selectedWarehouse.location }}</div>
            <div><strong>容量：</strong>{{ selectedWarehouse.capacityTon }} 吨</div>
            <div><strong>负责人：</strong>{{ selectedWarehouse.managerName }}</div>
            <div><strong>状态：</strong>{{ selectedWarehouse.status }}</div>
          </div>

          <el-empty v-else description="暂无仓库数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">仓库档案列表</div>
      </template>

      <div class="toolbar-row table-toolbar">
        <el-input
          v-model="warehouseTableKeyword"
          class="table-search-input"
          clearable
          placeholder="搜索编码、名称、位置、负责人、状态"
          :prefix-icon="Search"
        />
      </div>

      <el-table
        :data="warehouses"
        stripe
        v-loading="loading"
        highlight-current-row
        @row-click="handleRowClick"
      >
        <el-table-column prop="warehouseCode" label="仓库编码" />
        <el-table-column prop="warehouseName" label="仓库名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacityTon" label="容量（吨）" />
        <el-table-column prop="managerName" label="负责人" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click.stop="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="warehousePageState.pageNum"
          :page-size="warehousePageState.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="warehousePageState.total"
          @current-change="handleWarehousePageChange"
          @size-change="handleWarehousePageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @closed="resetForm">
      <el-form label-position="top" class="form-grid-2">
        <el-form-item label="仓库编码">
          <el-input v-model="form.warehouseCode" placeholder="例如 WH-A03" />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库位置">
          <el-input v-model="form.location" placeholder="请输入仓库位置" />
        </el-form-item>
        <el-form-item label="容量（吨）">
          <el-input-number v-model="form.capacityTon" :min="1" :step="10" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.managerName" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="运行中" value="ACTIVE" />
            <el-option label="关注" value="WARNING" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">{{ submitButtonText }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
