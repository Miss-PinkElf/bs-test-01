<script setup>
import { Search } from "@element-plus/icons-vue";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createUser,
  deleteUser,
  fetchRoleOptions,
  fetchUserStats,
  fetchUsersPage,
  fetchWarehouses,
  resetUserPassword,
  updateUser
} from "../api/grain";
import { filterRows } from "../utils/fuzzyText";

const loading = ref(false);
const metaLoading = ref(false);
const dialogVisible = ref(false);
const passwordDialogVisible = ref(false);
const users = ref([]);
const roles = ref([]);
const warehouses = ref([]);
const userStats = ref({ totalUsers: 0, activeUsers: 0 });
const editingUserId = ref(null);
const passwordTarget = ref(null);
const userTableKeyword = ref("");
const roleTableKeyword = ref("");
const userPageState = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const filteredRoles = computed(() =>
  filterRows(roles.value, roleTableKeyword.value, (row) => [row.roleName, row.roleCode, row.roleDesc])
);

watch(userTableKeyword, async () => {
  userPageState.pageNum = 1;
  await loadUsersPage();
});

const userForm = reactive({
  username: "",
  password: "",
  displayName: "",
  phone: "",
  warehouseId: null,
  status: "ACTIVE",
  roleCodes: []
});

const passwordForm = reactive({
  newPassword: ""
});

const summaryCards = computed(() => [
  { key: "users", label: "系统用户", value: userStats.value.totalUsers, note: "当前数据库中的用户账号数量" },
  { key: "roles", label: "角色类型", value: roles.value.length, note: "当前系统已配置的角色种类" },
  {
    key: "active",
    label: "启用账号",
    value: userStats.value.activeUsers,
    note: "当前可登录并参与业务流程的账号数量"
  }
]);

const dialogTitle = computed(() => (editingUserId.value == null ? "新增用户" : "编辑用户"));
const submitButtonText = computed(() => (editingUserId.value == null ? "保存用户" : "保存修改"));

function formatRoleNames(roleNames) {
  return roleNames.length > 0 ? roleNames.join(" / ") : "-";
}

function formatStatus(status) {
  if (status === "ACTIVE") {
    return "启用";
  }

  if (status === "DISABLED") {
    return "停用";
  }

  return status || "-";
}

function resetUserForm() {
  userForm.username = "";
  userForm.password = "";
  userForm.displayName = "";
  userForm.phone = "";
  userForm.warehouseId = null;
  userForm.status = "ACTIVE";
  userForm.roleCodes = [];
  editingUserId.value = null;
}

function resetPasswordForm() {
  passwordForm.newPassword = "";
  passwordTarget.value = null;
}

function openCreateDialog() {
  resetUserForm();
  dialogVisible.value = true;
}

function openEditDialog(row) {
  editingUserId.value = row.id;
  userForm.username = row.username;
  userForm.password = "";
  userForm.displayName = row.displayName;
  userForm.phone = row.phone || "";
  userForm.warehouseId = row.warehouseId ?? null;
  userForm.status = row.status || "ACTIVE";
  userForm.roleCodes = [...row.roleCodes];
  dialogVisible.value = true;
}

function openPasswordDialog(row) {
  passwordTarget.value = row;
  passwordForm.newPassword = "";
  passwordDialogVisible.value = true;
}

function validateUserForm() {
  if (!userForm.username.trim()) {
    ElMessage.warning("请输入用户名");
    return false;
  }

  if (editingUserId.value == null && !userForm.password.trim()) {
    ElMessage.warning("请输入初始密码");
    return false;
  }

  if (!userForm.displayName.trim()) {
    ElMessage.warning("请输入姓名");
    return false;
  }

  if (userForm.roleCodes.length === 0) {
    ElMessage.warning("请至少选择一个角色");
    return false;
  }

  return true;
}

function buildUserPayload() {
  return {
    username: userForm.username.trim(),
    password: userForm.password.trim(),
    displayName: userForm.displayName.trim(),
    phone: userForm.phone.trim(),
    warehouseId: userForm.warehouseId,
    status: userForm.status,
    roleCodes: userForm.roleCodes
  };
}

async function loadMeta() {
  metaLoading.value = true;

  try {
    const [roleList, warehouseList, stats] = await Promise.all([
      fetchRoleOptions(),
      fetchWarehouses(),
      fetchUserStats()
    ]);
    roles.value = roleList;
    warehouses.value = warehouseList;
    userStats.value = stats;
  } catch (error) {
    ElMessage.error(error?.message || "用户基础数据加载失败");
  } finally {
    metaLoading.value = false;
  }
}

async function loadUsersPage() {
  loading.value = true;

  try {
    const page = await fetchUsersPage({
      keyword: userTableKeyword.value.trim() || undefined,
      pageNum: userPageState.pageNum,
      pageSize: userPageState.pageSize
    });
    users.value = page.list;
    userPageState.pageNum = page.pageNum;
    userPageState.pageSize = page.pageSize;
    userPageState.total = page.total;
  } catch (error) {
    ElMessage.error(error?.message || "用户列表加载失败");
  } finally {
    loading.value = false;
  }
}

async function refreshUsersAndStats() {
  await Promise.all([loadUsersPage(), loadMeta()]);
}

async function handleUserPageChange(pageNum) {
  userPageState.pageNum = pageNum;
  await loadUsersPage();
}

async function handleUserPageSizeChange(pageSize) {
  userPageState.pageSize = pageSize;
  userPageState.pageNum = 1;
  await loadUsersPage();
}

async function submitUser() {
  if (!validateUserForm()) {
    return;
  }

  const payload = buildUserPayload();

  try {
    if (editingUserId.value == null) {
      await createUser(payload);
      ElMessage.success("用户已写入数据库");
    } else {
      await updateUser(editingUserId.value, payload);
      ElMessage.success("用户已更新");
    }

    dialogVisible.value = false;
    resetUserForm();
    await refreshUsersAndStats();
  } catch (error) {
    ElMessage.error(error?.message || "用户保存失败");
  }
}

async function submitPasswordReset() {
  if (!passwordForm.newPassword.trim()) {
    ElMessage.warning("请输入新密码");
    return;
  }

  try {
    await resetUserPassword(passwordTarget.value.id, passwordForm.newPassword.trim());
    ElMessage.success("密码已重置");
    passwordDialogVisible.value = false;
    resetPasswordForm();
  } catch (error) {
    ElMessage.error(error?.message || "密码重置失败");
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户“${row.displayName}”吗？`, "确认删除", {
      type: "warning"
    });
  } catch {
    return;
  }

  try {
    await deleteUser(row.id);
    ElMessage.success("用户已删除");
    await refreshUsersAndStats();
  } catch (error) {
    ElMessage.error(error?.message || "用户删除失败");
  }
}

onMounted(async () => {
  await Promise.all([loadMeta(), loadUsersPage()]);
});
</script>

<template>
  <div class="page-stack">
    <div class="metrics-grid">
      <el-card
        v-for="item in summaryCards"
        :key="item.key"
        class="metric-card"
        shadow="never"
      >
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-note">{{ item.note }}</div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div class="panel-title">用户列表</div>
              <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
            </div>
          </template>

          <div class="toolbar-row table-toolbar">
            <el-input
              v-model="userTableKeyword"
              class="table-search-input"
              clearable
              placeholder="搜索用户名、姓名、手机号、角色、所属范围、状态"
              :prefix-icon="Search"
            />
          </div>

          <el-table :data="users" stripe v-loading="loading">
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="displayName" label="姓名" min-width="120" />
            <el-table-column prop="phone" label="手机号" min-width="140" />
            <el-table-column label="角色" min-width="180">
              <template #default="{ row }">
                {{ formatRoleNames(row.roleNames) }}
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="所属范围" min-width="120" />
            <el-table-column label="状态" min-width="90">
              <template #default="{ row }">
                {{ formatStatus(row.status) }}
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginAt" label="最近登录" min-width="170" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click.stop="openEditDialog(row)">编辑</el-button>
                  <el-button link type="warning" @click.stop="openPasswordDialog(row)">重置密码</el-button>
                  <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="userPageState.pageNum"
              :page-size="userPageState.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="userPageState.total"
              @current-change="handleUserPageChange"
              @size-change="handleUserPageSizeChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div class="panel-title">角色说明</div>
            </div>
          </template>

          <div class="toolbar-row table-toolbar">
            <el-input
              v-model="roleTableKeyword"
              class="table-search-input"
              clearable
              placeholder="搜索角色名称、编码、说明"
              :prefix-icon="Search"
            />
          </div>

          <el-table :data="filteredRoles" stripe border v-loading="metaLoading">
            <el-table-column prop="roleName" label="角色名称" min-width="110" />
            <el-table-column label="角色编码" min-width="120">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.roleCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="roleDesc" label="说明" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" @closed="resetUserForm">
      <el-form label-position="top" class="form-grid-2">
        <el-form-item label="用户名">
          <el-input
            v-model="userForm.username"
            :disabled="editingUserId != null"
            placeholder="请输入登录用户名"
          />
        </el-form-item>
        <el-form-item v-if="editingUserId == null" label="初始密码">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            placeholder="请输入初始密码"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.displayName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号，可留空" />
        </el-form-item>
        <el-form-item label="所属仓库">
          <el-select v-model="userForm.warehouseId" clearable placeholder="留空表示平台级账号">
            <el-option
              v-for="item in warehouses"
              :key="item.id"
              :label="`${item.warehouseName}（${item.warehouseCode}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" class="full-span">
          <el-select v-model="userForm.roleCodes" multiple collapse-tags collapse-tags-tooltip>
            <el-option
              v-for="item in roles"
              :key="item.roleCode"
              :label="`${item.roleName}（${item.roleCode}）`"
              :value="item.roleCode"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUser">{{ submitButtonText }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="420px"
      @closed="resetPasswordForm"
    >
      <div class="page-stack">
        <div>
          当前用户：<strong>{{ passwordTarget?.displayName || "-" }}</strong>
          <span class="muted-inline">（{{ passwordTarget?.username || "-" }}）</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="新密码">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordReset">确认重置</el-button>
        </div>
      </template>
    </el-dialog>

    <el-alert
      type="info"
      :closable="false"
      title="提示"
      description="用户列表支持新增、编辑、删除与重置密码；右侧为各角色编码与权限说明，分配角色时请结合业务需要选择。"
    />
  </div>
</template>
