import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { deleteProduct } from "../services/productService";

export function useDeleteProduct() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
    }) =>
      deleteProduct(
        tenantSlug,
        productId
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