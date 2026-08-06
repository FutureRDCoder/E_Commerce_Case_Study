import toast from "react-hot-toast";

import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { updateCartItem } from "../services/cartService";

export function useUpdateCartItem() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      itemId,
      quantity,
    }) =>
      updateCartItem(
        tenantSlug,
        itemId,
        quantity
      ),

    onSuccess: (_, variables) => {

      queryClient.invalidateQueries({
        queryKey: [
          "cart",
          variables.tenantSlug,
        ],
      });

    },

    onError: (error) => {
      toast.error(
        error.response?.data?.message ??
        error.message ??
        "Failed to update cart"
      );
    },

  });

}
