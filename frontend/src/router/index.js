import { createRouter, createWebHistory } from "vue-router";
import ConsoleLayout from "../layout/ConsoleLayout.vue";
import LoginView from "../views/LoginView.vue";
import BigScreenView from "../views/BigScreenView.vue";
import DashboardView from "../views/DashboardView.vue";
import UsersView from "../views/UsersView.vue";
import WarehouseView from "../views/WarehouseView.vue";
import DataView from "../views/DataView.vue";
import PredictionView from "../views/PredictionView.vue";
import { canAccessAllowedRoles, resolveDefaultRouteByRoleCodes } from "../stores/auth";
import { getSession } from "../utils/session";

const CONSOLE_ALLOWED_ROLES = ["ADMIN", "WAREHOUSE_MANAGER", "VIEWER"];
const ADMIN_ONLY_ROLES = ["ADMIN"];

// 管理端页面在这里统一注册，路由 meta 既是页面说明，也是导航和权限配置的真相源。
const routes = [
  {
    path: "/login",
    component: LoginView,
    meta: { title: "登录", public: true }
  },
  {
    path: "/screen",
    component: BigScreenView,
    meta: { title: "数据大屏", public: true }
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
          description: "查看仓库概况、核心指标与近期预警。",
          allowedRoles: CONSOLE_ALLOWED_ROLES,
          showInNav: true,
          navOrder: 1
        }
      },
      {
        path: "users",
        component: UsersView,
        meta: {
          title: "用户管理",
          description: "维护账号、角色与演示权限范围。",
          allowedRoles: ADMIN_ONLY_ROLES,
          showInNav: true,
          navOrder: 2
        }
      },
      {
        path: "warehouses",
        component: WarehouseView,
        meta: {
          title: "仓库管理",
          description: "维护粮仓档案并查看当前运行状态。",
          allowedRoles: ADMIN_ONLY_ROLES,
          showInNav: true,
          navOrder: 3
        }
      },
      {
        path: "environment",
        component: DataView,
        meta: {
          title: "环境数据",
          description: "录入、筛选并查看环境数据趋势。",
          allowedRoles: CONSOLE_ALLOWED_ROLES,
          showInNav: true,
          navOrder: 4
        }
      },
      {
        path: "prediction",
        component: PredictionView,
        meta: {
          title: "温度预测",
          description: "执行温度预测并查看结果归档闭环。",
          allowedRoles: CONSOLE_ALLOWED_ROLES,
          showInNav: true,
          navOrder: 5
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

// 路由守卫只做三件事：校验登录态、按角色挑默认首页、拦住越权访问。
router.beforeEach((to) => {
  const session = getSession();
  const defaultRoute = resolveDefaultRouteByRoleCodes(session?.roleCodes);

  if (to.path === "/login" && session) {
    return defaultRoute;
  }

  if (to.meta.public) {
    return true;
  }

  if (!session) {
    return {
      path: "/login",
      query: {
        redirect: to.fullPath
      }
    };
  }

  if (to.path === "/") {
    return defaultRoute;
  }

  if (!canAccessAllowedRoles(session.roleCodes, to.meta.allowedRoles)) {
    return defaultRoute;
  }

  return true;
});

export default router;
