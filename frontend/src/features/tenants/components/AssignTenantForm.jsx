import { useState } from "react";
import toast from "react-hot-toast";

import { useTenants } from "../../../hooks/useTenants";
import { useUsers } from "../../../hooks/useUsers";
import { useAssignTenant } from "../../../hooks/useAssignTenant";

function AssignTenantForm() {

  const [selectedUserId, setSelectedUserId] = useState("");
  const [selectedTenantId, setSelectedTenantId] = useState("");

  const {
    data: tenantsData,
    isLoading: tenantsLoading,
  } = useTenants(0, 100);

  const {
    data: usersData,
    isLoading: usersLoading,
  } = useUsers(0, 100);

  const assignTenant = useAssignTenant();

  const users = usersData?.content ?? [];
  const tenants = tenantsData?.content ?? [];

  const handleSubmit = (event) => {

    event.preventDefault();

    if (!selectedUserId || !selectedTenantId) {
      toast.error("Please select both a user and a brand.");
      return;
    }

    assignTenant.mutate(
      {
        userId: Number(selectedUserId),
        tenantId: Number(selectedTenantId),
      },
      {
        onSuccess: () => {

          toast.success("User is now a brand admin.");

          setSelectedUserId("");
          setSelectedTenantId("");

        },

        onError: (error) => {

          toast.error(
            error.response?.data?.message ??
            "Failed to assign user to brand."
          );

        },
      }
    );

  };

  return (

    <form
      onSubmit={handleSubmit}
      className="space-y-5"
    >

      <div className="grid gap-5 md:grid-cols-2">

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            User
          </label>

          <select
            value={selectedUserId}
            onChange={(event) =>
              setSelectedUserId(event.target.value)
            }
            disabled={usersLoading}
            className="input cursor-pointer"
          >

            <option value="" className="bg-night-800">
              Select a user...
            </option>

            {users.map((user) => (
              <option
                key={user.id}
                value={user.id}
                className="bg-night-800"
              >
                {user.name} ({user.username})
              </option>
            ))}

          </select>

          {usersLoading && (
            <p className="mt-1 text-sm text-slate-500">
              Loading users...
            </p>
          )}

        </div>

        <div>

          <label className="mb-2 block font-medium text-slate-300">
            Brand
          </label>

          <select
            value={selectedTenantId}
            onChange={(event) =>
              setSelectedTenantId(event.target.value)
            }
            disabled={tenantsLoading}
            className="input cursor-pointer"
          >

            <option value="" className="bg-night-800">
              Select a brand...
            </option>

            {tenants.map((tenant) => (
              <option
                key={tenant.id}
                value={tenant.id}
                className="bg-night-800"
              >
                {tenant.name}
              </option>
            ))}

          </select>

          {tenantsLoading && (
            <p className="mt-1 text-sm text-slate-500">
              Loading brands...
            </p>
          )}

        </div>

      </div>

      <button
        type="submit"
        disabled={assignTenant.isPending}
        className="btn-primary px-8 py-3 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {assignTenant.isPending
          ? "Assigning..."
          : "Assign Brand Admin"}
      </button>

    </form>

  );

}

export default AssignTenantForm;
