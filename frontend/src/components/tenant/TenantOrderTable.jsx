import { Fragment, useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";

import OrderDetails from "../order/OrderDetails";

function TenantOrderTable({
  orders = [],
}) {

  const [expandedOrderId, setExpandedOrderId] = useState(null);

  if (orders.length === 0) {

    return (
      <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-10 text-center text-slate-500">
        No orders found.
      </div>
    );

  }

  return (

    <div className="card overflow-x-auto overflow-hidden">

      <table className="min-w-[640px] w-full">

        <thead className="bg-white/5">

          <tr>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Order
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Customer
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Date
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Status
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Quantity
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Total
            </th>

          </tr>

        </thead>

        <tbody>

          {orders.map((order) => {

            const isExpanded = expandedOrderId === order.id;

            const toggleOrder = () =>
              setExpandedOrderId((current) =>
                current === order.id ? null : order.id
              );

            return (

              <Fragment key={order.id}>

                <tr
                  onClick={toggleOrder}
                  className="cursor-pointer border-t border-white/5 text-slate-300 transition-colors hover:bg-white/[0.03]"
                >

                  <td className="px-4 py-4">
                    <span className="flex items-center gap-2 font-medium text-white">
                      #{order.id}

                      {isExpanded ? (
                        <ChevronUp className="h-4 w-4 text-slate-400" />
                      ) : (
                        <ChevronDown className="h-4 w-4 text-slate-400" />
                      )}
                    </span>
                  </td>

                  <td className="px-4">
                    {order.userFullName}
                  </td>

                  <td className="px-4">
                    {new Date(
                      order.orderDate
                    ).toLocaleDateString()}
                  </td>

                  <td className="px-4">
                    <span className="chip chip-success">
                      {order.status}
                    </span>
                  </td>

                  <td className="px-4">
                    {order.totalQuantity}
                  </td>

                  <td className="px-4 font-semibold text-primary-300">
                    ₹{order.totalAmount}
                  </td>

                </tr>

                {isExpanded && (

                  <tr className="border-t border-white/5 bg-white/[0.02]">

                    <td
                      colSpan={6}
                      className="px-4 py-5"
                    >
                      <OrderDetails
                        items={order.items}
                      />
                    </td>

                  </tr>

                )}

              </Fragment>

            );

          })}

        </tbody>

      </table>

    </div>

  );

}

export default TenantOrderTable;
