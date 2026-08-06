import { NavLink, Outlet } from "react-router-dom";

function TenantDashboardLayout() {

  const navLinkClass = ({ isActive }) =>
    `block rounded-lg px-4 py-2 transition whitespace-nowrap ${
      isActive
        ? "bg-gradient-brand text-white shadow-glow"
        : "text-slate-400 hover:bg-white/5 hover:text-white"
    }`;

  return (

    <div className="flex min-h-screen flex-col md:flex-row">

      {/* Sidebar */}

      <aside className="w-full shrink-0 border-b border-white/10 bg-night-900/80 p-4 backdrop-blur md:w-64 md:border-b-0 md:border-r md:p-6">

        <h1 className="font-display mb-4 text-xl font-bold text-white md:mb-8 md:text-2xl">
          Tenant Dashboard
        </h1>

        <nav className="flex gap-2 overflow-x-auto pb-1 md:flex-col md:space-y-2 md:overflow-visible md:pb-0">
          <NavLink
            to="/tenant/dashboard"
            end
            className={navLinkClass}
          >
            Dashboard
          </NavLink>

          <NavLink
            to="/tenant/dashboard/products"
            className={navLinkClass}
          >
            Products
          </NavLink>

          <NavLink
            to="/tenant/dashboard/orders"
            className={navLinkClass}
          >
            Orders
          </NavLink>
        </nav>

      </aside>

      {/* Content */}

      <main className="flex-1 p-4 md:p-8">

        <Outlet />

      </main>

    </div>

  );

}

export default TenantDashboardLayout;
