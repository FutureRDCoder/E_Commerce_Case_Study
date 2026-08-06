import apiClient from "../api/apiClient";

export const getProfile = async () => {

  const response = await apiClient.get(
    "/api/auth/me"
  );

  return response.data;
};