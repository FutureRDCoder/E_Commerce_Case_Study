import apiClient from "../api/apiClient";

const CART_BASE_URL = (tenantSlug) =>
  `/${tenantSlug}/cart`;

export const getCart = async (tenantSlug) => {
  const response = await apiClient.get(
    CART_BASE_URL(tenantSlug)
  );

  return response.data;
};

export const addToCart = async (
  tenantSlug,
  item
) => {
  const response = await apiClient.post(
    CART_BASE_URL(tenantSlug),
    item
  );

  return response.data;
};

export const updateCartItem = async (
  tenantSlug,
  itemId,
  quantity
) => {
  const response = await apiClient.put(
    `${CART_BASE_URL(tenantSlug)}/${itemId}`,
    { quantity }
  );

  return response.data;
};

export const removeCartItem = async (
  tenantSlug,
  itemId
) => {
  await apiClient.delete(
    `${CART_BASE_URL(tenantSlug)}/${itemId}`
  );
};

export const clearCart = async (
  tenantSlug
) => {
  await apiClient.delete(
    CART_BASE_URL(tenantSlug)
  );
};
