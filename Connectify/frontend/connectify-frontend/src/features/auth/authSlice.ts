import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export interface User {
  id: number;
  name: string;
  uname: string;
  email: string;

  bio: string | null;
  profileImageUrl: string | null;

  role: string;
  gender: string | null;

  languages: string[];

  dateOfBirth: string | null;
  age: number | null;

  city: string | null;

  accountStatus: string;
  provider: string;

  isActive: boolean;
  isEmailVerified: boolean;

  createdAt: string;
  updatedAt: string;
  lastLoginAt: string | null;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
}

const savedAuth = localStorage.getItem("auth");

const parsedAuth = savedAuth
  ? JSON.parse(savedAuth)
  : null;

const initialState: AuthState = {
  user: parsedAuth?.user ?? null,
  accessToken: parsedAuth?.accessToken ?? null,
  refreshToken: parsedAuth?.refreshToken ?? null,
  isAuthenticated: !!parsedAuth?.accessToken,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {

    hydrateAuth: (
      state,
      action: PayloadAction<{
        user: User;
        accessToken: string;
        refreshToken: string;
      }>
    ) => {
      state.user = action.payload.user;
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      state.isAuthenticated = true;
    },

    loginSuccess: (
      state,
      action: PayloadAction<{
        user: User;
        accessToken: string;
        refreshToken: string;
      }>
    ) => {
      state.user = action.payload.user;
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      state.isAuthenticated = true;
    },

    updateTokens: (
        state,
        action: PayloadAction<{
          accessToken: string;
          refreshToken: string;
        }>
    ) => {
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
    },

    logout: (state) => {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
    },
  },
});

export const {
  loginSuccess,
  logout,
  hydrateAuth,
  updateTokens,
} = authSlice.actions;
export default authSlice.reducer;

