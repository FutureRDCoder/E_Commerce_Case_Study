import { Package } from "lucide-react";

function OrderDetails({
  items = [],
}) {

  if (items.length === 0) {

    return (
      <div className="rounded-xl border border-white/10 bg-white/[0.02] p-6 text-center text-sm text-slate-500">
        No items in this order.
      </div>
    );

  }

  return (

    <div className="space-y-3">

      <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-400">
        Items Ordered
      </h3>

      {items.map((item) => (

        <div
          key={item.id}
          className="flex flex-col gap-4 rounded-xl border border-white/5 bg-white/[0.02] p-3 sm:flex-row sm:items-center"
        >

          {item.productImageUrl ? (

            <img
              src={item.productImageUrl}
              alt={item.productName}
              className="h-16 w-16 rounded-lg object-cover"
            />

          ) : (

            <div className="flex h-16 w-16 items-center justify-center rounded-lg bg-white/5">
              <Package className="h-6 w-6 text-slate-500" />
            </div>

          )}

          <div className="flex-1">
            <p className="font-semibold text-white">
              {item.productName}
            </p>

            <p className="text-sm text-slate-500">
              {item.productCategory}
            </p>

            <p className="text-sm text-slate-400">
              Unit Price: ₹{item.unitPrice}
            </p>
          </div>

          <div className="sm:text-right">
            <p className="text-sm text-slate-400">
              Qty: {item.quantity}
            </p>

            <p className="text-gradient font-bold">
              ₹{item.subtotal}
            </p>
          </div>

        </div>

      ))}

    </div>

  );

}

export default OrderDetails;
