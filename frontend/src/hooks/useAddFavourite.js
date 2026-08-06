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