import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { updateProductStock } from "../services/productService";

export function useUpdateProductStock() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
      stock,
    }) =>
      updateProductStock(
        tenantSlug,
        productId,
        stock
      ),

    onSuccess: (_, variables) => {

      queryClient.invalidateQueries({

        queryKey: [
          "products",
          variables.tenantSlug,
        ],

      });

    },

  });

}