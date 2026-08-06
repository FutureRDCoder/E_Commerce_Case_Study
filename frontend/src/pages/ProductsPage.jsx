import { useState } from "react";
import { useParams } from "react-router-dom";

import ProductGrid from "../components/product/ProductGrid";
import { useProducts } from "../hooks/useProducts";

import useDebounce from "../hooks/useDebounce";

import CategoryFilter from "../components/product/CategoryFilter";

import PriceFilter, {
  PRICE_RANGES,
} from "../components/product/PriceFilter";

import SearchBar from "../components/product/SearchBar";

import Pagination from "../components/common/Pagination";

function ProductsPage() {

  const { tenantSlug } = useParams();

  const [filters, setFilters] = useState({
    page: 0,
    size: 12,
    search: "",
    category: "",
    priceRange: "all",
  });

  const debouncedSearch = useDebounce(
    filters.search,
    500
  );

  const priceRange =
    PRICE_RANGES[filters.priceRange] ??
    PRICE_RANGES.all;

  const {
    data,
    isLoading,
    isError,
    error,
  } = useProducts(
    tenantSlug,
    {
      page: filters.page,
      size: filters.size,
      search: debouncedSearch,
      category: filters.category,
      minPrice: priceRange.minPrice,
      maxPrice: priceRange.maxPrice,
    }
  );

  const categories = [
    "Smartphones",
    "Footwear",
    "Audio",
    "Apparel",
    "Furniture",
    "Gaming",
  ];

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

      <div>

        <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
          Store
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          Products
        </h1>

        <p className="mt-2 text-slate-400">
          Browse products from our store.
        </p>

      </div>

      <div className="grid gap-4 rounded-2xl border border-white/10 bg-white/[0.03] p-4 md:grid-cols-2 lg:grid-cols-3">

        <SearchBar
          value={filters.search}
          onChange={(event) =>
            setFilters((previous) => ({
              ...previous,
              page: 0,
              search: event.target.value,
            }))
          }
        />

        <CategoryFilter
          categories={categories}
          value={filters.category}
          onChange={(event) =>
            setFilters((previous) => ({
              ...previous,
              page: 0,
              category: event.target.value,
            }))
          }
        />

        <PriceFilter
          value={filters.priceRange}
          onChange={(event) =>
            setFilters((previous) => ({
              ...previous,
              page: 0,
              priceRange: event.target.value,
            }))
          }
        />

      </div>

      <ProductGrid
        products={data?.content ?? []}
      />

      <Pagination
        page={data?.number ?? 0}
        totalPages={data?.totalPages ?? 0}
        onPageChange={(page) =>
          setFilters((previous) => ({
            ...previous,
            page,
          }))
        }
      />

    </section>

  );

}

export default ProductsPage;
