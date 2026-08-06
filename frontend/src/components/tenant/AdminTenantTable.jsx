import DeleteTenantButton from "./DeleteTenantButton";

function AdminTenantTable({
  tenants = [],
}) {

  if (tenants.length === 0) {

    return (
      <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-10 text-center text-slate-500">
        No brands found.
      </div>
    );

  }

  return (

    <div className="card overflow-x-auto overflow-hidden">

      <table className="min-w-[640px] w-full">

        <thead className="bg-white/5">

          <tr>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Brand
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Slug
            </th>

            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-400">
              Description
            </th>

            <th className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider text-slate-400">
              Actions
            </th>

          </tr>

        </thead>

        <tbody>

          {tenants.map((tenant) => (

            <tr
              key={tenant.id}
              className="border-t border-white/5 text-slate-300 transition-colors hover:bg-white/[0.03]"
            >

              <td className="px-4 py-4">

                <div className="flex items-center gap-3">

                  {tenant.logoUrl ? (
                    <img
                      src={tenant.logoUrl}
                      alt={tenant.name}
                      className="h-10 w-10 rounded-full border border-white/10 object-cover"
                    />
                  ) : (
                    <div className="bg-gradient-brand flex h-10 w-10 items-center justify-center rounded-full text-white">
                      {tenant.name?.charAt(0)}
                    </div>
                  )}

                  <div>

                    <p className="font-semibold text-white">
                      {tenant.name}
                    </p>

                    <p className="text-sm text-slate-500">
                      #{tenant.id}
                    </p>

                  </div>

                </div>

              </td>

              <td className="px-4">
                {tenant.slug}
              </td>

              <td className="px-4">
                {tenant.description}
              </td>

              <td className="px-4">

                <div className="flex justify-center gap-2">

                  <DeleteTenantButton tenant={tenant} />

                </div>

              </td>

            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default AdminTenantTable;
