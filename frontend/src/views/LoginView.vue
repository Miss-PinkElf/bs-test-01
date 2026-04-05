<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "../api/grain";
import { saveSession } from "../utils/session";

const router = useRouter();
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
    saveSession(user);
    router.push("/");
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-card">
    <div class="section-title">粮仓环境数据预测管理平台</div>
    <p class="section-hint">使用演示账号登录后即可查看仪表盘、数据与预测页面。</p>
    <div class="form-grid">
      <label>
        <span>用户名</span>
        <input v-model="form.username" class="input" />
      </label>
      <label>
        <span>密码</span>
        <input v-model="form.password" class="input" type="password" />
      </label>
    </div>
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <button class="primary-btn" :disabled="loading" @click="submit">
      {{ loading ? "登录中..." : "登录" }}
    </button>
  </div>
</template>
