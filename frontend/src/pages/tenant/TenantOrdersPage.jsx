import { useTenantOrders } from "../../hooks/useTenantOrders";

import TenantOrderTable from "../../components/tenant/TenantOrderTable";

import useAuthStore from "../../store/authStore";

function TenantOrdersPage() {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const {
    data: orders,
    isLoading,
    isError,
    error,
  } = useTenantOrders(
    tenantSlug
  );

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
          Tenancy
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          Order Management
        </h1>

        <p className="mt-2 text-slate-400">
          Orders placed for your products.
        </p>

      </div>

      <TenantOrderTable
        orders={orders}
      />

    </section>

  );

}

export default TenantOrdersPage;