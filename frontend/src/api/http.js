import axios from "axios";

export const API_BASE_URL = import.meta.env.VITE_API_BASE || "http://localhost:8081";

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
});

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "请求失败";

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

export async function request(config) {
  const payload = await http.request({
    headers: {
      "Content-Type": "application/json"
    },
    ...config
  });

  return unwrapPayload(payload);
}

export default http;
