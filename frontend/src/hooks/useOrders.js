import { useQuery } from "@tanstack/react-query";

import { getOrders } from "../services/orderService";

export function useOrders(
  tenantSlug
) {

  return useQuery({

    queryKey: [
      "orders",
      tenantSlug,
    ],

    queryFn: () =>
      getOrders(tenantSlug),

    enabled: !!tenantSlug,

  });

}