import apiClient from "../api/apiClient";

export const getNotifications = async () => {

  const response = await apiClient.get(
    "/api/notifications"
  );

  return response.data;
};

export const markNotificationAsRead = async (notificationId) => {

  const response = await apiClient.put(
    `/api/notifications/${notificationId}/read`
  );

  return response.data;
};

export const markAllNotificationsAsRead = async () => {

  const response = await apiClient.put(
    "/api/notifications/read-all"
  );

  return response.data;
};
