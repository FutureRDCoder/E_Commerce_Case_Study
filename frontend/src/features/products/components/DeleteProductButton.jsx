import toast from "react-hot-toast";

import { useDeleteProduct } from "../../../hooks/useDeleteProduct";
import useAuthStore from "../../../store/authStore";

function DeleteProductButton({
  product,
}) {

  const tenantSlug = useAuthStore(
    (state) => state.user?.tenantSlug
  );

  const deleteMutation = useDeleteProduct();

  const handleDelete = () => {

    const confirmed = window.confirm(
      `Delete "${product.name}"?`
    );

    if (!confirmed) {
      return;
    }

    deleteMutation.mutate(
      {
        tenantSlug,
        productId: product.id,
      },
      {
        onSuccess: () => {
          toast.success("Product deleted.");
        },

        onError: (error) => {
          toast.error(
            error.response?.data?.message ??
            "Failed to delete product."
          );
        },
      }
    );
  };

  return (
    <button
      onClick={handleDelete}
      disabled={deleteMutation.isPending}
      className="btn-danger px-3 py-2 text-sm disabled:cursor-not-allowed disabled:opacity-50"
    >
      Delete
    </button>
  );
}

export default DeleteProductButton;
