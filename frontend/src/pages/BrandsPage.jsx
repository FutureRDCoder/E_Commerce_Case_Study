import BrandGrid from "../components/tenant/BrandGrid";
import { useTenants } from "../hooks/useTenants";

function BrandsPage() {

  const {
    data,
    isLoading,
    isError,
    error,
  } = useTenants();

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading brands...
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
          Brands
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          Browse Brands
        </h1>

        <p className="mt-2 text-slate-400">
          Choose your favourite brand to start shopping.
        </p>

      </div>

      <BrandGrid
        tenants={data?.content ?? []}
      />

    </section>

  );

}

export default BrandsPage;