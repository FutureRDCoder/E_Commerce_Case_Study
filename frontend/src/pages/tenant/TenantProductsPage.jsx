import { useMemo, useState } from "react";

import useAuthStore from "../../store/authStore";

import { useProducts } from "../../hooks/useProducts";

import TenantProductTable from "../../components/tenant/TenantProductTable";

import CreateProductModal from "../../features/products/components/CreateProductModal";
import EditProductModal from "../../features/products/components/EditProductModal";
import UpdateStockForm from "../../features/products/components/UpdateStockForm";

function TenantProductsPage() {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const [showCreateForm, setShowCreateForm] = useState(false);

  const [editingProduct, setEditingProduct] = useState(null);

  const [stockProduct, setStockProduct] = useState(null);

  const filters = useMemo(
    () => ({
      page: 0,
      size: 100,
      search: "",
      category: "",
    }),
    []
  );

  const {
    data,
    isLoading,
    isError,
    error,
  } = useProducts(
    tenantSlug,
    filters
  );

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading products...
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

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div>

          <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
            Tenancy
          </p>

          <h1 className="text-3xl font-bold text-white sm:text-4xl">
            Product Management
          </h1>

          <p className="mt-2 text-slate-400">
            Manage your products.
          </p>

        </div>

        <button
          onClick={() => {
            setEditingProduct(null);
            setShowCreateForm(true);
          }}
          className="btn-primary px-6 py-3"
        >
          Add Product
        </button>

      </div>

      <TenantProductTable
        products={data?.content ?? []}
        onEdit={(product) => {
          setShowCreateForm(false);
          setEditingProduct(product);
        }}
        onDelete={(product) =>
          console.log("Delete", product)
        }
        onStock={(product) => {

          setShowCreateForm(false);

          setEditingProduct(null);

          setStockProduct(product);

        }}
      />

      {showCreateForm && (

        <div className="mt-8">

          <CreateProductModal
            onSuccess={() => setShowCreateForm(false)}
          />

        </div>

      )}

      {
        editingProduct && (

          <EditProductModal
            product={editingProduct}
            onSuccess={() =>
              setEditingProduct(null)
            }
          />

        )
      }

      {
        stockProduct && (

          <UpdateStockForm
            product={stockProduct}
            onSuccess={() =>
              setStockProduct(null)
            }
          />

        )
      }

    </section>

  );

}

export default TenantProductsPage;