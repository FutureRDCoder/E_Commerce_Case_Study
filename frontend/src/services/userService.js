import apiClient from "../api/apiClient";

export const getUsers = async (page = 0, size = 100) => {
  const response = await apiClient.get("/api/platform/users", {
    params: {
      page,
      size,
      role: "USER",
    },
  });

  return response.data;
};

export const assignTenantToUser = async (userId, tenantId) => {
  const response = await apiClient.put(
    `/api/platform/users/${userId}/tenant`,
    { tenantId }
  );

  return response.data;
};
