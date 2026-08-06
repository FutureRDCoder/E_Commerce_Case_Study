import { useState } from "react";
import { useParams } from "react-router-dom";
import { ChevronDown, ChevronUp } from "lucide-react";

import { useOrders } from "../hooks/useOrders";
import useAuthStore from "../store/authStore";
import OrderDetails from "../components/order/OrderDetails";

function OrdersPage() {

  const { tenantSlug = "global" } = useParams();

  const [expandedOrderId, setExpandedOrderId] = useState(null);

  const user = useAuthStore((state) => state.user);

  const {
    data: orders,
    isLoading,
    isError,
    error,
  } = useOrders(tenantSlug);

  if (user?.role === "ADMIN") {
    return (
      <section className="space-y-8">
        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Orders
        </h1>

        <div className="flex h-60 items-center justify-center rounded-2xl border border-dashed border-white/15 bg-white/[0.03]">
          <p className="text-lg text-slate-500">
            Admin accounts cannot place orders.
          </p>
        </div>
      </section>
    );
  }

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading orders...
      </div>
    );
  }

  if (isError) {
    return (
      <div className="py-16 text-center text-red-400">
        {error.message}
      </div>
    );
  }

  return (

    <section className="space-y-8">

      <div>

        <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
          History
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Orders
        </h1>

        <p className="mt-2 text-slate-400">
          Your previous purchases.
        </p>

      </div>

      <div className="space-y-6">

        {orders.map((order) => {

          const isExpanded = expandedOrderId === order.id;

          const toggleOrder = () =>
            setExpandedOrderId((current) =>
              current === order.id ? null : order.id
            );

          return (

            <div
              key={order.id}
              onClick={toggleOrder}
              role="button"
              tabIndex={0}
              aria-expanded={isExpanded}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  toggleOrder();
                }
              }}
              className="card card-hover cursor-pointer p-4 sm:p-6"
            >

              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">

                <h2 className="font-display text-lg font-semibold text-white sm:text-xl">
                  Order #{order.id}
                </h2>

                <div className="flex items-center gap-2 self-start sm:self-auto">

                  <span className="chip chip-success">
                    {order.status}
                  </span>

                  {isExpanded ? (
                    <ChevronUp className="h-5 w-5 text-slate-400" />
                  ) : (
                    <ChevronDown className="h-5 w-5 text-slate-400" />
                  )}

                </div>

              </div>

              <div className="mt-4 grid gap-1 text-sm text-slate-300 sm:grid-cols-3">
                <p>
                  Total Items: {order.totalQuantity}
                </p>

                <p>
                  Total Amount: ₹{order.totalAmount}
                </p>

                <p>
                  Order Date: {order.orderDate}
                </p>
              </div>

              {isExpanded && (

                <div className="mt-5 border-t border-white/10 pt-5">
                  <OrderDetails
                    items={order.items}
                  />
                </div>

              )}

            </div>

          );

        })}

      </div>

    </section>

  );

}

export default OrdersPage;
