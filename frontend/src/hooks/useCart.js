import { useQuery } from "@tanstack/react-query";

import { getCart } from "../services/cartService";

export function useCart(tenantSlug) {
  return useQuery({
    queryKey: ["cart", tenantSlug],
    queryFn: () => getCart(tenantSlug),
    enabled: !!tenantSlug,
  });
}
