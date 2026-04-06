export type DashboardCard = {
  key: string;
  label: string;
  value: number | string;
  suffix?: string;
  note: string;
};

export type TrendPoint = {
  label: string;
  value: number;
};

export type TrendSeries = {
  key: string;
  label: string;
  color: string;
  dashed?: boolean;
  points: TrendPoint[];
};

export type RankingItem = {
  key: string;
  label: string;
  value: number;
  suffix?: string;
  note: string;
  color: string;
};

type ProcessStage = {
  title: string;
  description: string;
  status: "done" | "active" | "next";
};

type WarehouseRecord = {
  id: number;
  code: string;
  name: string;
  location: string;
  capacityTon: number;
  utilizationRate: number;
  managerName: string;
  latestTemperature: string;
  latestHumidity: string;
  statusLabel: string;
  statusColor: string;
};

type EnvironmentRecord = {
  id: number;
  warehouseName: string;
  metricType: string;
  metricTypeLabel: string;
  metricValue: number;
  metricValueLabel: string;
  collectedAt: string;
  sourceLabel: string;
  statusLabel: string;
  statusColor: string;
};

type ForecastSummaryItem = {
  time: string;
  temperature: string;
  statusColor: string;
  percent: number;
};

type PredictionRecord = {
  time: string;
  actualValue: string;
  predictedValue: string;
  confidence: string;
  statusLabel: string;
  statusColor: string;
};

type UserRecord = {
  id: number;
  username: string;
  displayName: string;
  roleLabel: string;
  warehouseName: string;
  statusLabel: string;
  statusColor: string;
  lastLoginAt: string;
};

type RoleProfile = {
  roleCode: string;
  roleLabel: string;
  description: string;
  permissions: string[];
};

type PermissionMatrixRow = {
  module: string;
  admin: string;
  warehouseManager: string;
  viewer: string;
};

type ArchiveRecord = {
  id: number;
  taskName: string;
  createdAt: string;
  algorithmName: string;
  summary: string;
};

type ScreenMetricCard = {
  key: string;
  label: string;
  value: string;
  note: string;
};

type ScreenAlert = {
  title: string;
  level: string;
  description: string;
};

type ScreenWarehousePanel = {
  warehouseName: string;
  temperature: string;
  humidity: string;
  trend: string;
  riskLabel: string;
};

type ScreenPredictionBoardItem = {
  warehouseName: string;
  nextPeakTime: string;
  peakValue: string;
  action: string;
};

export const MOCK_CURRENT_USER = {
  username: "admin",
  displayName: "毕业设计演示账号",
  roleLabel: "管理员"
};

export const dashboardCards: DashboardCard[] = [
  { key: "warehouse", label: "在线粮仓", value: 6, note: "纳入本次毕业设计演示范围的粮仓数量" },
  { key: "records", label: "今日采样", value: 386, note: "展示录入、查询、图表和预测所需的核心数据量" },
  { key: "warning", label: "待处理预警", value: 3, note: "高温、高湿、维护状态三类提醒" },
  { key: "forecast", label: "已归档预测", value: 18, note: "静态模拟预测执行与归档后的留痕结果" }
];

export const processStages: ProcessStage[] = [
  {
    title: "登录与权限识别",
    description: "用户进入系统后获得角色信息和对应菜单能力。",
    status: "done"
  },
  {
    title: "仓库与环境数据管理",
    description: "完成粮仓档案维护、环境数据录入、导入与查询。",
    status: "done"
  },
  {
    title: "图表分析与温度预测",
    description: "展示历史趋势，执行短期预测并输出风险提示。",
    status: "active"
  },
  {
    title: "预测结果归档",
    description: "把预测任务结果保存下来，形成历史记录。",
    status: "next"
  }
];

export const dashboardHighlights = [
  "正式版将由 Vue 3 + Element Plus 实现，这里只负责把最终成品感提前看清。",
  "核心展示链路严格对齐 PRD：仪表盘、用户、仓库、环境数据、温度预测。",
  "不扩展到机器人、硬件接入、数字孪生或复杂深度学习，只做可演示闭环。"
];

export const dashboardTrendSeries: TrendSeries[] = [
  {
    key: "sampling",
    label: "采样条数",
    color: "#2f6fed",
    points: [
      { label: "08:00", value: 42 },
      { label: "10:00", value: 56 },
      { label: "12:00", value: 61 },
      { label: "14:00", value: 58 },
      { label: "16:00", value: 72 },
      { label: "18:00", value: 63 }
    ]
  },
  {
    key: "alerts",
    label: "预警关联条数",
    color: "#f59e0b",
    points: [
      { label: "08:00", value: 8 },
      { label: "10:00", value: 12 },
      { label: "12:00", value: 14 },
      { label: "14:00", value: 11 },
      { label: "16:00", value: 18 },
      { label: "18:00", value: 13 }
    ]
  }
];

export const warehouseHealthRanking: RankingItem[] = [
  { key: "wh-a01", label: "一号粮仓", value: 82, suffix: "分", note: "温度略高，建议提前通风", color: "#2f6fed" },
  { key: "wh-b02", label: "二号粮仓", value: 76, suffix: "分", note: "湿度波动较大，需要关注夜间采样", color: "#0f9d7a" },
  { key: "wh-c03", label: "三号粮仓", value: 68, suffix: "分", note: "当前维护中，展示但不参与自动判断", color: "#f59e0b" },
  { key: "wh-d04", label: "四号粮仓", value: 88, suffix: "分", note: "温湿度稳定，状态良好", color: "#8b5cf6" }
];

export const warehouseAlerts = [
  {
    title: "一号粮仓温度接近预警阈值",
    level: "高",
    description: "近 6 小时温度连续上涨，预测 16:00 前后可能突破 27 °C。"
  },
  {
    title: "二号粮仓湿度波动偏大",
    level: "中",
    description: "夜间湿度区间震荡明显，建议复核通风策略和采样稳定性。"
  },
  {
    title: "三号粮仓维护中",
    level: "中",
    description: "维护期数据参与展示，但默认不纳入自动预警判断。"
  }
];

export const warehouseRecords: WarehouseRecord[] = [
  {
    id: 1,
    code: "WH-A01",
    name: "一号粮仓",
    location: "北区 1 栋",
    capacityTon: 500,
    utilizationRate: 76,
    managerName: "努尔夏提",
    latestTemperature: "25.3 °C",
    latestHumidity: "56.2 %",
    statusLabel: "运行中",
    statusColor: "green"
  },
  {
    id: 2,
    code: "WH-B02",
    name: "二号粮仓",
    location: "北区 2 栋",
    capacityTon: 650,
    utilizationRate: 71,
    managerName: "艾力江",
    latestTemperature: "26.1 °C",
    latestHumidity: "61.5 %",
    statusLabel: "关注",
    statusColor: "orange"
  },
  {
    id: 3,
    code: "WH-C03",
    name: "三号粮仓",
    location: "南区 1 栋",
    capacityTon: 720,
    utilizationRate: 64,
    managerName: "阿依古丽",
    latestTemperature: "24.2 °C",
    latestHumidity: "54.8 %",
    statusLabel: "维护中",
    statusColor: "blue"
  },
  {
    id: 4,
    code: "WH-D04",
    name: "四号粮仓",
    location: "南区 2 栋",
    capacityTon: 540,
    utilizationRate: 82,
    managerName: "海日古丽",
    latestTemperature: "23.9 °C",
    latestHumidity: "52.4 %",
    statusLabel: "运行中",
    statusColor: "green"
  }
];

export const warehouseSummaryCards = [
  { key: "count", label: "仓库总数", value: "6 个", note: "后台正式版会支持分页、状态筛选和详情查看" },
  { key: "capacity", label: "总容量", value: "3,860 吨", note: "静态原型用汇总卡片模拟答辩时的一屏概览" },
  { key: "coverage", label: "采样覆盖率", value: "96%", note: "用来体现数据质量和管理能力" }
];

export const warehouseCapacityRanking: RankingItem[] = warehouseRecords.map((record) => ({
  key: record.code,
  label: record.name,
  value: record.utilizationRate,
  suffix: "%",
  note: `${record.capacityTon} 吨容量，负责人 ${record.managerName}`,
  color: record.code === "WH-B02" ? "#f59e0b" : "#2f6fed"
}));

export const warehouseInspectionItems = [
  "一号粮仓 16:00 前执行通风，复核高温预测。",
  "二号粮仓夜间增加一次湿度采样，确认波动是否持续。",
  "三号粮仓维护结束后重新纳入自动判断。",
  "四号粮仓保持常规巡检节奏，作为稳定样本对照。"
];

export const environmentRows: EnvironmentRecord[] = [
  {
    id: 1,
    warehouseName: "一号粮仓",
    metricType: "temperature",
    metricTypeLabel: "温度",
    metricValue: 24.8,
    metricValueLabel: "24.8 °C",
    collectedAt: "2026-04-05 09:00",
    sourceLabel: "人工录入",
    statusLabel: "正常",
    statusColor: "green"
  },
  {
    id: 2,
    warehouseName: "一号粮仓",
    metricType: "humidity",
    metricTypeLabel: "湿度",
    metricValue: 56.2,
    metricValueLabel: "56.2 %",
    collectedAt: "2026-04-05 09:00",
    sourceLabel: "人工录入",
    statusLabel: "正常",
    statusColor: "green"
  },
  {
    id: 3,
    warehouseName: "二号粮仓",
    metricType: "temperature",
    metricTypeLabel: "温度",
    metricValue: 26.1,
    metricValueLabel: "26.1 °C",
    collectedAt: "2026-04-05 09:00",
    sourceLabel: "Excel 导入",
    statusLabel: "偏高",
    statusColor: "orange"
  },
  {
    id: 4,
    warehouseName: "二号粮仓",
    metricType: "humidity",
    metricTypeLabel: "湿度",
    metricValue: 61.5,
    metricValueLabel: "61.5 %",
    collectedAt: "2026-04-05 09:00",
    sourceLabel: "Excel 导入",
    statusLabel: "偏高",
    statusColor: "orange"
  },
  {
    id: 5,
    warehouseName: "三号粮仓",
    metricType: "temperature",
    metricTypeLabel: "温度",
    metricValue: 24.2,
    metricValueLabel: "24.2 °C",
    collectedAt: "2026-04-05 10:00",
    sourceLabel: "维护补录",
    statusLabel: "维护中",
    statusColor: "blue"
  },
  {
    id: 6,
    warehouseName: "一号粮仓",
    metricType: "temperature",
    metricTypeLabel: "温度",
    metricValue: 25.3,
    metricValueLabel: "25.3 °C",
    collectedAt: "2026-04-05 12:00",
    sourceLabel: "人工录入",
    statusLabel: "偏高",
    statusColor: "orange"
  }
];

export const environmentOverviewCards = [
  {
    label: "平均温度",
    value: "25.4 °C",
    tags: ["重点关注一号粮仓", "较昨日上升 0.8 °C"]
  },
  {
    label: "平均湿度",
    value: "58.6 %",
    tags: ["整体稳定", "二号粮仓波动偏大"]
  },
  {
    label: "数据完整率",
    value: "96 %",
    tags: ["导入后完成基础预处理", "具备预测输入条件"]
  }
];

export const environmentTrendSeries: TrendSeries[] = [
  {
    key: "temperature",
    label: "温度",
    color: "#ef4444",
    points: [
      { label: "06:00", value: 23.8 },
      { label: "08:00", value: 24.2 },
      { label: "10:00", value: 24.9 },
      { label: "12:00", value: 25.3 },
      { label: "14:00", value: 25.8 },
      { label: "16:00", value: 26.2 }
    ]
  },
  {
    key: "humidity",
    label: "湿度",
    color: "#2f6fed",
    points: [
      { label: "06:00", value: 54.1 },
      { label: "08:00", value: 55.8 },
      { label: "10:00", value: 57.2 },
      { label: "12:00", value: 58.6 },
      { label: "14:00", value: 60.1 },
      { label: "16:00", value: 59.4 }
    ]
  }
];

export const environmentMetricRanking: RankingItem[] = [
  { key: "temp-a01", label: "一号粮仓温度", value: 25.3, suffix: " °C", note: "较稳定但持续上升", color: "#ef4444" },
  { key: "temp-b02", label: "二号粮仓温度", value: 26.1, suffix: " °C", note: "当前最高，需要重点关注", color: "#f59e0b" },
  { key: "hum-b02", label: "二号粮仓湿度", value: 61.5, suffix: " %", note: "湿度波动偏大", color: "#2f6fed" },
  { key: "temp-d04", label: "四号粮仓温度", value: 23.9, suffix: " °C", note: "作为稳定对照样本", color: "#0f9d7a" }
];

export const dataImportSteps = [
  "选择 Excel 模板或手工录入单条环境数据。",
  "校验仓库编号、指标类型、采样时间是否完整。",
  "完成基础预处理，去除空值并标记异常区间。",
  "写入数据库后即可在图表与预测页直接使用。"
];

export const userSummaryCards = [
  { key: "all", label: "系统用户", value: 9, note: "覆盖管理员、仓库管理员、查看者三种角色" },
  { key: "active", label: "启用账号", value: 8, note: "保留 1 个禁用账号，演示状态控制能力" },
  { key: "managers", label: "仓库管理员", value: 4, note: "每个仓库管理员可被绑定所属仓库" }
];

export const userRecords: UserRecord[] = [
  {
    id: 1,
    username: "admin",
    displayName: "系统管理员",
    roleLabel: "管理员",
    warehouseName: "全部仓库",
    statusLabel: "启用",
    statusColor: "green",
    lastLoginAt: "2026-04-06 09:10"
  },
  {
    id: 2,
    username: "nurxat",
    displayName: "努尔夏提",
    roleLabel: "仓库管理员",
    warehouseName: "一号粮仓",
    statusLabel: "启用",
    statusColor: "green",
    lastLoginAt: "2026-04-06 08:42"
  },
  {
    id: 3,
    username: "elijiang",
    displayName: "艾力江",
    roleLabel: "仓库管理员",
    warehouseName: "二号粮仓",
    statusLabel: "启用",
    statusColor: "green",
    lastLoginAt: "2026-04-05 20:16"
  },
  {
    id: 4,
    username: "viewer01",
    displayName: "数据查看员",
    roleLabel: "查看者",
    warehouseName: "全部仓库",
    statusLabel: "禁用",
    statusColor: "default",
    lastLoginAt: "2026-04-03 17:30"
  }
];

export const roleProfiles: RoleProfile[] = [
  {
    roleCode: "ADMIN",
    roleLabel: "管理员",
    description: "负责系统级配置、用户管理、仓库管理和全量数据查看。",
    permissions: ["用户增删改查", "仓库维护", "查看全部环境数据", "查看全部预测记录"]
  },
  {
    roleCode: "WAREHOUSE_MANAGER",
    roleLabel: "仓库管理员",
    description: "面向业务主操作者，负责录入环境数据、查看所属仓库图表和预测结果。",
    permissions: ["录入环境数据", "按仓库查询数据", "执行预测任务", "查看归档结果"]
  },
  {
    roleCode: "VIEWER",
    roleLabel: "查看者",
    description: "只读查看图表、概览和预测结果，适合答辩演示中的领导视角。",
    permissions: ["查看仪表盘", "查看图表", "查看预测结果"]
  }
];

export const permissionMatrix: PermissionMatrixRow[] = [
  { module: "用户管理", admin: "读 / 写", warehouseManager: "无", viewer: "无" },
  { module: "仓库管理", admin: "读 / 写", warehouseManager: "读", viewer: "读" },
  { module: "环境数据", admin: "读 / 写", warehouseManager: "读 / 写", viewer: "读" },
  { module: "温度预测", admin: "读 / 写", warehouseManager: "读 / 写", viewer: "读" },
  { module: "预测归档", admin: "读", warehouseManager: "读", viewer: "读" }
];

export const forecastSummary: ForecastSummaryItem[] = [
  { time: "13:00", temperature: "25.7 °C", statusColor: "gold", percent: 56 },
  { time: "14:00", temperature: "26.1 °C", statusColor: "orange", percent: 64 },
  { time: "15:00", temperature: "26.5 °C", statusColor: "orange", percent: 72 },
  { time: "16:00", temperature: "27.0 °C", statusColor: "red", percent: 84 }
];

export const predictionRecords: PredictionRecord[] = [
  {
    time: "2026-04-05 13:00",
    actualValue: "-",
    predictedValue: "25.7 °C",
    confidence: "92%",
    statusLabel: "关注",
    statusColor: "gold"
  },
  {
    time: "2026-04-05 14:00",
    actualValue: "-",
    predictedValue: "26.1 °C",
    confidence: "89%",
    statusLabel: "偏高",
    statusColor: "orange"
  },
  {
    time: "2026-04-05 15:00",
    actualValue: "-",
    predictedValue: "26.5 °C",
    confidence: "88%",
    statusLabel: "偏高",
    statusColor: "orange"
  },
  {
    time: "2026-04-05 16:00",
    actualValue: "-",
    predictedValue: "27.0 °C",
    confidence: "85%",
    statusLabel: "预警",
    statusColor: "red"
  }
];

export const predictionColumnsData = [
  { label: "预测对象", value: "一号粮仓 / 温度" },
  { label: "预测算法", value: "线性回归（静态演示）" },
  { label: "预测步数", value: "未来 6 小时" },
  { label: "最高预测值", value: "27.0 °C" },
  { label: "建议动作", value: "16:00 前后执行通风并人工复核" }
];

export const predictionTrendSeries: TrendSeries[] = [
  {
    key: "history",
    label: "历史温度",
    color: "#2f6fed",
    points: [
      { label: "07:00", value: 23.9 },
      { label: "08:00", value: 24.2 },
      { label: "09:00", value: 24.8 },
      { label: "10:00", value: 24.9 },
      { label: "11:00", value: 25.1 },
      { label: "12:00", value: 25.3 }
    ]
  },
  {
    key: "predict",
    label: "未来预测",
    color: "#ef4444",
    dashed: true,
    points: [
      { label: "13:00", value: 25.7 },
      { label: "14:00", value: 26.1 },
      { label: "15:00", value: 26.5 },
      { label: "16:00", value: 27.0 },
      { label: "17:00", value: 26.8 },
      { label: "18:00", value: 26.4 }
    ]
  }
];

export const predictionInsightItems = [
  { label: "历史样本量", value: "72 条", note: "已满足当前静态演示中的预测输入条件" },
  { label: "预测任务状态", value: "已执行", note: "正式版会由后端接口触发算法并返回结果" },
  { label: "风险判断", value: "16:00 预警", note: "建议提前通风并复核当前采样数据" }
];

export const predictionArchiveRecords: ArchiveRecord[] = [
  {
    id: 1,
    taskName: "一号粮仓 2026-04-05 日间温度预测",
    createdAt: "2026-04-05 12:05",
    algorithmName: "线性回归",
    summary: "未来 4 小时温度继续上升，16:00 达到预警区间。"
  },
  {
    id: 2,
    taskName: "二号粮仓 2026-04-04 夜间湿度辅助分析",
    createdAt: "2026-04-04 20:40",
    algorithmName: "加权移动平均",
    summary: "湿度波动较大，但温度仍处于可控区间。"
  }
];

export const screenMetricCards: ScreenMetricCard[] = [
  { key: "online", label: "在线粮仓", value: "6", note: "全部纳入统一监测视图" },
  { key: "samples", label: "今日采样", value: "386", note: "温湿度为核心展示指标" },
  { key: "predict", label: "预测任务", value: "18", note: "已执行并归档的任务数" },
  { key: "risk", label: "风险仓库", value: "2", note: "当前需重点关注的一号与二号粮仓" }
];

export const screenTrendSeries: TrendSeries[] = [
  {
    key: "avg-temperature",
    label: "平均温度",
    color: "#22c55e",
    points: [
      { label: "08:00", value: 23.7 },
      { label: "10:00", value: 24.6 },
      { label: "12:00", value: 25.2 },
      { label: "14:00", value: 25.9 },
      { label: "16:00", value: 26.4 },
      { label: "18:00", value: 26.1 }
    ]
  },
  {
    key: "avg-humidity",
    label: "平均湿度",
    color: "#38bdf8",
    points: [
      { label: "08:00", value: 55.1 },
      { label: "10:00", value: 56.9 },
      { label: "12:00", value: 58.2 },
      { label: "14:00", value: 59.6 },
      { label: "16:00", value: 60.4 },
      { label: "18:00", value: 59.1 }
    ]
  },
  {
    key: "predict-temperature",
    label: "预测温度",
    color: "#facc15",
    dashed: true,
    points: [
      { label: "19:00", value: 26.0 },
      { label: "20:00", value: 26.3 },
      { label: "21:00", value: 26.7 },
      { label: "22:00", value: 27.1 },
      { label: "23:00", value: 26.8 },
      { label: "24:00", value: 26.4 }
    ]
  }
];

export const screenHealthRanking: RankingItem[] = [
  { key: "screen-a01", label: "一号粮仓风险指数", value: 84, suffix: "%", note: "温度上升最明显", color: "#ef4444" },
  { key: "screen-b02", label: "二号粮仓风险指数", value: 71, suffix: "%", note: "湿度波动偏大", color: "#f59e0b" },
  { key: "screen-c03", label: "三号粮仓运行指数", value: 62, suffix: "%", note: "维护中，展示为辅助视图", color: "#38bdf8" },
  { key: "screen-d04", label: "四号粮仓运行指数", value: 92, suffix: "%", note: "状态稳定，可作为对照仓", color: "#22c55e" }
];

export const screenAlerts: ScreenAlert[] = [
  {
    title: "A01 预计 22:00 达到温度峰值",
    level: "高",
    description: "预测温度 27.1 °C，建议提前安排通风和人工巡查。"
  },
  {
    title: "B02 夜间湿度振幅扩大",
    level: "中",
    description: "请关注夜间采样连续性，必要时补录缺失点。"
  },
  {
    title: "C03 维护模式",
    level: "提示",
    description: "当前展示数据仅供参考，不进入自动预警计算。"
  }
];

export const screenWarehousePanels: ScreenWarehousePanel[] = [
  {
    warehouseName: "一号粮仓",
    temperature: "25.3 °C",
    humidity: "56.2 %",
    trend: "温度持续上升",
    riskLabel: "高风险"
  },
  {
    warehouseName: "二号粮仓",
    temperature: "26.1 °C",
    humidity: "61.5 %",
    trend: "湿度波动明显",
    riskLabel: "中风险"
  },
  {
    warehouseName: "四号粮仓",
    temperature: "23.9 °C",
    humidity: "52.4 %",
    trend: "运行稳定",
    riskLabel: "低风险"
  }
];

export const screenPredictionBoard: ScreenPredictionBoardItem[] = [
  {
    warehouseName: "一号粮仓",
    nextPeakTime: "22:00",
    peakValue: "27.1 °C",
    action: "提前通风"
  },
  {
    warehouseName: "二号粮仓",
    nextPeakTime: "21:00",
    peakValue: "26.2 °C",
    action: "复核湿度"
  },
  {
    warehouseName: "四号粮仓",
    nextPeakTime: "20:00",
    peakValue: "24.4 °C",
    action: "保持观察"
  }
];

export function warehouseColumnsData(record: WarehouseRecord) {
  return [
    { label: "仓库名称", value: record.name },
    { label: "仓库编码", value: record.code },
    { label: "负责人", value: record.managerName },
    { label: "仓库位置", value: record.location },
    { label: "容量", value: `${record.capacityTon} 吨` },
    { label: "装载率", value: `${record.utilizationRate}%` },
    { label: "最近温度", value: record.latestTemperature },
    { label: "最近湿度", value: record.latestHumidity },
    { label: "运行状态", value: record.statusLabel }
  ];
}
