import toast from "react-hot-toast";

import { useDeleteTenant } from "../../hooks/useDeleteTenant";

function DeleteTenantButton({
  tenant,
}) {

  const deleteMutation = useDeleteTenant();

  const handleDelete = () => {

    const confirmed = window.confirm(
      `Delete brand "${tenant.name}"? This will also remove its products and orders.`
    );

    if (!confirmed) {
      return;
    }

    deleteMutation.mutate(
      tenant.id,
      {
        onSuccess: () => {
          toast.success("Brand deleted.");
        },

        onError: (error) => {
          toast.error(
            error.response?.data?.message ??
            "Failed to delete brand."
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
      {deleteMutation.isPending ? "Deleting..." : "Delete"}
    </button>
  );
}

export default DeleteTenantButton;
