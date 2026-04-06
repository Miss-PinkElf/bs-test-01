<script setup>
import {
  mockRoleProfiles,
  mockUserSummaryCards,
  mockUsers
} from "../mock/platform";
</script>

<template>
  <div class="page-stack">
    <div class="metrics-grid">
      <el-card
        v-for="item in mockUserSummaryCards"
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

          <el-table :data="mockUsers" stripe>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="displayName" label="姓名" />
            <el-table-column prop="roleName" label="角色" />
            <el-table-column prop="warehouseName" label="所属范围" />
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="lastLoginAt" label="最近登录" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <div class="page-stack">
          <el-card
            v-for="item in mockRoleProfiles"
            :key="item.roleCode"
            class="panel-card"
            shadow="never"
          >
            <div class="list-card-header">
              <strong>{{ item.roleName }}</strong>
              <el-tag>{{ item.roleCode }}</el-tag>
            </div>
            <div class="list-card-desc">{{ item.description }}</div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <el-alert
      type="info"
      :closable="false"
      title="当前说明"
      description="用户页已经接入正式路由结构，后续待后端用户接口补齐后，当前静态表格会直接替换为真实 API 数据。"
    />
  </div>
</template>
