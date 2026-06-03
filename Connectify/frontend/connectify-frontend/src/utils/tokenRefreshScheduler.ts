import { refreshTokenApi } from "../api/authApi";
import { store } from "../store/store";
import { updateTokens } from "../features/auth/authSlice";
import { showError } from "./toast";
import { performLogout } from "./logout";

let refreshTimer: NodeJS.Timeout | null = null;

// Decode JWT to get expiry time
const getTokenExpiry = (token: string): number => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.exp * 1000; // convert to ms
  } catch {
    return 0;
  }
};

// Schedule refresh 1 minute before expiry
export const scheduleAutoRefresh = (
  accessToken: string,
  refreshToken: string
) => {
  if (refreshTimer) clearTimeout(refreshTimer);

  const expiryTime = getTokenExpiry(accessToken);
  const now = Date.now();

  const refreshTime = expiryTime - now - 60 * 1000; // 1 min before expiry

  if (refreshTime <= 0) {
    console.warn("Token already expired or too close");
    return;
  }

  refreshTimer = setTimeout(async () => {
    try {
      const res = await refreshTokenApi(refreshToken);

      const newAccess = res.accessToken;
      const newRefresh = res.refreshToken;

      // update redux
      store.dispatch(
        updateTokens({
          accessToken: newAccess,
          refreshToken: newRefresh,
        })
      );

      // update localStorage
      const auth = JSON.parse(localStorage.getItem("auth") || "{}");

      localStorage.setItem(
        "auth",
        JSON.stringify({
          ...auth,
          accessToken: newAccess,
          refreshToken: newRefresh,
        })
      );

      // reschedule again
      scheduleAutoRefresh(newAccess, newRefresh);
    } catch (err) {
      showError("Session expired. Please login again.");
      performLogout();
      window.location.href = "/login";
    }
  }, refreshTime);
};

// Stop auto refresh (on logout)
export const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer);
    refreshTimer = null;
  }
};

