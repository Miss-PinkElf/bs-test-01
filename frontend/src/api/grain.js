import axios from "axios";
import { API_BASE_URL, request } from "./http";

const metricNameMap = {
  temperature: "温度",
  humidity: "湿度",
  co2: "二氧化碳浓度"
};

const DEFAULT_METRIC_CODE = "temperature";

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

function calcRiskLevel(points, maxThreshold) {
  const peak = Math.max(...points.map((item) => Number(item.predictedValue || 0)), 0);

  if (!maxThreshold) {
    return "NORMAL";
  }

  if (peak >= Number(maxThreshold)) {
    return "WARNING";
  }

  if (peak >= Number(maxThreshold) * 0.9) {
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
      : [],
    recentSensorRecords: Array.isArray(raw.recentSensorRecords)
      ? raw.recentSensorRecords.map(normalizeSensorData)
      : [],
    warehouseHealthList: Array.isArray(raw.warehouseHealthList)
      ? raw.warehouseHealthList
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
  const metricCode = item?.metricCode || DEFAULT_METRIC_CODE;

  const maxThreshold = item?.maxThreshold ?? null;

  return {
    taskId: item?.taskId || null,
    taskNo: item?.taskNo || "TEMP-DEMO",
    metricCode,
    metricName: item?.metricName || metricNameMap[metricCode] || metricCode,
    unit: item?.unit || "",
    maxThreshold,
    algorithmName: item?.algorithmName || "线性回归",
    riskLevel: item?.riskLevel || calcRiskLevel(resultList, maxThreshold),
    requestedAt: item?.requestedAt || new Date().toISOString(),
    summary: item?.summary || "预测执行完成",
    resultList
  };
}

export async function predictMetric(payload) {
  const metricCode = payload.metricCode || DEFAULT_METRIC_CODE;
  console.info("[Prediction] 调用预测接口", {
    warehouseId: payload.warehouseId,
    metricCode,
    futureSteps: Number(payload.futureSteps)
  });

  const raw = await request({
    url: "/api/predictions",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricCode,
      futureSteps: Number(payload.futureSteps)
    }
  });

  const task = normalizePredictionTask(raw);
  console.info("[Prediction] 预测接口返回", {
    taskId: task.taskId,
    metricCode: task.metricCode,
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

function splitCsvValue(value) {
  return value ? String(value).split(",").filter(Boolean) : [];
}

function normalizeUser(item) {
  return {
    id: item.id,
    username: item.username || "-",
    displayName: item.displayName || "-",
    roleCodes: splitCsvValue(item.roleCodes),
    roleNames: splitCsvValue(item.roleNames),
    warehouseName: item.warehouseName || "平台级",
    status: item.status || "ACTIVE",
    lastLoginAt: item.lastLoginAt || "-"
  };
}

function normalizeRoleOption(item) {
  return {
    roleCode: item.roleCode || "-",
    roleName: item.roleName || "-",
    roleDesc: item.roleDesc || "暂无说明"
  };
}

export async function fetchUsers() {
  console.info("[User] 调用用户列表接口");

  const raw = await request({
    url: "/api/users",
    method: "get"
  });

  const users = Array.isArray(raw) ? raw.map(normalizeUser) : [];
  console.info("[User] 用户列表返回", { count: users.length });
  return users;
}

export async function fetchRoleOptions() {
  console.info("[User] 调用角色选项接口");

  const raw = await request({
    url: "/api/roles/options",
    method: "get"
  });

  const roles = Array.isArray(raw) ? raw.map(normalizeRoleOption) : [];
  console.info("[User] 角色选项返回", { count: roles.length });
  return roles;
}

function normalizeMetricOption(item) {
  const metricCode = item?.metricCode || DEFAULT_METRIC_CODE;

  return {
    value: metricCode,
    label: item?.metricName || metricNameMap[metricCode] || metricCode,
    unit: item?.unit || "",
    minThreshold: item?.minThreshold ?? null,
    maxThreshold: item?.maxThreshold ?? null
  };
}

export async function fetchMetricOptions() {
  console.info("[Metric] 调用指标选项接口");

  const raw = await request({
    url: "/api/metrics/options",
    method: "get"
  });

  const options = Array.isArray(raw) ? raw.map(normalizeMetricOption) : [];
  console.info("[Metric] 指标选项返回", { count: options.length });
  return options;
}
