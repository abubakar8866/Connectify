import axiosInstance from "./axiosInstance";
import plainAxios from "./plainAxios";

export const loginApi = async (data: {
  email: string;
  password: string;
}) => {
  const response = await plainAxios.post("/auth/login", data);
  return response.data;
};

export const registerApi = async (data: any) => {
  const response = await plainAxios.post("/auth/register", data);
  return response.data;
};

export const refreshTokenApi = async (refreshToken: string) => {
    const response = await plainAxios.post("/auth/refresh-token",{refreshToken,});
    return response.data;
};

export const forgotPasswordApi = (data: { email: string }) => {
  return plainAxios.post("/auth/forgot-password", data);
};

export const logoutApi = async () => {
  const response = await axiosInstance.post("/auth/logout");
  return response.data;
};

export const resetPasswordApi = async (
  token: string,
  data: {
    password: string;
  }
) => {
  const response = await plainAxios.post(
    `/auth/reset-password?token=${token}`,
    data
  );

  return response.data;
};

