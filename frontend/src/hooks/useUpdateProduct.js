import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { updateProduct } from "../services/productService";

export function useUpdateProduct() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
      product,
    }) =>
      updateProduct(
        tenantSlug,
        productId,
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