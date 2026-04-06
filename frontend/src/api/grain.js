import { request } from "./http";

const metricNameMap = {
  temperature: "温度",
  humidity: "湿度",
  co2: "二氧化碳浓度"
};

function normalizeRoleCodes(raw) {
  if (Array.isArray(raw?.roleCodes)) {
    return raw.roleCodes;
  }

  if (raw?.role) {
    return [raw.role];
  }

  return [];
}

function normalizeAlert(item) {
  if (typeof item === "string") {
    return {
      title: item,
      level: "中",
      description: "当前演示后端仅返回简化预警文本，正式版会升级为结构化预警对象。"
    };
  }

  return {
    title: item.title || "预警信息",
    level: item.level || "中",
    description: item.description || "暂无详细描述"
  };
}

function normalizeWarehouse(item) {
  return {
    id: item.id,
    warehouseCode: item.warehouseCode || item.code,
    warehouseName: item.warehouseName || item.name,
    location: item.location || "-",
    capacityTon: item.capacityTon || 0,
    managerName: item.managerName || "-",
    status: item.status || "ACTIVE"
  };
}

function normalizeSensorData(item) {
  const metricCode = item.metricCode || item.metricType || "temperature";

  return {
    id: item.id,
    warehouseId: item.warehouseId,
    warehouseName: item.warehouseName || "-",
    metricCode,
    metricName: item.metricName || metricNameMap[metricCode] || metricCode,
    metricValue: item.metricValue,
    collectedAt: item.collectedAt,
    sourceType: item.sourceType || "MANUAL",
    qualityFlag: item.qualityFlag || "NORMAL"
  };
}

function normalizePredictionPoint(item, index) {
  return {
    stepIndex: item.stepIndex || index + 1,
    predictedTime: item.predictedTime || item.time,
    actualValue: item.actualValue ?? null,
    predictedValue: item.predictedValue ?? item.value ?? null
  };
}

function calcRiskLevel(points) {
  const peak = Math.max(...points.map((item) => Number(item.predictedValue || 0)), 0);

  if (peak >= 28) {
    return "WARNING";
  }

  if (peak >= 26) {
    return "ATTENTION";
  }

  return "NORMAL";
}

export async function login(payload) {
  const raw = await request({
    url: "/api/auth/login",
    method: "post",
    data: payload
  });

  return {
    ...raw,
    roleCodes: normalizeRoleCodes(raw)
  };
}

export async function fetchOverview() {
  const raw = await request({
    url: "/api/dashboard/overview",
    method: "get"
  });

  return {
    warehouseCount: raw.warehouseCount || 0,
    todayDataCount: raw.todayDataCount || 0,
    alertCount: raw.alertCount || 0,
    archivedPredictionCount: raw.archivedPredictionCount || 0,
    latestAlerts: Array.isArray(raw.latestAlerts)
      ? raw.latestAlerts.map(normalizeAlert)
      : []
  };
}

export async function fetchWarehouses() {
  const raw = await request({
    url: "/api/warehouses",
    method: "get"
  });

  return Array.isArray(raw) ? raw.map(normalizeWarehouse) : [];
}

export async function createWarehouse(payload) {
  return request({
    url: "/api/warehouses",
    method: "post",
    data: {
      code: payload.warehouseCode,
      name: payload.warehouseName,
      location: payload.location,
      capacityTon: Number(payload.capacityTon),
      managerName: payload.managerName,
      status: payload.status
    }
  });
}

export async function fetchSensorData(params = {}) {
  const metricCode = params.metricCode || params.metricType;
  const raw = await request({
    url: "/api/sensor-data",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      metricType: metricCode || undefined
    }
  });

  return Array.isArray(raw) ? raw.map(normalizeSensorData) : [];
}

export async function createSensorData(payload) {
  const raw = await request({
    url: "/api/sensor-data",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricType: payload.metricCode || payload.metricType,
      metricValue: Number(payload.metricValue),
      collectedAt: payload.collectedAt || undefined
    }
  });

  return normalizeSensorData(raw);
}

export async function predictTemperature(payload) {
  const raw = await request({
    url: "/api/predictions/temperature",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricType: payload.metricCode || payload.metricType || "temperature",
      futureSteps: Number(payload.futureSteps)
    }
  });

  const resultList = Array.isArray(raw)
    ? raw.map(normalizePredictionPoint)
    : Array.isArray(raw.resultList)
      ? raw.resultList.map(normalizePredictionPoint)
      : [];

  return {
    taskId: raw.taskId || null,
    taskNo: raw.taskNo || "TEMP-DEMO",
    algorithmName: raw.algorithmName || "线性回归",
    riskLevel: raw.riskLevel || calcRiskLevel(resultList),
    requestedAt: raw.requestedAt || new Date().toISOString(),
    resultList
  };
}

export function getMetricOptions() {
  return [
    { value: "temperature", label: "温度" },
    { value: "humidity", label: "湿度" },
    { value: "co2", label: "二氧化碳浓度" }
  ];
}
