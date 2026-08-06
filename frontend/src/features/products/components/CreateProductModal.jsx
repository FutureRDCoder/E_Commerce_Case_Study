import toast from "react-hot-toast";

import ProductForm from "./ProductForm";
import { useCreateProduct } from "../../../hooks/useCreateProduct";
import useAuthStore from "../../../store/authStore";

function CreateProductModal({ onSuccess }) {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const createProduct = useCreateProduct();

  const handleSubmit = (formData) => {

    createProduct.mutate(
      {
        tenantSlug,
        product: formData,
      },
      {
        onSuccess: () => {

          toast.success("Product created successfully!");

          onSuccess?.();

        },

        onError: (error) => {

          toast.error(
            error.response?.data?.message ??
            "Failed to create product."
          );

        },

      }
    );

  };

  return (

    <div className="card p-4 shadow-card sm:p-8">

      <h2 className="mb-6 text-xl font-bold text-white sm:text-2xl">
        Create Product
      </h2>

      <ProductForm
        submitText="Create Product"
        onSubmit={handleSubmit}
      />

    </div>

  );

}

export default CreateProductModal;