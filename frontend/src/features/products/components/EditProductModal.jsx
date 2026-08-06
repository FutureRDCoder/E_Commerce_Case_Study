import toast from "react-hot-toast";

import ProductForm from "./ProductForm";
import { useUpdateProduct } from "../../../hooks/useUpdateProduct";
import useAuthStore from "../../../store/authStore";

function EditProductModal({
  product,
  onSuccess,
}) {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const updateProduct = useUpdateProduct();

  const handleSubmit = (formData) => {

    updateProduct.mutate(
      {
        tenantSlug,
        productId: product.id,
        product: formData,
      },
      {
        onSuccess: () => {

          toast.success(
            "Product updated successfully!"
          );

          onSuccess?.();

        },

        onError: (error) => {

          toast.error(
            error.response?.data?.message ??
            "Failed to update product."
          );

        },

      }
    );

  };

  return (

    <div className="card mt-8 p-4 shadow-card sm:p-8">

      <h2 className="mb-6 text-xl font-bold text-white sm:text-2xl">
        Edit Product
      </h2>

      <ProductForm
        defaultValues={product}
        onSubmit={handleSubmit}
        submitText="Update Product"
      />

    </div>

  );

}

export default EditProductModal;