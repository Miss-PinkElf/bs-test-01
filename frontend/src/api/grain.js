import axios from "axios";
import { API_BASE_URL, request } from "./http";

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
  const metricCode = item.metricCode || item.metricCode || "temperature";

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
  const raw = await request({
    url: "/api/warehouses",
    method: "post",
    data: {
      warehouseCode: payload.warehouseCode,
      warehouseName: payload.warehouseName,
      location: payload.location,
      capacityTon: Number(payload.capacityTon),
      managerName: payload.managerName,
      status: payload.status
    }
  });

  return raw;
}

export async function fetchSensorData(params = {}) {
  const metricCode = params.metricCode || params.metricCode;
  const raw = await request({
    url: "/api/sensor-data",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      metricCode: metricCode || undefined
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
      metricCode: payload.metricCode || payload.metricCode,
      metricValue: Number(payload.metricValue),
      collectedAt: payload.collectedAt || undefined
    }
  });

  return normalizeSensorData(raw);
}

export async function importSensorData(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axios.post(`${API_BASE_URL}/api/sensor-data/import`, formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });

  return unwrapImportResult(response.data);
}

export function downloadSensorTemplate() {
  window.open(`${API_BASE_URL}/api/sensor-data/import/template`, "_blank");
}

function unwrapImportResult(payload) {
  if (payload?.code === 200) {
    return payload.data;
  }

  throw new Error(payload?.message || "导入失败");
}

function normalizePredictionTask(item) {
  const resultList = Array.isArray(item?.resultList)
    ? item.resultList.map(normalizePredictionPoint)
    : [];

  return {
    taskId: item?.taskId || null,
    taskNo: item?.taskNo || "TEMP-DEMO",
    algorithmName: item?.algorithmName || "线性回归",
    riskLevel: item?.riskLevel || calcRiskLevel(resultList),
    requestedAt: item?.requestedAt || new Date().toISOString(),
    summary: item?.summary || "预测执行完成",
    resultList
  };
}

export async function predictTemperature(payload) {
  console.info("[Prediction] 调用预测接口", {
    warehouseId: payload.warehouseId,
    metricCode: payload.metricCode || "temperature",
    futureSteps: Number(payload.futureSteps)
  });

  const raw = await request({
    url: "/api/predictions/temperature",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricCode: payload.metricCode || payload.metricCode || "temperature",
      futureSteps: Number(payload.futureSteps)
    }
  });

  const task = normalizePredictionTask(raw);
  console.info("[Prediction] 预测接口返回", {
    taskId: task.taskId,
    riskLevel: task.riskLevel,
    resultCount: task.resultList.length
  });
  return task;
}

export async function fetchPredictionTasks() {
  console.info("[Prediction] 调用预测历史列表接口");

  const raw = await request({
    url: "/api/predictions/tasks",
    method: "get"
  });

  const tasks = Array.isArray(raw) ? raw.map(normalizePredictionTask) : [];
  console.info("[Prediction] 预测历史列表返回", { count: tasks.length });
  return tasks;
}

export function getMetricOptions() {
  return [
    { value: "temperature", label: "温度" },
    { value: "humidity", label: "湿度" },
    { value: "co2", label: "二氧化碳浓度" }
  ];
}
