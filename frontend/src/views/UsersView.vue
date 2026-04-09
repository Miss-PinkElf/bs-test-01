<script setup>
import { computed, onMounted, ref } from "vue";
import { fetchRoleOptions, fetchUsers } from "../api/grain";
import { useClientPagination } from "../composables/useClientPagination";

const loading = ref(false);
const users = ref([]);
const roles = ref([]);

const userPagination = useClientPagination(users);

const summaryCards = computed(() => [
  { key: "users", label: "系统用户", value: users.value.length, note: "当前数据库中的用户账号数量" },
  { key: "roles", label: "角色类型", value: roles.value.length, note: "当前系统已配置的角色种类" },
  {
    key: "active",
    label: "启用账号",
    value: users.value.filter((item) => item.status === "ACTIVE").length,
    note: "当前可登录并参与业务流程的账号数量"
  }
]);

function formatRoleNames(roleNames) {
  return roleNames.length > 0 ? roleNames.join(" / ") : "-";
}

// 加载真实用户列表与角色说明，替换原先静态 mock 展示。
async function loadUsersPage() {
  loading.value = true;

  try {
    const [userList, roleList] = await Promise.all([fetchUsers(), fetchRoleOptions()]);
    users.value = userList;
    roles.value = roleList;
  } finally {
    loading.value = false;
  }
}

onMounted(loadUsersPage);
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
            <div class="panel-title">用户列表</div>
          </template>

          <el-table :data="userPagination.pagedItems" stripe v-loading="loading">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="displayName" label="姓名" />
            <el-table-column label="角色">
              <template #default="{ row }">
                {{ formatRoleNames(row.roleNames) }}
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="所属范围" />
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="lastLoginAt" label="最近登录" />
          </el-table>

          <div class="table-pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="userPagination.currentPage"
              :page-size="userPagination.pageSize"
              :page-sizes="userPagination.pageSizes"
              :total="userPagination.total"
              @current-change="userPagination.handleCurrentChange"
              @size-change="userPagination.handleSizeChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <div class="page-stack">
          <el-card
            v-for="item in roles"
            :key="item.roleCode"
            class="panel-card"
            shadow="never"
          >
            <div class="list-card-header">
              <strong>{{ item.roleName }}</strong>
              <el-tag>{{ item.roleCode }}</el-tag>
            </div>
            <div class="list-card-desc">{{ item.roleDesc }}</div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <el-alert
      type="info"
      :closable="false"
      title="当前说明"
      description="用户页已切到真实接口，当前先聚焦真实列表与角色说明展示，新增、编辑和状态切换留待下一轮。"
    />
  </div>
</template>
