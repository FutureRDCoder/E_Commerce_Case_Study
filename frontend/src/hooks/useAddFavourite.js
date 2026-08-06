import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { addFavourite } from "../services/favouriteService";

export function useAddFavourite() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
    }) =>
      addFavourite(
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

      queryClient.invalidateQueries({
        queryKey: [
          "product",
          variables.tenantSlug,
          variables.productId,
        ],
      });

      queryClient.invalidateQueries({
        queryKey: [
          "favourites",
          variables.tenantSlug,
        ],
      });

    },

  });

}