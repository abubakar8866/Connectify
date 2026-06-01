import axios from "axios";
import { performLogout } from "../utils/logout";

import { store } from "../store/store";
import { updateTokens } from "../features/auth/authSlice";
import { refreshTokenApi } from "./authApi";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// REQUEST INTERCEPTOR (ADD TOKEN AUTOMATICALLY)
axiosInstance.interceptors.request.use(
  (config) => {
    const auth = localStorage.getItem("auth");

    if (auth) {

      const parsed = JSON.parse(auth);

      if (parsed.accessToken) {
        config.headers.Authorization =
          `Bearer ${parsed.accessToken}`;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => response,

  async (error) => {

    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {

      originalRequest._retry = true;

      try {

          const auth = JSON.parse(localStorage.getItem("auth") || "{}");

        if (!auth) {
          throw new Error("Authentication missing");
        }

        const refreshToken =
          auth.refreshToken;

        const refreshResponse =
          await refreshTokenApi(refreshToken);

        const newAccessToken =
          refreshResponse.accessToken;

        const newRefreshToken =
          refreshResponse.refreshToken;

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

        originalRequest.headers.Authorization =
          `Bearer ${newAccessToken}`;

        return axiosInstance(originalRequest);

      } catch (refreshError) {

        performLogout();

        window.location.href = "/login";

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;

