import { request } from "./http";

export function login(payload) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function fetchOverview() {
  return request("/api/dashboard/overview");
}

export function fetchWarehouses() {
  return request("/api/warehouses");
}

export function createWarehouse(payload) {
  return request("/api/warehouses", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function fetchSensorData(params) {
  const query = new URLSearchParams();
  if (params.warehouseId) query.append("warehouseId", params.warehouseId);
  if (params.metricType) query.append("metricType", params.metricType);
  return request(`/api/sensor-data?${query.toString()}`);
}

export function createSensorData(payload) {
  return request("/api/sensor-data", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function predictTemperature(payload) {
  return request("/api/predictions/temperature", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
