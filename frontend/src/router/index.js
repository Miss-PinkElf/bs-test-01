import { createRouter, createWebHistory } from "vue-router";
import ConsoleLayout from "../layout/ConsoleLayout.vue";
import LoginView from "../views/LoginView.vue";
import BigScreenView from "../views/BigScreenView.vue";
import DashboardView from "../views/DashboardView.vue";
import UsersView from "../views/UsersView.vue";
import WarehouseView from "../views/WarehouseView.vue";
import DataView from "../views/DataView.vue";
import PredictionView from "../views/PredictionView.vue";
import { getSession } from "../utils/session";

const routes = [
  {
    path: "/login",
    component: LoginView,
    meta: { title: "登录", public: true }
  },
  {
    path: "/screen",
    component: BigScreenView,
    meta: { title: "展示大屏", public: true }
  },
  {
    path: "/",
    component: ConsoleLayout,
    meta: { requiresAuth: true },
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        component: DashboardView,
        meta: {
          title: "仪表盘",
          description: "查看仓库概况、核心指标与近期预警。"
        }
      },
      {
        path: "users",
        component: UsersView,
        meta: {
          title: "用户管理",
          description: "维护账号、角色与演示权限范围。"
        }
      },
      {
        path: "warehouses",
        component: WarehouseView,
        meta: {
          title: "仓库管理",
          description: "维护粮仓档案并查看当前运行状态。"
        }
      },
      {
        path: "environment",
        component: DataView,
        meta: {
          title: "环境数据",
          description: "录入、筛选并查看环境数据趋势。"
        }
      },
      {
        path: "prediction",
        component: PredictionView,
        meta: {
          title: "温度预测",
          description: "执行温度预测并查看结果归档闭环。"
        }
      }
    ]
  },
  {
    path: "/data",
    redirect: "/environment"
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.path === "/login" && getSession()) {
    return "/dashboard";
  }

  if (to.meta.public) {
    return true;
  }

  if (!getSession()) {
    return {
      path: "/login",
      query: {
        redirect: to.fullPath
      }
    };
  }

  return true;
});

export default router;
