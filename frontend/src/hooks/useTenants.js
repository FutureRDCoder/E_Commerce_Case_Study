import { useQuery } from "@tanstack/react-query";

import { getTenants } from "../services/tenantService";

export function useTenants(
  page = 0,
  size = 12
) {
  return useQuery({

    queryKey: [
      "tenants",
      page,
      size,
    ],

    queryFn: () =>
      getTenants(page, size),

  });
}