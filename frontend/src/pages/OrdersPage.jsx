import { useParams } from "react-router-dom";

import { useOrders } from "../hooks/useOrders";
import useAuthStore from "../store/authStore";

function OrdersPage() {

  const { tenantSlug } = useParams();

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

        {orders.map((order) => (

          <div
            key={order.id}
            className="card card-hover p-4 sm:p-6"
          >

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">

              <h2 className="font-display text-lg font-semibold text-white sm:text-xl">
                Order #{order.id}
              </h2>

              <span className="chip chip-success self-start sm:self-auto">
                {order.status}
              </span>

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

          </div>

        ))}

      </div>

    </section>

  );

}

export default OrdersPage;
