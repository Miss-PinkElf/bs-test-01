<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 左侧导航直接从路由表生成，这样新增页面时只需要维护路由配置，不必再手写菜单。
const navItems = computed(() => router.getRoutes()
  .filter((item) => item.meta?.showInNav)
  .filter((item) => authStore.canAccessRoute(item.meta?.allowedRoles))
  .sort((left, right) => (left.meta?.navOrder || 999) - (right.meta?.navOrder || 999))
  .map((item) => ({
    label: item.meta?.title || item.name || item.path,
    path: item.path
  })));

const pageTitle = computed(() => route.meta.title || "粮仓平台");
const pageSubtitle = computed(
  () => route.meta.description || "集中查看平台数据、预警与业务管理。"
);

function openScreen() {
  router.push("/screen");
}

// 退出时同步清掉前端保存的会话，再回登录页，避免旧身份残留。
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
              <div class="brand-subtitle">管理后台</div>
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
        <div class="aside-tip">快速入口</div>
        <el-button class="screen-entry" plain @click="openScreen">
          数据大屏
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
          <el-button plain @click="openScreen">数据大屏</el-button>
          <el-button type="danger" plain @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="console-main">
        <!-- 主内容不用 el-scrollbar：其与 ECharts 的 ResizeObserver 叠加时可能形成横向尺寸反馈循环 -->
        <div class="console-main-native">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>
