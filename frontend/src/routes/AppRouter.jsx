import { Routes, Route } from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import TenantDashboardLayout from "../layouts/TenantDashboardLayout";

import ProtectedRoute from "./ProtectedRoute";
import AdminRoute from "./AdminRoute";
import TenantAdminRoute from "./TenantAdminRoute";
import GuestRoute from "./GuestRoute";

import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ProductsPage from "../pages/ProductsPage";
import ProductDetailsPage from "../pages/ProductDetailsPage";
import FavouritesPage from "../pages/FavouritesPage";
import CartPage from "../pages/CartPage";
import OrdersPage from "../pages/OrdersPage";
import ProfilePage from "../pages/ProfilePage";
import TenantDashboardHome from "../pages/tenant/TenantDashboardHome";
import TenantProductsPage from "../pages/tenant/TenantProductsPage";
import TenantOrdersPage from "../pages/tenant/TenantOrdersPage";
import AdminDashboardPage from "../pages/AdminDashboardPage";
import NotFoundPage from "../pages/NotFoundPage";
import BrandsPage from "../pages/BrandsPage";

function AppRouter() {
  return (
    <Routes>

      <Route element={<MainLayout />}>

        {/* Public Routes */}

        <Route path="/" element={<ProductsPage />} />
        <Route element={<GuestRoute />}>

          <Route
            path="/login"
            element={<LoginPage />}
          />

          <Route
            path="/register"
            element={<RegisterPage />}
          />

        </Route>

        <Route
          path="/products"
          element={<ProductsPage />}
        />

        <Route
          path="/:tenantSlug/products"
          element={<ProductsPage />}
        />

        <Route
          path="/:tenantSlug/products/:productId"
          element={<ProductDetailsPage />}
        />

        <Route
          path="/brands"
          element={<BrandsPage />}
        />

        {/* Protected Routes */}

        <Route element={<ProtectedRoute />}>

          <Route path="/profile" element={<ProfilePage />} />

          <Route path="/cart" element={<CartPage />} />

          <Route path="/orders" element={<OrdersPage />} />

          <Route path="/favourites" element={<FavouritesPage />} />

          <Route path="/:tenantSlug/cart" element={<CartPage />} />

          <Route path="/:tenantSlug/orders" element={<OrdersPage />} />

          <Route
            path="/:tenantSlug/favourites"
            element={<FavouritesPage />}
          />

        </Route>

        {/* Tenant Admin */}

        <Route element={<TenantAdminRoute />}>

          <Route element={<TenantDashboardLayout />}>

            <Route
              path="/tenant/dashboard"
              element={<TenantDashboardHome />}
            />

            <Route
              path="/tenant/dashboard/products"
              element={<TenantProductsPage />}
            />

            <Route
              path="/tenant/dashboard/orders"
              element={<TenantOrdersPage />}
            />

          </Route>

        </Route>

        {/* Platform Admin */}

        <Route element={<AdminRoute />}>

          <Route
            path="/admin/dashboard"
            element={<AdminDashboardPage />}
          />

        </Route>

      </Route>

      <Route path="*" element={<NotFoundPage />} />

    </Routes>
  );
}

export default AppRouter;