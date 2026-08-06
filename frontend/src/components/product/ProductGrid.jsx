import ProductCard from "./ProductCard";

function ProductGrid({ products = [] }) {

  if (products.length === 0) {
    return (
      <div className="flex h-60 items-center justify-center rounded-xl border border-dashed border-white/15 bg-white/[0.03]">

        <div className="text-center">

          <h2 className="text-xl font-semibold text-slate-200">
            No Products Found
          </h2>

          <p className="mt-2 text-slate-500">
            Try changing your search or category filter.
          </p>

        </div>

      </div>
    );
  }

  return (

    <div
      className="
        grid
        gap-6
        sm:grid-cols-2
        lg:grid-cols-3
        xl:grid-cols-4
      "
    >

      {products.map((product) => (

        <ProductCard
          key={product.id}
          product={product}
        />

      ))}

    </div>

  );

}

export default ProductGrid;