import { Heart } from "lucide-react";

import { useAddFavourite } from "../../hooks/useAddFavourite";
import { useRemoveFavourite } from "../../hooks/useRemoveFavourite";

function FavouriteButton({
    tenantSlug,
    product,
    variant = "icon",
}) {

    const addFavourite = useAddFavourite();
    const removeFavourite = useRemoveFavourite();

    const handleClick = () => {

        if (product.isFavourite) {

            removeFavourite.mutate({
                tenantSlug,
                productId: product.id,
            });

            return;
        }

        addFavourite.mutate({
            tenantSlug,
            productId: product.id,
        });
    };

    if (variant === "button") {
        return (
            <button
                onClick={handleClick}
                disabled={
                    addFavourite.isPending ||
                    removeFavourite.isPending
                }
                className={`
        flex items-center gap-2 rounded-lg px-6 py-3
        font-semibold transition
        disabled:cursor-not-allowed disabled:opacity-60
        ${product.isFavourite
                        ? "border border-red-500/40 bg-red-500/15 text-red-300 hover:bg-red-500/25"
                        : "btn-primary"
                    }
      `}
            >
                <Heart
                    className={`h-5 w-5 ${product.isFavourite ? "fill-red-400 text-red-400" : ""
                        }`}
                />

                {product.isFavourite
                    ? "Remove from Favourites"
                    : "Add to Favourites"}
            </button>
        );
    }

    return (
        <button
            onClick={handleClick}
            disabled={
                addFavourite.isPending ||
                removeFavourite.isPending
            }
            className="
      transition hover:scale-110
      disabled:cursor-not-allowed
      disabled:opacity-60
    "
        >
            <Heart
                className={`h-6 w-6 transition ${product.isFavourite
                        ? "fill-red-500 text-red-500"
                        : "text-slate-500 hover:text-red-500"
                    }`}
            />
        </button>
    );

}

export default FavouriteButton;
