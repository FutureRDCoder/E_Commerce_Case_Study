import { useState } from "react";
import { useParams } from "react-router-dom";

import { useProduct } from "../hooks/useProduct";

import AddToCartButton from "../components/product/AddToCartButton";
import FavouriteButton from "../components/product/FavouriteButton";

function ProductDetailsPage() {

    const {
        tenantSlug,
        productId,
    } = useParams();

    const [quantity, setQuantity] = useState(1);

    const {
        data: product,
        isLoading,
        isError,
        error,
    } = useProduct(
        tenantSlug,
        productId
    );

    if (isLoading) {
        return (
            <div className="py-16 text-center text-slate-400">
                Loading product...
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

    const maxQuantity = product.availableQuantity ?? 1;

    const handleQuantityChange = (event) => {
        const value = Number(event.target.value);

        if (Number.isNaN(value) || value < 1) {
            setQuantity(1);
            return;
        }

        setQuantity(Math.min(value, maxQuantity));
    };

    return (

        <section className="grid gap-8 lg:grid-cols-2 lg:gap-12">

            {/* Image */}

            <div className="card overflow-hidden">
                <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="w-full object-cover"
                />
            </div>

            {/* Details */}

            <div className="space-y-6">

                <span className="chip">
                    {product.category}
                </span>

                <h1 className="text-3xl font-bold text-white sm:text-4xl">
                    {product.name}
                </h1>

                <p className="text-slate-400">
                    {product.description}
                </p>

                <h2 className="text-gradient text-3xl font-bold sm:text-4xl">
                    ₹{product.price}
                </h2>

                <p className="text-slate-300">
                    Available Stock:
                    <strong className="ml-1 text-white">
                        {product.availableQuantity}
                    </strong>
                </p>

                <div className="flex flex-wrap items-center gap-4">

                    <label className="flex items-center gap-2 text-sm font-medium text-slate-300">
                        Quantity:
                        <input
                            type="number"
                            min="1"
                            max={maxQuantity}
                            value={quantity}
                            onChange={handleQuantityChange}
                            className="input w-20"
                        />
                    </label>

                    <AddToCartButton
                        tenantSlug={tenantSlug}
                        product={product}
                        variant="button"
                        quantity={quantity}
                    />

                </div>

                <FavouriteButton
                    tenantSlug={tenantSlug}
                    product={product}
                    variant="button"
                />

            </div>

        </section>

    );

}

export default ProductDetailsPage;
