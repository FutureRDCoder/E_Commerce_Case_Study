import { useQuery } from "@tanstack/react-query";

import { getFavourites } from "../services/favouriteService";

export function useFavourites(tenantSlug) {
  return useQuery({
    queryKey: ["favourites", tenantSlug],
    queryFn: () => getFavourites(tenantSlug),
    enabled: !!tenantSlug,
  });
}