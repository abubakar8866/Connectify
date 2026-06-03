import axiosInstance from "./axiosInstance";

export const getCurrentUserApi = async () => {
  const res = await axiosInstance.get("/auth/me");
  return res.data;
};

export const updateProfileApi = async (
  userId: number,
  data: any,
  file?: File | null
) => {
  const formData = new FormData();

  formData.append(
    "data",
    new Blob([JSON.stringify(data)], {
      type: "application/json",
    })
  );

  if (file) {
    formData.append("file", file);
  }

  const res = await axiosInstance.put(
    `/auth/update-profile/${userId}`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return res.data;
};

