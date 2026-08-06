import { useQuery } from "@tanstack/react-query";

import { getAllOrders } from "../services/orderService";

export function useAllOrders() {

  return useQuery({

    queryKey: ["all-orders"],

    queryFn: getAllOrders,

  });

}
