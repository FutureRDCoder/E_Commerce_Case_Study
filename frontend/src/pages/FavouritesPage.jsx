import { useParams } from "react-router-dom";

import ProductGrid from "../components/product/ProductGrid";
import { useFavourites } from "../hooks/useFavourites";

function FavouritesPage() {

  const { tenantSlug = "global" } = useParams();

  const {
    data: favourites,
    isLoading,
    isError,
    error,
  } = useFavourites(tenantSlug);

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading favourites...
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
          Saved
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Favourites
        </h1>

        <p className="mt-2 text-slate-400">
          Products you've added to your favourites.
        </p>

      </div>

      <ProductGrid
        products={favourites}
      />

    </section>

  );

}

export default FavouritesPage;