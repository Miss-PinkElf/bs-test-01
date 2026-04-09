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
  if (!item) {
    return {
      title: "预警信息",
      level: "ATTENTION",
      sourceType: "REAL",
      warehouseName: "-",
      eventTime: "",
      description: "预警数据缺少明细，已按兜底信息展示。"
    };
  }

  if (typeof item === "string") {
    return {
      title: item,
      level: "ATTENTION",
      sourceType: "REAL",
      warehouseName: "-",
      eventTime: "",
      description: "当前演示后端仅返回简化预警文本，正式版会升级为结构化预警对象。"
    };
  }

  return {
    title: item.title || "预警信息",
    level: item.level || "ATTENTION",
    sourceType: item.sourceType || "REAL",
    warehouseName: item.warehouseName || "-",
    eventTime: item.eventTime || "",
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
  const metricCode = item.metricCode || DEFAULT_METRIC_CODE;

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

function normalizeGrainRecord(item) {
  return {
    id: item.id,
    warehouseId: item.warehouseId,
    warehouseName: item.warehouseName || "-",
    pointId: item.pointId ?? null,
    pointName: item.pointName || "-",
    zoneCode: item.zoneCode || "",
    layerNo: item.layerNo ?? null,
    pointNo: item.pointNo ?? null,
    collectedAt: item.collectedAt || "",
    temperatureValue: item.temperatureValue ?? null,
    sourceType: item.sourceType || "MANUAL",
    qualityFlag: item.qualityFlag || "NORMAL"
  };
}

function normalizePageResult(raw, itemNormalizer) {
  const list = Array.isArray(raw?.list) ? raw.list.map(itemNormalizer) : [];

  return {
    list,
    pageNum: raw?.pageNum ?? 1,
    pageSize: raw?.pageSize ?? (list.length || 10),
    total: raw?.total ?? list.length
  };
}

function normalizeDashboardSummary(item) {
  return {
    id: item.id ?? null,
    warehouseId: item.warehouseId ?? null,
    warehouseName: item.warehouseName || "-",
    avgTemp: item.avgTemp ?? null,
    maxTemp: item.maxTemp ?? null,
    minTemp: item.minTemp ?? null,
    collectedAt: item.collectedAt || "",
    warningLevel: item.warningLevel || "NORMAL",
    warningFlag: Boolean(item.warningFlag),
    warningMessage: item.warningMessage || ""
  };
}

function normalizeDashboardWarehouseHealth(item) {
  return {
    warehouseId: item.warehouseId ?? null,
    warehouseName: item.warehouseName || "-",
    healthScore: item.healthScore ?? 0,
    riskLevel: item.riskLevel || "NORMAL",
    realWarningLevel: item.realWarningLevel || "NORMAL",
    predictionWarningLevel: item.predictionWarningLevel || "NORMAL",
    latestAvgTemp: item.latestAvgTemp ?? null,
    latestForecastValue: item.latestForecastValue ?? null
  };
}

function normalizeGrainSummary(item) {
  return {
    id: item.id,
    warehouseId: item.warehouseId,
    warehouseName: item.warehouseName || "-",
    collectedAt: item.collectedAt,
    avgTemp: item.avgTemp ?? null,
    maxTemp: item.maxTemp ?? null,
    minTemp: item.minTemp ?? null,
    layer1Avg: item.layer1Avg ?? null,
    layer2Avg: item.layer2Avg ?? null,
    layer3Avg: item.layer3Avg ?? null,
    layer4Avg: item.layer4Avg ?? null,
    warningLevel: item.warningLevel || "NORMAL",
    warningFlag: Boolean(item.warningFlag),
    warningMessage: item.warningMessage || "",
    analysisResult: item.analysisResult || ""
  };
}

function normalizePredictionPoint(item, index) {
  return {
    id: item.id || null,
    phaseType: item.phaseType || "FUTURE",
    stepIndex: item.stepIndex || index + 1,
    resultTime: item.resultTime || item.predictedTime || item.time,
    actualValue: item.actualValue ?? null,
    predictedValue: item.predictedValue ?? item.value ?? null,
    errorValue: item.errorValue ?? null,
    errorRate: item.errorRate ?? null,
    warningLevel: item.warningLevel || "NORMAL",
    warningFlag: Boolean(item.warningFlag),
    warningMessage: item.warningMessage || "",
    isCorrected: Boolean(item.isCorrected)
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
    grainSummaryCount: raw.grainSummaryCount || 0,
    realAlertCount: raw.realAlertCount || 0,
    predictionAlertCount: raw.predictionAlertCount || 0,
    archivedPredictionCount: raw.archivedPredictionCount || 0,
    latestAlerts: Array.isArray(raw.latestAlerts)
      ? raw.latestAlerts.filter((item) => item != null).map(normalizeAlert)
      : [],
    latestGrainSummaries: Array.isArray(raw.latestGrainSummaries)
      ? raw.latestGrainSummaries.map(normalizeDashboardSummary)
      : [],
    warehouseHealthList: Array.isArray(raw.warehouseHealthList)
      ? raw.warehouseHealthList.map(normalizeDashboardWarehouseHealth)
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
      warehouseCode: payload.warehouseCode,
      warehouseName: payload.warehouseName,
      location: payload.location,
      capacityTon: Number(payload.capacityTon),
      managerName: payload.managerName,
      status: payload.status
    }
  });
}

export async function updateWarehouse(id, payload) {
  return request({
    url: `/api/warehouses/${id}`,
    method: "put",
    data: {
      warehouseCode: payload.warehouseCode,
      warehouseName: payload.warehouseName,
      location: payload.location,
      capacityTon: Number(payload.capacityTon),
      managerName: payload.managerName,
      status: payload.status
    }
  });
}

export async function deleteWarehouse(id) {
  return request({
    url: `/api/warehouses/${id}`,
    method: "delete"
  });
}

export async function fetchSensorData(params = {}) {
  const raw = await request({
    url: "/api/sensor-data",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      metricCode: params.metricCode || undefined,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
  });

  return normalizePageResult(raw, normalizeSensorData);
}

export async function fetchSensorTrend(params = {}) {
  const raw = await request({
    url: "/api/sensor-data/trend",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      metricCode: params.metricCode || undefined
    }
  });

  return Array.isArray(raw?.points)
    ? raw.points.map((item) => ({
        time: item.time || "",
        value: item.value ?? null
      }))
    : [];
}

export async function createSensorData(payload) {
  return request({
    url: "/api/sensor-data",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricCode: payload.metricCode,
      metricValue: Number(payload.metricValue),
      collectedAt: payload.collectedAt || undefined
    }
  });
}

export async function updateSensorData(id, payload) {
  return request({
    url: `/api/sensor-data/${id}`,
    method: "put",
    data: {
      warehouseId: payload.warehouseId,
      metricCode: payload.metricCode,
      metricValue: Number(payload.metricValue),
      collectedAt: payload.collectedAt || undefined
    }
  });
}

export async function deleteSensorData(id) {
  return request({
    url: `/api/sensor-data/${id}`,
    method: "delete"
  });
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

export async function fetchGrainTempSummaries(params = {}) {
  const raw = await request({
    url: "/api/grain-temp/summaries",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      startTime: params.startTime || undefined,
      endTime: params.endTime || undefined
    }
  });

  return Array.isArray(raw) ? raw.map(normalizeGrainSummary) : [];
}

export async function fetchGrainTempRecords(params = {}) {
  const raw = await request({
    url: "/api/grain-temp/records",
    method: "get",
    params: {
      warehouseId: params.warehouseId || undefined,
      startTime: params.startTime || undefined,
      endTime: params.endTime || undefined,
      zoneCode: params.zoneCode || undefined,
      layerNo: params.layerNo || undefined,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
  });

  return normalizePageResult(raw, normalizeGrainRecord);
}

export async function createGrainTempRecord(payload) {
  return request({
    url: "/api/grain-temp/records",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      zoneCode: payload.zoneCode,
      layerNo: Number(payload.layerNo),
      pointNo: Number(payload.pointNo),
      collectedAt: payload.collectedAt,
      temperatureValue: Number(payload.temperatureValue),
      probeCode: payload.probeCode || undefined,
      remark: payload.remark || undefined
    }
  });
}

export async function updateGrainTempRecord(id, payload) {
  return request({
    url: `/api/grain-temp/records/${id}`,
    method: "put",
    data: {
      warehouseId: payload.warehouseId,
      zoneCode: payload.zoneCode,
      layerNo: Number(payload.layerNo),
      pointNo: Number(payload.pointNo),
      collectedAt: payload.collectedAt,
      temperatureValue: Number(payload.temperatureValue),
      probeCode: payload.probeCode || undefined,
      remark: payload.remark || undefined
    }
  });
}

export async function deleteGrainTempRecord(id) {
  return request({
    url: `/api/grain-temp/records/${id}`,
    method: "delete"
  });
}

export async function importGrainTemp(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axios.post(`${API_BASE_URL}/api/grain-temp/import`, formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });

  return unwrapImportResult(response.data);
}

export function downloadGrainTempTemplate() {
  window.open(`${API_BASE_URL}/api/grain-temp/import/template`, "_blank");
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
    parentTaskId: item?.parentTaskId ?? null,
    taskRound: item?.taskRound ?? 1,
    warehouseId: item?.warehouseId ?? null,
    warehouseName: item?.warehouseName || "-",
    metricCode,
    metricName: item?.metricName || metricNameMap[metricCode] || metricCode,
    unit: item?.unit || "",
    maxThreshold,
    targetType: item?.targetType || "AVG_TEMP",
    dataSourceType: item?.dataSourceType || "GRAIN_TEMP_SUMMARY",
    algorithmCode: item?.algorithmCode || "LINEAR_REGRESSION",
    algorithmName: item?.algorithmName || "线性回归",
    trainStartTime: item?.trainStartTime || "",
    trainEndTime: item?.trainEndTime || "",
    forecastStartTime: item?.forecastStartTime || "",
    forecastEndTime: item?.forecastEndTime || "",
    basedOnActualEndTime: item?.basedOnActualEndTime || "",
    forecastDays: item?.forecastDays ?? 0,
    triggerType: item?.triggerType || "INITIAL",
    adjustStatus: item?.adjustStatus || "UNADJUSTED",
    riskLevel: item?.riskLevel || calcRiskLevel(resultList, maxThreshold),
    requestedAt: item?.requestedAt || new Date().toISOString(),
    completedAt: item?.completedAt || "",
    summary: item?.summary || "预测执行完成",
    resultList
  };
}

export async function predictMetric(payload) {
  const raw = await request({
    url: "/api/predictions",
    method: "post",
    data: {
      warehouseId: payload.warehouseId,
      metricCode: payload.metricCode || DEFAULT_METRIC_CODE,
      targetType: payload.targetType || "AVG_TEMP",
      trainStartTime: payload.trainStartTime || undefined,
      trainEndTime: payload.trainEndTime || undefined,
      forecastDays: Number(payload.forecastDays)
    }
  });

  return normalizePredictionTask(raw);
}

export async function fetchPredictionTasks() {
  const raw = await request({
    url: "/api/predictions/tasks",
    method: "get"
  });

  return Array.isArray(raw) ? raw.map(normalizePredictionTask) : [];
}

function splitCsvValue(value) {
  return value ? String(value).split(",").filter(Boolean) : [];
}

function normalizeUser(item) {
  return {
    id: item.id,
    username: item.username || "-",
    displayName: item.displayName || "-",
    phone: item.phone || "",
    warehouseId: item.warehouseId ?? null,
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
  const raw = await request({
    url: "/api/users",
    method: "get"
  });

  return Array.isArray(raw) ? raw.map(normalizeUser) : [];
}

export async function fetchRoleOptions() {
  const raw = await request({
    url: "/api/roles/options",
    method: "get"
  });

  return Array.isArray(raw) ? raw.map(normalizeRoleOption) : [];
}

export async function createUser(payload) {
  return request({
    url: "/api/users",
    method: "post",
    data: {
      username: payload.username,
      password: payload.password,
      displayName: payload.displayName,
      phone: payload.phone || undefined,
      warehouseId: payload.warehouseId ?? undefined,
      status: payload.status,
      roleCodes: Array.isArray(payload.roleCodes) ? payload.roleCodes : []
    }
  });
}

export async function updateUser(id, payload) {
  return request({
    url: `/api/users/${id}`,
    method: "put",
    data: {
      displayName: payload.displayName,
      phone: payload.phone || undefined,
      warehouseId: payload.warehouseId ?? undefined,
      status: payload.status,
      roleCodes: Array.isArray(payload.roleCodes) ? payload.roleCodes : []
    }
  });
}

export async function resetUserPassword(id, newPassword) {
  return request({
    url: `/api/users/${id}/password`,
    method: "put",
    data: {
      newPassword
    }
  });
}

export async function deleteUser(id) {
  return request({
    url: `/api/users/${id}`,
    method: "delete"
  });
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
  const raw = await request({
    url: "/api/metrics/options",
    method: "get"
  });

  return Array.isArray(raw) ? raw.map(normalizeMetricOption) : [];
}
