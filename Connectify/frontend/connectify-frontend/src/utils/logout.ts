import { store } from "../store/store";
import { logout } from "../features/auth/authSlice";

export const performLogout = () => {

  localStorage.removeItem("auth");

  store.dispatch(logout());
};

