export const saveAuth = (data: any) => {
  localStorage.setItem("auth", JSON.stringify(data));
};

export const getAuth = () => {
  const auth = localStorage.getItem("auth");
  return auth ? JSON.parse(auth) : null;
};

export const clearAuth = () => {
  localStorage.removeItem("auth");
};

