import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import {
    Menu,
    X,
    Heart,
    ShoppingCart,
    User,
} from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { useCart } from '../../hooks/useCart';
import NotificationBell from './NotificationBell';

function Navbar() {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const { isAuthenticated, logout, user } = useAuthStore();

    const cartTenantSlug = "global";

    const { data: cartItems = [] } = useCart(cartTenantSlug);

    const cartCount = cartItems.reduce(
        (sum, item) => sum + item.quantity,
        0
    );

    const navLinkClass = ({ isActive }) =>
        `transition-colors ${isActive
            ? 'font-semibold text-primary-400'
            : 'text-slate-400 hover:text-white'
        }`;

    return (
        <header className="sticky top-0 z-50 border-b border-white/10 bg-night-900/80 backdrop-blur">

            <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6">

                {/* Logo */}

                <Link
                    to="/"
                    className="flex shrink-0 items-center gap-2"
                >
                    <div className="bg-gradient-brand rounded-lg p-2 text-white shadow-glow">
                        🛍
                    </div>

                    <span className="text-xl font-bold text-white">
                        eCommerce
                    </span>
                </Link>

                {/* Desktop Navigation */}

                <nav className="hidden items-center gap-8 md:flex">

                    <NavLink
                        to="/products"
                        className={navLinkClass}
                    >
                        Products
                    </NavLink>

                    <NavLink
                        to="/brands"
                        className={navLinkClass}
                    >
                        Brands
                    </NavLink>

                </nav>

                {/* Desktop Right */}

                <div className="hidden items-center gap-4 md:flex">

                    {isAuthenticated ? (
                        <>
                            <NotificationBell />

                            <Link to="/favourites" aria-label="Favourites">
                                <Heart className="h-5 w-5 cursor-pointer text-slate-400 transition-colors hover:text-pink-500" />
                            </Link>

                            {user?.role !== 'ADMIN' && (
                                <Link to="/cart" aria-label="Cart">
                                    <span className="relative inline-block">
                                        <ShoppingCart className="h-5 w-5 cursor-pointer text-slate-400 transition-colors hover:text-primary-400" />

                                        {cartCount > 0 && (
                                            <span className="bg-gradient-brand absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full px-1 text-xs font-bold text-white">
                                                {cartCount}
                                            </span>
                                        )}
                                    </span>
                                </Link>
                            )}

                            {user?.role === 'ADMIN' && (
                                <Link
                                    to="/admin/dashboard"
                                    className="btn-primary px-4 py-2 text-sm"
                                >
                                    Admin
                                </Link>
                            )}

                            <Link to="/profile" aria-label="Profile">
                                <User className="h-5 w-5 cursor-pointer text-slate-400 transition-colors hover:text-primary-400" />
                            </Link>

                            <button
                                onClick={logout}
                                className="btn-secondary px-4 py-2 text-sm"
                            >
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <NavLink
                                to="/login"
                                className={navLinkClass}
                            >
                                Login
                            </NavLink>

                            <NavLink
                                to="/register"
                                className="btn-primary px-4 py-2 text-sm"
                            >
                                Register
                            </NavLink>
                        </>
                    )}

                </div>

                {/* Mobile Menu Button */}

                <div className="flex items-center gap-2 md:hidden">

                    {isAuthenticated && (
                        <NotificationBell />
                    )}

                    <button
                        onClick={() =>
                            setMobileMenuOpen(!mobileMenuOpen)
                        }
                        aria-label={
                            mobileMenuOpen
                                ? "Close menu"
                                : "Open menu"
                        }
                        aria-expanded={mobileMenuOpen}
                        className="rounded-lg p-2 text-slate-300 transition-colors hover:bg-white/5"
                    >
                        {mobileMenuOpen ? <X /> : <Menu />}
                    </button>

                </div>

            </div>

            {/* Mobile Menu */}

            {mobileMenuOpen && (

                <nav className="border-t border-white/10 bg-night-900 md:hidden">

                    <div className="flex flex-col gap-4 p-4 sm:p-6">

                        <NavLink
                            to="/products"
                            className={navLinkClass}
                            onClick={() => setMobileMenuOpen(false)}
                        >
                            Products
                        </NavLink>

                        <NavLink
                            to="/brands"
                            className={navLinkClass}
                            onClick={() => setMobileMenuOpen(false)}
                        >
                            Brands
                        </NavLink>

                        {isAuthenticated ? (
                            <>
                                <NavLink
                                    to="/favourites"
                                    className={navLinkClass}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Favourites
                                </NavLink>

                                {user?.role !== 'ADMIN' && (
                                    <>
                                        <NavLink
                                            to="/cart"
                                            className={navLinkClass}
                                            onClick={() => setMobileMenuOpen(false)}
                                        >
                                            Cart
                                        </NavLink>

                                        <NavLink
                                            to="/orders"
                                            className={navLinkClass}
                                            onClick={() => setMobileMenuOpen(false)}
                                        >
                                            Orders
                                        </NavLink>
                                    </>
                                )}

                                {user?.role === 'ADMIN' && (
                                    <>
                                        <NavLink
                                            to="/admin/orders"
                                            className={navLinkClass}
                                            onClick={() => setMobileMenuOpen(false)}
                                        >
                                            Orders
                                        </NavLink>
                                    </>
                                )}

                                {user?.role === 'TENANT_ADMIN' && (
                                    <NavLink
                                        to="/tenant/dashboard"
                                        className={navLinkClass}
                                        onClick={() => setMobileMenuOpen(false)}
                                    >
                                        Dashboard
                                    </NavLink>
                                )}

                                {user?.role === 'ADMIN' && (
                                    <NavLink
                                        to="/admin/dashboard"
                                        className={navLinkClass}
                                        onClick={() => setMobileMenuOpen(false)}
                                    >
                                        Admin
                                    </NavLink>
                                )}

                                <NavLink
                                    to="/profile"
                                    className={navLinkClass}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Profile
                                </NavLink>

                                <button
                                    onClick={() => {
                                        setMobileMenuOpen(false);
                                        logout();
                                    }}
                                    className="btn-secondary w-full px-4 py-2 text-left"
                                >
                                    Logout
                                </button>
                            </>
                        ) : (
                            <>
                                <NavLink
                                    to="/login"
                                    className={navLinkClass}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Login
                                </NavLink>

                                <NavLink
                                    to="/register"
                                    className="btn-primary px-4 py-2 text-center"
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    Register
                                </NavLink>
                            </>
                        )}

                    </div>

                </nav>

            )}

        </header>
    );
}

export default Navbar;
