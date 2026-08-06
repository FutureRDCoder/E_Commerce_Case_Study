import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { createOrder } from "../services/orderService";

export function useCreateOrder() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      order,
    }) =>
      createOrder(
        tenantSlug,
        order
      ),

    onSuccess: () => {

      queryClient.invalidateQueries({
        queryKey: ["orders"],
      });

      queryClient.invalidateQueries({
        queryKey: ["products"],
      });

      queryClient.invalidateQueries({
        queryKey: ["cart"],
      });

    },

  });

}