import DeleteProductButton from "../../features/products/components/DeleteProductButton";

function TenantProductTable({
  products = [],
  onEdit,
  _onDelete,
  onStock,
}) {

  if (products.length === 0) {

    return (
      <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-10 text-center text-slate-500">
        No products found.
      </div>
    );

  }

  return (

    <div className="card overflow-x-auto overflow-hidden">

      <table className="min-w-[640px] w-full">

        <thead className="bg-white/5">

          <tr>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Product
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Category
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Price
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Stock
            </th>

            <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider text-slate-400">
              Actions
            </th>

          </tr>

        </thead>

        <tbody>

          {products.map((product) => (

            <tr
              key={product.id}
              className="border-t border-white/5 text-slate-300 transition-colors hover:bg-white/[0.03]"
            >

              <td className="px-4 py-4">

                <div className="flex items-center gap-3">

                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="h-14 w-14 rounded-lg border border-white/10 object-cover"
                  />

                  <div>

                    <p className="font-semibold text-white">
                      {product.name}
                    </p>

                    <p className="text-sm text-slate-500">
                      #{product.id}
                    </p>

                  </div>

                </div>

              </td>

              <td className="px-4">
                {product.category}
              </td>

              <td className="px-4 font-medium text-primary-300">
                ₹{product.price}
              </td>

              <td className="px-4">
                {product.availableQuantity}
              </td>

              <td className="px-4">

                <div className="flex justify-center gap-2">

                  <button
                    onClick={() => onEdit(product)}
                    className="btn-primary px-3 py-2 text-sm"
                  >
                    Edit
                  </button>

                  <button
                    onClick={() => onStock(product)}
                    className="border border-yellow-500/40 bg-yellow-500/15 px-3 py-2 text-sm font-semibold text-yellow-300 transition hover:bg-yellow-500/25"
                  >
                    Stock
                  </button>

                  <DeleteProductButton
                    product={product}
                  />

                </div>

              </td>

            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default TenantProductTable;
