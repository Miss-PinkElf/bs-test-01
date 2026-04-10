<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const navItems = [
  { label: "仪表盘", path: "/dashboard" },
  { label: "用户管理", path: "/users" },
  { label: "仓库管理", path: "/warehouses" },
  { label: "环境数据", path: "/environment" },
  { label: "温度预测", path: "/prediction" }
];

const pageTitle = computed(() => route.meta.title || "粮仓平台");
const pageSubtitle = computed(
  () => route.meta.description || "正式版 Vue 管理端骨架"
);

function openScreen() {
  router.push("/screen");
}

function handleLogout() {
  authStore.logout();
  router.push("/login");
}
</script>

<template>
  <el-container class="console-shell">
    <el-aside width="248px" class="console-aside">
      <el-scrollbar class="aside-scroll">
        <div class="aside-main">
          <div class="brand-panel">
            <div class="brand-mark">粮</div>
            <div>
              <div class="brand-title">粮仓环境数据平台</div>
              <div class="brand-subtitle">Vue 正式实现骨架</div>
            </div>
          </div>

          <el-menu
            class="console-menu"
            :default-active="route.path"
            router
            background-color="transparent"
            text-color="#d7edf3"
            active-text-color="#ffffff"
          >
            <el-menu-item
              v-for="item in navItems"
              :key="item.path"
              :index="item.path"
            >
              {{ item.label }}
            </el-menu-item>
          </el-menu>
        </div>
      </el-scrollbar>

      <div class="aside-footer">
        <div class="aside-tip">答辩演示入口</div>
        <el-button class="screen-entry" plain @click="openScreen">
          展示大屏
        </el-button>
      </div>
    </el-aside>

    <el-container class="console-body">
      <el-header class="console-header">
        <div>
          <div class="page-title">{{ pageTitle }}</div>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>

        <div class="header-actions">
          <div class="user-panel">
            <span>{{ authStore.user?.displayName || "未登录" }}</span>
            <el-tag type="success">{{ authStore.primaryRole }}</el-tag>
          </div>
          <el-button plain @click="openScreen">展示大屏</el-button>
          <el-button type="danger" plain @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="console-main">
        <el-scrollbar class="console-main-scroll">
          <router-view />
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>
