function AdminOrderTable({
  orders = [],
}) {

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
              Brand
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

          {orders.map((order) => (

            <tr
              key={order.id}
              className="border-t border-white/5 text-slate-300 transition-colors hover:bg-white/[0.03]"
            >

              <td className="px-4 py-4 font-medium text-white">
                #{order.id}
              </td>

              <td className="px-4">
                {order.tenantName}
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

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default AdminOrderTable;
