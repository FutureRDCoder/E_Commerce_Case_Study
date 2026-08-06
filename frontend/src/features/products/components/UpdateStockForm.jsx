import { useForm } from "react-hook-form";
import toast from "react-hot-toast";

import { useUpdateProductStock } from "../../../hooks/useUpdateProductStock";
import useAuthStore from "../../../store/authStore";

function UpdateStockForm({
  product,
  onSuccess,
}) {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const updateStock = useUpdateProductStock();

  const {
    register,
    handleSubmit,
  } = useForm({

    defaultValues: {
      availableQuantity: product.availableQuantity,
    },

  });

  const submit = (data) => {

    updateStock.mutate(
      {
        tenantSlug,
        productId: product.id,
        stock: data,
      },
      {
        onSuccess: () => {

          toast.success("Stock updated successfully!");

          onSuccess?.();

        },

        onError: (error) => {

          toast.error(
            error.response?.data?.message ??
            "Failed to update stock."
          );

        },

      }
    );

  };

  return (

    <div className="card mt-8 p-4 shadow-card sm:p-8">

      <h2 className="mb-6 text-xl font-bold text-white sm:text-2xl">
        Update Stock
      </h2>

      <form
        onSubmit={handleSubmit(submit)}
        className="space-y-5"
      >

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Available Quantity
          </label>

          <input
            type="number"
            {...register("availableQuantity", {
              valueAsNumber: true,
            })}
            className="input"
          />

        </div>

        <button
          className="border border-yellow-500/40 bg-yellow-500/15 px-6 py-3 font-semibold text-yellow-300 transition hover:bg-yellow-500/25"
        >
          Update Stock
        </button>

      </form>

    </div>

  );

}

export default UpdateStockForm;
