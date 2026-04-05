import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import DashboardView from "../views/DashboardView.vue";
import WarehouseView from "../views/WarehouseView.vue";
import DataView from "../views/DataView.vue";
import PredictionView from "../views/PredictionView.vue";
import { getSession } from "../utils/session";

const routes = [
  { path: "/login", component: LoginView, meta: { title: "登录" } },
  { path: "/", component: DashboardView, meta: { title: "仪表盘" } },
  { path: "/warehouses", component: WarehouseView, meta: { title: "仓库管理" } },
  { path: "/data", component: DataView, meta: { title: "环境数据" } },
  { path: "/prediction", component: PredictionView, meta: { title: "温度预测" } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.path === "/login") {
    return true;
  }

  if (!getSession()) {
    return "/login";
  }

  return true;
});

export default router;
