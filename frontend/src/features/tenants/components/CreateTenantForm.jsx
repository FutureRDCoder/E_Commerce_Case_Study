import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import toast from "react-hot-toast";

import { tenantSchema } from "../schemas/tenantSchema";
import { useCreateTenant } from "../../../hooks/useCreateTenant";

function CreateTenantForm({ onSuccess }) {

  const createTenant = useCreateTenant();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(tenantSchema),
    defaultValues: {
      name: "",
      slug: "",
      description: "",
      logoUrl: "",
    },
  });

  const handleSubmitForm = (formData) => {

    createTenant.mutate(
      {
        name: formData.name,
        slug: formData.slug,
        description: formData.description || undefined,
        logoUrl: formData.logoUrl || undefined,
      },
      {
        onSuccess: () => {

          toast.success("Brand created successfully!");

          reset();

          onSuccess?.();

        },

        onError: (error) => {

          toast.error(
            error.response?.data?.message ??
            "Failed to create brand."
          );

        },
      }
    );

  };

  return (

    <form
      onSubmit={handleSubmit(handleSubmitForm)}
      className="space-y-5"
    >

      <div className="grid gap-5 md:grid-cols-2">

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Brand Name
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
            Slug
          </label>

          <input
            {...register("slug")}
            className="input"
          />

          <p className="text-sm text-red-400">
            {errors.slug?.message}
          </p>

        </div>

      </div>

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Description
        </label>

        <textarea
          {...register("description")}
          rows={3}
          className="input"
        />

        <p className="text-sm text-red-400">
          {errors.description?.message}
        </p>

      </div>

      <div>

        <label className="mb-2 block font-medium text-slate-300">
          Logo URL
        </label>

        <input
          {...register("logoUrl")}
          className="input"
        />

        <p className="text-sm text-red-400">
          {errors.logoUrl?.message}
        </p>

      </div>

      <button
        disabled={createTenant.isPending}
        className="btn-primary px-8 py-3 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {createTenant.isPending
          ? "Creating..."
          : "Create Brand"}
      </button>

    </form>

  );

}

export default CreateTenantForm;
