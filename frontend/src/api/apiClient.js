import axios from "axios";
import useAuthStore from "../store/authStore";

import { HTTP_STATUS } from "../utils/httpStatus";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// apiClient.interceptors.request.use(
//   (config) => {
//     const token = useAuthStore.getState().token;

//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//     }

//     return config;
//   },
//   (error) => Promise.reject(error)
// );

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;

  const isPublic =
    config.url?.startsWith("/api/tenants") ||
    config.url?.startsWith("/api/public/");

  if (token && !isPublic) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,

  (error) => {
    const requestSentToken = Boolean(
      error.config?.headers?.Authorization
    );

    if (
      error.response?.status === HTTP_STATUS.UNAUTHORIZED &&
      requestSentToken &&
      useAuthStore.getState().isAuthenticated
    ) {
      console.warn("Authentication expired.");
      useAuthStore.getState().logout();
    }

    return Promise.reject(error);
  }
);

export default apiClient;