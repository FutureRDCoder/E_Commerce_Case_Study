import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import useAuthStore from "../store/authStore";
import {
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../services/notificationService";

export function useNotifications() {

  const isAuthenticated = useAuthStore(
    (state) => state.isAuthenticated
  );

  return useQuery({
    queryKey: ["notifications"],
    queryFn: getNotifications,
    enabled: isAuthenticated,
    refetchInterval: isAuthenticated ? 10000 : false,
  });
}

export function useMarkNotificationAsRead() {

  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId) =>
      markNotificationAsRead(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["notifications"],
      });
    },
  });
}

export function useMarkAllNotificationsAsRead() {

  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => markAllNotificationsAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["notifications"],
      });
    },
  });
}
