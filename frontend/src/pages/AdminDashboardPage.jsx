import { useState } from "react";

import { useTenants } from "../hooks/useTenants";
import { useAllOrders } from "../hooks/useAllOrders";

import AdminTenantTable from "../components/tenant/AdminTenantTable";
import AdminOrderTable from "../components/tenant/AdminOrderTable";
import CreateTenantForm from "../features/tenants/components/CreateTenantForm";
import AssignTenantForm from "../features/tenants/components/AssignTenantForm";

function AdminDashboardPage() {

  const [showCreateForm, setShowCreateForm] = useState(false);

  const {
    data: tenantsData,
    isLoading: tenantsLoading,
    isError: tenantsError,
    error: tenantsErrorObject,
  } = useTenants(0, 100);

  const {
    data: orders,
    isLoading: ordersLoading,
    isError: ordersError,
    error: ordersErrorObject,
  } = useAllOrders();

  return (

    <section className="space-y-12">

      <div>

        <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
          Administration
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          Admin Dashboard
        </h1>

        <p className="mt-2 text-slate-400">
          Manage brands and view all orders across tenants.
        </p>

      </div>

      {/* Brands */}

      <div className="space-y-6">

        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

          <div>

            <h2 className="text-2xl font-bold text-white">
              Brands
            </h2>

            <p className="mt-1 text-slate-400">
              Add or remove brands.
            </p>

          </div>

          <button
            onClick={() =>
              setShowCreateForm((value) => !value)
            }
            className="btn-primary px-6 py-3"
          >
            {showCreateForm ? "Close Form" : "Add Brand"}
          </button>

        </div>

        {showCreateForm && (

          <div className="card p-4 sm:p-8">

            <CreateTenantForm
              onSuccess={() => setShowCreateForm(false)}
            />

          </div>

        )}

        {tenantsLoading ? (
          <div className="py-16 text-center text-slate-400">
            Loading brands...
          </div>
        ) : tenantsError ? (
          <div className="py-16 text-center text-red-400">
            {tenantsErrorObject.message}
          </div>
        ) : (
          <AdminTenantTable
            tenants={tenantsData?.content ?? []}
          />
        )}

      </div>

      {/* Assign Brand Admin */}

      <div className="space-y-6">

        <div>

          <h2 className="text-2xl font-bold text-white">
            Assign Brand Admin
          </h2>

          <p className="mt-1 text-slate-400">
            Make a normal user a tenant admin for a brand.
          </p>

        </div>

        <div className="card p-4 sm:p-8">

          <AssignTenantForm />

        </div>

      </div>

      {/* All Orders */}

      <div className="space-y-6">

        <div>

          <h2 className="text-2xl font-bold text-white">
            All Orders
          </h2>

          <p className="mt-1 text-slate-400">
            Orders placed across all brands.
          </p>

        </div>

        {ordersLoading ? (
          <div className="py-16 text-center text-slate-400">
            Loading orders...
          </div>
        ) : ordersError ? (
          <div className="py-16 text-center text-red-400">
            {ordersErrorObject.message}
          </div>
        ) : (
          <AdminOrderTable orders={orders ?? []} />
        )}

      </div>

    </section>

  );

}

export default AdminDashboardPage;
