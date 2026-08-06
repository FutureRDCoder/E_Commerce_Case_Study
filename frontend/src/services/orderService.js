import apiClient from "../api/apiClient";

const ORDER_BASE_URL = (tenantSlug) =>
  `/${tenantSlug}/orders`;

export const createOrder = async (
  tenantSlug,
  order
) => {

  const response = await apiClient.post(
    ORDER_BASE_URL(tenantSlug),
    order
  );

  return response.data;
};

export const getOrders = async (
  tenantSlug
) => {

  const response = await apiClient.get(
    `${ORDER_BASE_URL(tenantSlug)}/my-history`
  );

  return response.data;
};

export const getTenantOrders = async (
  tenantSlug
) => {

  const response = await apiClient.get(
    `/${tenantSlug}/orders`
  );

  return response.data;

};

export const getAllOrders = async () => {

  const response = await apiClient.get(
    "/api/platform/orders"
  );

  return response.data;

};