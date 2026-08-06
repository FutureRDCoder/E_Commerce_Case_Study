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

    onSuccess: (_, variables) => {

      queryClient.invalidateQueries({
        queryKey: [
          "orders",
          variables.tenantSlug,
        ],
      });

      queryClient.invalidateQueries({
        queryKey: [
          "products",
          variables.tenantSlug,
        ],
      });

    },

  });

}