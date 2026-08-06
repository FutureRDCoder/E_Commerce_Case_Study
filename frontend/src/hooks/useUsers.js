import { useQuery } from "@tanstack/react-query";

import { getUsers } from "../services/userService";

export function useUsers(page = 0, size = 100) {
  return useQuery({
    queryKey: ["users", page, size],
    queryFn: () => getUsers(page, size),
  });
}
