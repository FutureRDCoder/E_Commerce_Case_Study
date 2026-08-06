import { Navigate, Outlet } from "react-router-dom";
import useAuthStore from "../store/authStore";

function CustomerRoute() {
  const user = useAuthStore((state) => state.user);

  const isCustomer =
    user?.role === "USER" ||
    user?.role === "TENANT_ADMIN";

  return isCustomer
    ? <Outlet />
    : <Navigate to="/" replace />;
}

export default CustomerRoute;
