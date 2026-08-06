import { useQuery } from "@tanstack/react-query";

import { getTenantOrders } from "../services/orderService";

export function useTenantOrders(
  tenantSlug
) {

  return useQuery({

    queryKey: [
      "tenant-orders",
      tenantSlug,
    ],

    queryFn: () =>
      getTenantOrders(tenantSlug),

    enabled: !!tenantSlug,

  });

}