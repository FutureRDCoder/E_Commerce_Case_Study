import { useMutation, useQueryClient } from "@tanstack/react-query";

import { deleteTenant } from "../services/tenantService";

export function useDeleteTenant() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: (tenantId) =>
      deleteTenant(tenantId),

    onSuccess: () => {

      queryClient.invalidateQueries({
        queryKey: ["tenants"],
      });

    },

  });

}
