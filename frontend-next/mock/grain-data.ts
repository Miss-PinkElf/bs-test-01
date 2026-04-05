type DashboardCard = {
  key: string;
  label: string;
  value: number | string;
  suffix?: string;
  note: string;
};

type WarehouseRecord = {
  id: number;
  code: string;
  name: string;
  location: string;
  capacityTon: number;
  managerName: string;
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
  statusLabel: string;
  statusColor: string;
};

export const MOCK_CURRENT_USER = {
  username: "nurxat",
  displayName: "努尔夏提",
  roleLabel: "仓库管理员"
};

export const dashboardCards: DashboardCard[] = [
  { key: "warehouse", label: "在线粮仓", value: 3, note: "当前纳入管理的仓库数量" },
  { key: "records", label: "今日采样", value: 128, note: "环境数据全部来自本地 mock" },
  { key: "warning", label: "待处理预警", value: 2, note: "高温和湿度波动需要关注" },
  { key: "accuracy", label: "预测可用率", value: 93, suffix: "%", note: "用于展示最终页面效果" }
];

export const warehouseRecords: WarehouseRecord[] = [
  {
    id: 1,
    code: "WH-A01",
    name: "一号粮仓",
    location: "北区 1 栋",
    capacityTon: 500,
    managerName: "努尔夏提",
    statusLabel: "运行中",
    statusColor: "green"
  },
  {
    id: 2,
    code: "WH-B02",
    name: "二号粮仓",
    location: "北区 2 栋",
    capacityTon: 650,
    managerName: "艾力江",
    statusLabel: "运行中",
    statusColor: "blue"
  },
  {
    id: 3,
    code: "WH-C03",
    name: "三号粮仓",
    location: "南区 1 栋",
    capacityTon: 720,
    managerName: "阿依古丽",
    statusLabel: "维护中",
    statusColor: "orange"
  }
];

export const warehouseAlerts = [
  {
    title: "一号粮仓温度接近预警阈值",
    level: "高",
    description: "近 6 小时温度连续上涨，建议尽快安排通风处理。"
  },
  {
    title: "二号粮仓湿度波动偏大",
    level: "中",
    description: "夜间湿度波动明显，建议复核传感器和通风策略。"
  },
  {
    title: "三号粮仓维护中",
    level: "中",
    description: "维护期间数据仅做展示，不参与自动判断。"
  }
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
    statusLabel: "偏高",
    statusColor: "orange"
  },
  {
    id: 5,
    warehouseName: "三号粮仓",
    metricType: "oxygen",
    metricTypeLabel: "氧气浓度",
    metricValue: 20.4,
    metricValueLabel: "20.4 %",
    collectedAt: "2026-04-05 09:00",
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
    statusLabel: "偏高",
    statusColor: "orange"
  }
];

export const metricOverview = [
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
    label: "设备状态",
    value: "4 / 5 在线",
    tags: ["三号粮仓维护中", "建议补采样"]
  }
];

export const forecastSummary: ForecastSummaryItem[] = [
  { time: "13:00", temperature: "25.7 °C", statusColor: "gold", percent: 56 },
  { time: "14:00", temperature: "26.1 °C", statusColor: "orange", percent: 64 },
  { time: "15:00", temperature: "26.5 °C", statusColor: "orange", percent: 72 },
  { time: "16:00", temperature: "27.0 °C", statusColor: "red", percent: 84 }
];

export const predictionRecords: PredictionRecord[] = [
  { time: "2026-04-05 13:00", actualValue: "-", predictedValue: "25.7 °C", statusLabel: "关注", statusColor: "gold" },
  { time: "2026-04-05 14:00", actualValue: "-", predictedValue: "26.1 °C", statusLabel: "偏高", statusColor: "orange" },
  { time: "2026-04-05 15:00", actualValue: "-", predictedValue: "26.5 °C", statusLabel: "偏高", statusColor: "orange" },
  { time: "2026-04-05 16:00", actualValue: "-", predictedValue: "27.0 °C", statusLabel: "预警", statusColor: "red" }
];

export const predictionColumnsData = [
  { label: "预测对象", value: "一号粮仓 / 温度" },
  { label: "预测算法", value: "线性回归（静态展示）" },
  { label: "最高预测值", value: "27.0 °C" },
  { label: "建议动作", value: "提前安排通风和人工复核" }
];

export function warehouseColumnsData(record: WarehouseRecord) {
  return [
    { label: "仓库名称", value: record.name },
    { label: "仓库编码", value: record.code },
    { label: "负责人", value: record.managerName },
    { label: "仓库位置", value: record.location },
    { label: "容量", value: `${record.capacityTon} 吨` },
    { label: "运行状态", value: record.statusLabel }
  ];
}
