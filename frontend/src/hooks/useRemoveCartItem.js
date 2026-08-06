import toast from "react-hot-toast";

import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { removeCartItem } from "../services/cartService";

export function useRemoveCartItem() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      itemId,
    }) =>
      removeCartItem(
        tenantSlug,
        itemId
      ),

    onSuccess: () => {

      queryClient.invalidateQueries({
        queryKey: ["cart"],
      });

      toast.success("Item removed from cart");

    },

    onError: (error) => {
      toast.error(
        error.response?.data?.message ??
        error.message ??
        "Failed to remove item"
      );
    },

  });

}
