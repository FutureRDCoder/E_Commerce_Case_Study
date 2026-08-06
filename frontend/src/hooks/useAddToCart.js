import toast from "react-hot-toast";

import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { addToCart } from "../services/cartService";

export function useAddToCart() {

  const queryClient = useQueryClient();

  return useMutation({

    mutationFn: ({
      tenantSlug,
      productId,
      quantity,
    }) =>
      addToCart(
        tenantSlug,
        { productId, quantity }
      ),

    onSuccess: (data) => {

      queryClient.invalidateQueries({
        queryKey: ["cart"],
      });

      toast.success(
        `${data.productName} added to cart`
      );

    },

    onError: (error) => {
      toast.error(
        error.response?.data?.message ??
        error.message ??
        "Failed to add to cart"
      );
    },

  });

}
