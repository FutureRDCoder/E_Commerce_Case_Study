import { Navigate, Outlet } from "react-router-dom";
import useAuthStore from "../store/authStore";

function TenantAdminRoute() {
  const user = useAuthStore((state) => state.user);

  return user?.role === "TENANT_ADMIN"
    ? <Outlet />
    : <Navigate to="/" replace />;
}

export default TenantAdminRoute;