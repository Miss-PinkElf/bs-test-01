<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { clearSession, getSession } from "./utils/session";

const route = useRoute();
const router = useRouter();

const isLoginPage = computed(() => route.path === "/login");
const user = computed(() => getSession());

const navItems = [
  { label: "仪表盘", path: "/" },
  { label: "仓库管理", path: "/warehouses" },
  { label: "环境数据", path: "/data" },
  { label: "温度预测", path: "/prediction" }
];

function logout() {
  clearSession();
  router.push("/login");
}
</script>

<template>
  <div v-if="isLoginPage" class="login-shell">
    <router-view />
  </div>
  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-title">粮仓平台</div>
        <div class="brand-subtitle">数据预测管理</div>
      </div>
      <nav class="nav-list">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-link"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <main class="main-panel">
      <header class="topbar">
        <div>
          <div class="page-title">{{ route.meta.title }}</div>
          <div class="page-subtitle">毕业设计 MVP 演示平台</div>
        </div>
        <div class="user-box">
          <span>{{ user?.displayName || "未登录" }}</span>
          <button class="ghost-btn" @click="logout">退出</button>
        </div>
      </header>
      <section class="content-panel">
        <router-view />
      </section>
    </main>
  </div>
</template>
