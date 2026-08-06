import { ShoppingCart } from "lucide-react";
import { useNavigate } from "react-router-dom";

import useAuthStore from "../../store/authStore";
import { useAddToCart } from "../../hooks/useAddToCart";

function AddToCartButton({
    tenantSlug,
    product,
    quantity = 1,
    variant = "icon",
}) {

    const navigate = useNavigate();

    const addToCart = useAddToCart();

    const isAuthenticated =
        useAuthStore((state) => state.isAuthenticated);

    const user = useAuthStore((state) => state.user);

    if (user?.role === "ADMIN") {
        return null;
    }

    const outOfStock =
        (product.availableQuantity ?? 0) <= 0;

    const handleClick = () => {
        if (!isAuthenticated) {
            navigate("/login");
            return;
        }

        addToCart.mutate({
            tenantSlug,
            productId: product.id,
            quantity,
        });
    };

    if (variant === "button") {
        return (
            <button
                onClick={handleClick}
                disabled={
                    addToCart.isPending ||
                    outOfStock
                }
                className={`
                    flex items-center justify-center gap-2 rounded-lg px-6 py-3
                    font-semibold transition
                    disabled:cursor-not-allowed disabled:opacity-60
                    ${outOfStock
                        ? "btn-secondary"
                        : "btn-primary"
                    }
                `}
            >
                <ShoppingCart className="h-5 w-5" />

                {outOfStock
                    ? "Out of Stock"
                    : "Add to Cart"}
            </button>
        );
    }

    return (
        <button
            onClick={handleClick}
            disabled={
                addToCart.isPending ||
                outOfStock
            }
            className="
                transition hover:scale-110
                disabled:cursor-not-allowed
                disabled:opacity-50
            "
            aria-label={`Add ${product.name} to cart`}
        >
            <ShoppingCart className="h-6 w-6 text-slate-500 transition hover:text-primary-400" />
        </button>
    );

}

export default AddToCartButton;
