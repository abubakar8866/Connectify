import { store } from "../store/store";
import { logout } from "../features/auth/authSlice";
import { stopAutoRefresh } from "./tokenRefreshScheduler";

export const performLogout = () => {
  localStorage.removeItem("auth");

  store.dispatch(logout());

  stopAutoRefresh();
};

