<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login } from "../api/grain";
import { useAuthStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const errorMessage = ref("");
const form = reactive({
  username: "admin",
  password: "123456"
});

async function submit() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const user = await login(form);
    authStore.setUser(user);

    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "";
    const resolvedRedirect = redirect ? router.resolve(redirect) : null;
    const canUseRedirect = Boolean(
      resolvedRedirect &&
      resolvedRedirect.matched.length > 0 &&
      (resolvedRedirect.meta.public || authStore.canAccessRoute(resolvedRedirect.meta.allowedRoles))
    );

    router.push(canUseRedirect ? redirect : authStore.defaultRoute);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

function openScreen() {
  router.push("/screen");
}
</script>

<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="login-eyebrow">平台统一入口</div>
      <h1>粮仓环境数据预测管理平台</h1>
      <p>
        系统提供登录鉴权、后台管理和数据大屏入口，便于统一查看粮仓数据、预警信息与预测结果。
      </p>
      <div class="hero-badges">
        <span>登录与角色识别</span>
        <span>环境数据管理</span>
        <span>温度预测闭环</span>
      </div>
    </div>

    <el-card class="login-panel" shadow="never">
      <template #header>
        <div class="login-panel-title">系统登录</div>
      </template>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            @keyup.enter="submit"
          />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="errorMessage"
        type="error"
        :closable="false"
        :title="errorMessage"
        class="login-alert"
      />

      <div class="login-actions">
        <el-button type="primary" :loading="loading" @click="submit">
          {{ loading ? "登录中" : "进入后台" }}
        </el-button>
        <el-button plain @click="openScreen">查看数据大屏</el-button>
      </div>
    </el-card>
  </div>
</template>
