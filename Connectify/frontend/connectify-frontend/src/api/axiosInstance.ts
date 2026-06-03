import axios from "axios";
import { store } from "../store/store";
import { updateTokens } from "../features/auth/authSlice";
import { refreshTokenApi } from "./authApi";
import { performLogout } from "../utils/logout";
import { showError } from "../utils/toast";

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (token: string | null) => {
  failedQueue.forEach((prom) => {
    if (token) {
      prom.resolve(token);
    } else {
      prom.reject();
    }
  });

  failedQueue = [];
};

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

axiosInstance.interceptors.request.use((config) => {
  const auth = localStorage.getItem("auth");

  if (auth) {
    const parsed = JSON.parse(auth);

    if (parsed?.accessToken) {
      config.headers.Authorization = `Bearer ${parsed.accessToken}`;
    }
  }

  return config;
});

axiosInstance.interceptors.response.use(
  (res) => res,
  async (error) => {
    const originalRequest = error.config;

    const status = error.response?.status;
    if (status !== 401) {
      return Promise.reject(error);
    }

    // already retried
    if (originalRequest._retry) {
      performLogout();
      window.location.href = "/login";
      return Promise.reject(error);
    }

    // queue requests while refreshing
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        })
        .catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const auth = JSON.parse(localStorage.getItem("auth") || "{}");

      if (!auth?.refreshToken) {
        throw new Error("No refresh token");
      }

      const refreshResponse = await refreshTokenApi(
        auth.refreshToken
      );

      const newAccessToken = refreshResponse.accessToken;
      const newRefreshToken = refreshResponse.refreshToken;

      localStorage.setItem(
        "auth",
        JSON.stringify({
          ...auth,
          accessToken: newAccessToken,
          refreshToken: newRefreshToken,
        })
      );

      store.dispatch(
        updateTokens({
          accessToken: newAccessToken,
          refreshToken: newRefreshToken,
        })
      );

      axiosInstance.defaults.headers.common.Authorization =
        `Bearer ${newAccessToken}`;

      processQueue(newAccessToken);

      originalRequest.headers.Authorization =
        `Bearer ${newAccessToken}`;

      return axiosInstance(originalRequest);
    } catch (err) {
      processQueue(null);

      showError("Session expired. Please login again.");
      performLogout();
      window.location.href = "/login";

      return Promise.reject(err);
    } finally {
      isRefreshing = false;
    }
  }
);

export default axiosInstance;

