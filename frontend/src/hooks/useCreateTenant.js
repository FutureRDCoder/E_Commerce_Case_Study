import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createTenant } from "../services/tenantService";

export function useCreateTenant() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: (tenant) =>
      createTenant(tenant),

    onSuccess: () => {

      queryClient.invalidateQueries({
        queryKey: ["tenants"],
      });

    },

  });

}
