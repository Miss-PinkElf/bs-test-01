import axios from "axios";
import { getSession } from "../utils/session";

export const API_BASE_URL = import.meta.env.VITE_API_BASE || "http://localhost:8081";
export const DEMO_USERNAME_HEADER = "X-Demo-Username";
const LOGIN_API_PATH = "/api/auth/login";

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
});

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const backendMessage = error.response?.data?.message || error.response?.data?.error;
    const message =
      error.response?.status === 403
        ? backendMessage || error.message || "无权限访问"
        : backendMessage || error.message || "请求失败";

    return Promise.reject(new Error(message));
  }
);

export function unwrapPayload(payload) {
  if (
    payload &&
    typeof payload === "object" &&
    "code" in payload &&
    "data" in payload
  ) {
    if (payload.code !== 200) {
      throw new Error(payload.message || "请求失败");
    }

    return payload.data;
  }

  return payload;
}

function hasHeader(headers, headerName) {
  return Object.keys(headers).some((key) => key.toLowerCase() === headerName.toLowerCase());
}

function buildRequestHeaders(config) {
  const headers = {
    "Content-Type": "application/json",
    ...(config.headers || {})
  };
  const session = getSession();

  if (
    session?.username &&
    config.url !== LOGIN_API_PATH &&
    !hasHeader(headers, DEMO_USERNAME_HEADER)
  ) {
    headers[DEMO_USERNAME_HEADER] = session.username;
  }

  return headers;
}

export async function request(config) {
  const payload = await http.request({
    headers: buildRequestHeaders(config),
    ...config
  });

  return unwrapPayload(payload);
}

export default http;
