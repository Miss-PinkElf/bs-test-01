export const mockUserSummaryCards = [
  { key: "users", label: "系统用户", value: 12, note: "用于答辩演示的账号规模" },
  { key: "roles", label: "角色类型", value: 3, note: "管理员、仓库管理员、查看者" },
  { key: "active", label: "启用账号", value: 10, note: "当前可登录并参与业务流程" }
];

export const mockUsers = [
  {
    id: 1,
    username: "admin",
    displayName: "系统管理员",
    roleName: "管理员",
    warehouseName: "平台级",
    status: "ACTIVE",
    lastLoginAt: "2026-04-06 09:20:00"
  },
  {
    id: 2,
    username: "manager_a01",
    displayName: "东一区管理员",
    roleName: "仓库管理员",
    warehouseName: "一号平房仓",
    status: "ACTIVE",
    lastLoginAt: "2026-04-06 08:46:00"
  },
  {
    id: 3,
    username: "viewer_demo",
    displayName: "演示查看者",
    roleName: "查看者",
    warehouseName: "平台级",
    status: "ACTIVE",
    lastLoginAt: "2026-04-05 18:10:00"
  }
];

export const mockRoleProfiles = [
  {
    roleCode: "ADMIN",
    roleName: "管理员",
    description: "管理用户、仓库、全部环境数据和预测记录。"
  },
  {
    roleCode: "WAREHOUSE_MANAGER",
    roleName: "仓库管理员",
    description: "维护所属仓库数据并触发预测任务。"
  },
  {
    roleCode: "VIEWER",
    roleName: "查看者",
    description: "只读查看图表、统计与预测结果。"
  }
];

export const mockRecentSensorRecords = [
  {
    id: 1,
    warehouseName: "一号平房仓",
    metricName: "温度",
    metricValue: 25.3,
    collectedAt: "2026-04-06 08:00:00",
    qualityFlag: "NORMAL"
  },
  {
    id: 2,
    warehouseName: "二号平房仓",
    metricName: "湿度",
    metricValue: 61.5,
    collectedAt: "2026-04-06 08:00:00",
    qualityFlag: "ATTENTION"
  }
];

export const mockWarehouseHealth = [
  { warehouseName: "一号平房仓", healthScore: 82, riskLevel: "ATTENTION" },
  { warehouseName: "二号平房仓", healthScore: 76, riskLevel: "WARNING" },
  { warehouseName: "三号立筒仓", healthScore: 68, riskLevel: "MAINTENANCE" }
];

export const mockPredictionArchives = [
  {
    id: 1,
    taskName: "WH-A01 温度预测",
    createdAt: "2026-04-06 09:00:00",
    algorithmName: "线性回归",
    summary: "未来 6 小时峰值预计出现在 14:00 左右。"
  },
  {
    id: 2,
    taskName: "WH-A02 温度预测",
    createdAt: "2026-04-05 17:00:00",
    algorithmName: "加权移动平均",
    summary: "温度波动较大，建议重点关注通风。"
  }
];

export const mockScreenMetrics = [
  { key: "warehouse", label: "在线粮仓", value: "06", note: "当前纳入演示范围的仓库" },
  { key: "sampling", label: "今日采样", value: "386", note: "已录入和可查询的数据量" },
  { key: "warning", label: "重点预警", value: "03", note: "当前需要优先处理的异常" },
  { key: "forecast", label: "已归档预测", value: "18", note: "已留痕的预测任务数量" }
];

export const mockScreenAlerts = [
  "一号平房仓温度在未来 6 小时内存在上涨风险。",
  "二号平房仓湿度波动偏大，建议关注夜间采样。",
  "三号立筒仓处于维护状态，当前不参与自动预警。"
];
