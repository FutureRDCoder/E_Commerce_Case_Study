import { useQuery } from "@tanstack/react-query";

import { getProducts } from "../services/productService";

export function useProducts(
  tenantSlug,
  searchParams
) {
  return useQuery({
    queryKey: [
      "products",
      tenantSlug,
      JSON.stringify(searchParams),
    ],

    queryFn: () =>
      getProducts(
        tenantSlug,
        searchParams
      ),

    enabled: true,
  });
}