import { Link } from "react-router-dom";

import AddToCartButton from "./AddToCartButton";
import FavouriteButton from "./FavouriteButton";
import useAuthStore from "../../store/authStore";

function ProductCard({ product }) {

  const user = useAuthStore((state) => state.user);

  return (

    <div className="card card-hover overflow-hidden">

      {/* Product Image */}

      <div className="relative">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="h-56 w-full object-cover"
        />

        <span
          className={`absolute left-3 top-3 rounded-full px-3 py-1 text-xs font-semibold ${product.availableQuantity > 0
            ? "chip chip-success"
            : "border border-red-500/40 bg-red-500/10 text-red-400"
            }`}
        >
          {product.availableQuantity > 0
            ? "In Stock"
            : "Out of Stock"}
        </span>
      </div>

      {/* Content */}

      <div className="space-y-3 p-5">

        <div className="flex items-start justify-between">

          <h2 className="line-clamp-2 text-lg font-semibold text-white">
            {product.name}
          </h2>

          {user?.role !== 'ADMIN' && (
            <FavouriteButton
              tenantSlug={product.tenantSlug}
              product={product}
            />
          )}

        </div>

        <p className="chip">
          {product.category}
        </p>

        <p className="line-clamp-2 text-sm text-slate-400">
          {product.description}
        </p>

        <div className="flex items-center justify-between border-t border-white/10 pt-3">

          <span className="text-gradient text-2xl font-bold">
            ₹{product.price}
          </span>

        </div>

        <div className="flex gap-3">

          <Link
            to={`/${product.tenantSlug}/products/${product.id}`}
            className="btn-secondary block flex-1 py-3 text-center"
          >
            View Details
          </Link>

          <AddToCartButton
            tenantSlug={product.tenantSlug}
            product={product}
            variant="button"
            quantity={1}
          />

        </div>

      </div>

    </div>

  );

}

export default ProductCard;
