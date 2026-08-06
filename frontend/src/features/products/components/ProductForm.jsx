import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { productSchema } from "../schemas/productSchema";

function ProductForm({
  defaultValues,
  onSubmit,
  submitText = "Save",
}) {

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({

    resolver: zodResolver(productSchema),

    defaultValues,

  });

  return (

    <form
      onSubmit={handleSubmit(onSubmit)}
      className="space-y-5"
    >

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Product Name
        </label>

        <input
          {...register("name")}
          className="input"
        />

        <p className="text-sm text-red-400">
          {errors.name?.message}
        </p>

      </div>

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Description
        </label>

        <textarea
          {...register("description")}
          rows={4}
          className="input"
        />

        <p className="text-sm text-red-400">
          {errors.description?.message}
        </p>

      </div>

      <div className="grid gap-5 md:grid-cols-2">

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Price
          </label>

          <input
            type="number"
            {...register("price")}
            className="input"
          />

        </div>

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Stock
          </label>

          <input
            type="number"
            {...register("availableQuantity")}
            className="input"
          />

        </div>

      </div>

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Category
        </label>

        <input
          {...register("category")}
          className="input"
        />

      </div>

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Image URL
        </label>

        <input
          {...register("imageUrl")}
          className="input"
        />

      </div>

      <button
        className="btn-primary w-full px-8 py-3 sm:w-auto"
      >
        {submitText}
      </button>

    </form>

  );

}

export default ProductForm;
