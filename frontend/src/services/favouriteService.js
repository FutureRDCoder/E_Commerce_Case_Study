import apiClient from "../api/apiClient";

const FAVOURITE_BASE_URL = (tenantSlug) =>
  `/${tenantSlug}/favourites`;

export const getFavourites = async (tenantSlug) => {
  const response = await apiClient.get(
    FAVOURITE_BASE_URL(tenantSlug)
  );

  return response.data;
};

export const addFavourite = async (
  tenantSlug,
  productId
) => {
  const response = await apiClient.post(
    `${FAVOURITE_BASE_URL(tenantSlug)}/${productId}`
  );

  return response.data;
};

export const removeFavourite = async (
  tenantSlug,
  productId
) => {
  await apiClient.delete(
    `${FAVOURITE_BASE_URL(tenantSlug)}/${productId}`
  );
};