import { useQuery } from "@tanstack/react-query";

import { getProductById } from "../services/productService";

export function useProduct(
  tenantSlug,
  productId
) {
  return useQuery({
    queryKey: [
      "product",
      tenantSlug,
      productId,
    ],

    queryFn: () =>
      getProductById(
        tenantSlug,
        productId
      ),

    enabled:
      !!tenantSlug &&
      !!productId,
  });
}