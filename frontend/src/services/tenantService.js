import apiClient from "../api/apiClient";

export const getTenants = async (
  page = 0,
  size = 12
) => {

  const response = await apiClient.get(
    "/api/tenants",
    {
      params: {
        page,
        size,
      },
    }
  );

  return response.data;
};

export const createTenant = async (tenant) => {

  const response = await apiClient.post(
    "/api/platform/tenants",
    tenant
  );

  return response.data;
};

export const deleteTenant = async (tenantId) => {

  const response = await apiClient.delete(
    `/api/platform/tenants/${tenantId}`
  );

  return response.data;
};