import { useNavigate, useParams } from "react-router-dom";
import toast from "react-hot-toast";

import { Minus, Plus, Trash2 } from "lucide-react";

import { useCart } from "../hooks/useCart";
import { useUpdateCartItem } from "../hooks/useUpdateCartItem";
import { useRemoveCartItem } from "../hooks/useRemoveCartItem";
import { useClearCart } from "../hooks/useClearCart";
import { useCreateOrder } from "../hooks/useCreateOrder";
import useAuthStore from "../store/authStore";

function CartPage() {

  const { tenantSlug } = useParams();

  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);

  const {
    data: cart,
    isLoading,
    isError,
    error,
  } = useCart(tenantSlug);

  const updateCartItem = useUpdateCartItem();
  const removeCartItem = useRemoveCartItem();
  const clearCart = useClearCart();
  const createOrder = useCreateOrder();

  const emptyState = (message) => (
    <div className="flex h-60 items-center justify-center rounded-2xl border border-dashed border-white/15 bg-white/[0.03]">
      <p className="text-lg text-slate-500">
        {message}
      </p>
    </div>
  );

  if (user?.role === "ADMIN") {
    return (
      <section className="space-y-8">
        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Cart
        </h1>

        {emptyState("Admin accounts cannot add products to a cart or place orders.")}
      </section>
    );
  }

  if (isLoading) {
    return (
      <div className="py-16 text-center text-slate-400">
        Loading cart...
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

  const totalQuantity = cart.reduce(
    (sum, item) => sum + item.quantity,
    0
  );

  const totalAmount = cart.reduce(
    (sum, item) => sum + Number(item.subtotal),
    0
  );

  const handleDecrease = (item) => {
    if (item.quantity <= 1) {
      return;
    }

    updateCartItem.mutate({
      tenantSlug,
      itemId: item.id,
      quantity: item.quantity - 1,
    });
  };

  const handleIncrease = (item) => {
    if (item.quantity >= item.availableQuantity) {
      toast.error(
        `Only ${item.availableQuantity} units available in stock`
      );
      return;
    }

    updateCartItem.mutate({
      tenantSlug,
      itemId: item.id,
      quantity: item.quantity + 1,
    });
  };

  const handleRemove = (item) => {
    removeCartItem.mutate({
      tenantSlug,
      itemId: item.id,
    });
  };

  const handlePlaceOrder = async () => {
    try {
      await createOrder.mutateAsync({
        tenantSlug,
        order: {
          items: cart.map((item) => ({
            productId: item.productId,
            quantity: item.quantity,
          })),
        },
      });

      clearCart.mutate(tenantSlug);

      toast.success("Order placed successfully");

      navigate(`/${tenantSlug}/orders`);
    } catch (orderError) {
      toast.error(
        orderError.response?.data?.message ??
        orderError.message ??
        "Failed to place order"
      );
    }
  };

  if (cart.length === 0) {
    return (
      <section className="space-y-8">
        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Cart
        </h1>

        {emptyState("Your cart is empty.")}
      </section>
    );
  }

  return (

    <section className="space-y-8">

      <div>
        <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">
          Cart
        </p>

        <h1 className="text-3xl font-bold text-white sm:text-4xl">
          My Cart
        </h1>

        <p className="mt-2 text-slate-400">
          Review the items in your cart.
        </p>
      </div>

      <div className="grid gap-8 lg:grid-cols-3">

        <div className="space-y-4 lg:col-span-2">

          {cart.map((item) => (

            <div
              key={item.id}
              className="card flex flex-col gap-4 p-4 sm:flex-row sm:items-center"
            >

              <img
                src={item.productImageUrl}
                alt={item.productName}
                className="h-24 w-24 rounded-lg object-cover"
              />

              <div className="flex-1">
                <h2 className="text-lg font-semibold text-white">
                  {item.productName}
                </h2>

                <p className="text-sm text-slate-500">
                  {item.productCategory}
                </p>

                <p className="mt-1 text-sm text-slate-400">
                  Unit Price: ₹{item.unitPrice}
                </p>
              </div>

              <div className="flex items-center gap-3">

                <button
                  onClick={() => handleDecrease(item)}
                  disabled={item.quantity <= 1 || updateCartItem.isPending}
                  className="btn-secondary p-2 disabled:cursor-not-allowed disabled:opacity-50"
                  aria-label="Decrease quantity"
                >
                  <Minus className="h-4 w-4" />
                </button>

                <span className="w-8 text-center font-semibold text-white">
                  {item.quantity}
                </span>

                <button
                  onClick={() => handleIncrease(item)}
                  disabled={
                    item.quantity >= item.availableQuantity ||
                    updateCartItem.isPending
                  }
                  className="btn-secondary p-2 disabled:cursor-not-allowed disabled:opacity-50"
                  aria-label="Increase quantity"
                >
                  <Plus className="h-4 w-4" />
                </button>

              </div>

              <div className="sm:w-28 sm:text-right">
                <p className="text-gradient font-bold">
                  ₹{item.subtotal}
                </p>
              </div>

              <button
                onClick={() => handleRemove(item)}
                disabled={removeCartItem.isPending}
                className="btn-danger p-2 disabled:cursor-not-allowed disabled:opacity-50"
                aria-label="Remove item"
              >
                <Trash2 className="h-5 w-5" />
              </button>

            </div>

          ))}

        </div>

        <div className="card space-y-4 self-start p-6 shadow-glow">

          <h2 className="font-display text-xl font-bold text-white">
            Order Summary
          </h2>

          <div className="space-y-2 text-sm">

            <div className="flex justify-between">
              <span className="text-slate-400">
                Total Items
              </span>
              <span className="font-semibold text-white">
                {totalQuantity}
              </span>
            </div>

            <div className="flex justify-between border-t border-white/10 pt-2 text-base">
              <span className="font-semibold text-slate-200">
                Total Amount
              </span>
              <span className="text-gradient font-bold">
                ₹{totalAmount.toFixed(2)}
              </span>
            </div>

          </div>

          <button
            onClick={handlePlaceOrder}
            disabled={
              createOrder.isPending ||
              clearCart.isPending
            }
            className="btn-primary w-full py-3"
          >
            {createOrder.isPending
              ? "Placing Order..."
              : "Place Order"}
          </button>

        </div>

      </div>

    </section>

  );

}

export default CartPage;
