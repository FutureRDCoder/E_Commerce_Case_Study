import apiClient from "../api/apiClient";

const PRODUCT_BASE_URL = (tenantSlug) =>
  tenantSlug ? `/${tenantSlug}/products` : '/api/public/products';

export const getProducts = async (
  tenantSlug,
  searchParams = {}
) => {
  const params = Object.entries(searchParams).reduce(
    (acc, [key, value]) => {
      if (
        value !== undefined &&
        value !== null &&
        value !== ""
      ) {
        acc[key] = value;
      }
      return acc;
    },
    {}
  );

  const response = await apiClient.get(
    PRODUCT_BASE_URL(tenantSlug),
    {
      params,
    }
  );

  return response.data;
};

export const getProductById = async (
  tenantSlug,
  productId
) => {
  const response = await apiClient.get(
    `${PRODUCT_BASE_URL(tenantSlug)}/${productId}`
  );

  return response.data;
};

export const createProduct = async (
  tenantSlug,
  product
) => {

  const response = await apiClient.post(
    `/${tenantSlug}/products`,
    product
  );

  return response.data;

};

export const updateProduct = async (
    tenantSlug,
    productId,
    product
) => {

    const response = await apiClient.put(
        `/${tenantSlug}/products/${productId}`,
        product
    );

    return response.data;

};

export const updateProductStock = async (
  tenantSlug,
  productId,
  stock
) => {

  const response = await apiClient.patch(
    `/${tenantSlug}/products/${productId}/stock`,
    stock
  );

  return response.data;

};

export const deleteProduct = async (
  tenantSlug,
  productId
) => {

  await apiClient.delete(
    `/${tenantSlug}/products/${productId}`
  );

};