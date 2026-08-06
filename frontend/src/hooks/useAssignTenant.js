import { useMutation, useQueryClient } from "@tanstack/react-query";

import { assignTenantToUser } from "../services/userService";

export function useAssignTenant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, tenantId }) =>
      assignTenantToUser(userId, tenantId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });
    },
  });
}
