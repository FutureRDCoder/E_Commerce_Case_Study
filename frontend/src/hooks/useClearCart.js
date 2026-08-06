import toast from "react-hot-toast";

import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { clearCart } from "../services/cartService";

export function useClearCart() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: (tenantSlug) =>
      clearCart(tenantSlug),

    onSuccess: (_, tenantSlug) => {

      queryClient.invalidateQueries({
        queryKey: [
          "cart",
          tenantSlug,
        ],
      });

    },

    onError: (error) => {
      toast.error(
        error.response?.data?.message ??
        error.message ??
        "Failed to clear cart"
      );
    },

  });

}
