import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { removeFavourite } from "../services/favouriteService";

export function useRemoveFavourite() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
    }) =>
      removeFavourite(
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