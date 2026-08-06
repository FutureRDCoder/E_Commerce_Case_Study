import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createProduct } from "../services/productService";

export function useCreateProduct() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      product,
    }) =>
      createProduct(
        tenantSlug,
        product
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