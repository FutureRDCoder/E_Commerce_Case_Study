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

    onSuccess: () => {

      queryClient.invalidateQueries({
        queryKey: ["products"],
      });

      queryClient.invalidateQueries({
        queryKey: ["product"],
      });

      queryClient.invalidateQueries({
        queryKey: ["favourites"],
      });

    },

  });

}