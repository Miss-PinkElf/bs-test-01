<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { createWarehouse, deleteWarehouse, fetchWarehouses, updateWarehouse } from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";

const loading = ref(false);
const dialogVisible = ref(false);
const warehouses = ref([]);
const selectedWarehouseId = ref(null);
const editingWarehouseId = ref(null);
const warehousePagination = useClientPagination(warehouses);
const form = reactive({
  warehouseCode: "",
  warehouseName: "",
  location: "",
  capacityTon: 500,
  managerName: "",
  status: "ACTIVE"
});

const summaryCards = computed(() => [
  { label: "仓库总数", value: warehouses.value.length, note: "当前纳入平台管理的仓库数量" },
  {
    label: "运行中",
    value: warehouses.value.filter((item) => item.status === "ACTIVE").length,
    note: "状态为 ACTIVE 的仓库"
  },
  {
    label: "维护/关注",
    value: warehouses.value.filter((item) => item.status !== "ACTIVE").length,
    note: "需重点关注的仓库"
  }
]);

const selectedWarehouse = computed(() => {
  if (selectedWarehouseId.value != null) {
    const matched = warehouses.value.find((item) => item.id === selectedWarehouseId.value);
    if (matched) {
      return matched;
    }
  }

  return warehouses.value[0] || null;
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

function handleRowClick(row) {
  selectedWarehouseId.value = row.id;
}

async function loadWarehouses() {
  loading.value = true;

  try {
    warehouses.value = await fetchWarehouses();

    if (warehouses.value.length === 0) {
      selectedWarehouseId.value = null;
      return;
    }

    const stillExists = warehouses.value.some((item) => item.id === selectedWarehouseId.value);
    if (!stillExists) {
      selectedWarehouseId.value = warehouses.value[0].id;
    }
  } finally {
    loading.value = false;
  }
}

async function submit() {
  if (editingWarehouseId.value == null) {
    await createWarehouse(form);
    ElMessage.success("仓库已写入数据库");
  } else {
    await updateWarehouse(editingWarehouseId.value, form);
    selectedWarehouseId.value = editingWarehouseId.value;
    ElMessage.success("仓库已更新");
  }

  dialogVisible.value = false;
  resetForm();
  await loadWarehouses();
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除仓库“${row.warehouseName}”吗？`, "确认删除", {
    type: "warning"
  });

  await deleteWarehouse(row.id);
  ElMessage.success("仓库已删除");
  await loadWarehouses();
}

onMounted(loadWarehouses);
</script>

<template>
  <div class="page-stack">
    <div class="metrics-grid">
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

      <el-table
        :data="warehousePagination.pagedItems"
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
          :current-page="warehousePagination.currentPage"
          :page-size="warehousePagination.pageSize"
          :page-sizes="warehousePagination.pageSizes"
          :total="warehousePagination.total"
          @current-change="warehousePagination.handleCurrentChange"
          @size-change="warehousePagination.handleSizeChange"
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
