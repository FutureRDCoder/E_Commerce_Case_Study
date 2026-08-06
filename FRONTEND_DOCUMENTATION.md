# Frontend Source Documentation

A line-by-line guide to everything under `frontend/src/` (except `src/assets/`).

## Tech Stack & Tooling

| Concern | Choice |
| --- | --- |
| UI library | React 19 (`react`, `react-dom`) |
| Build tool | Vite 8 (`vite`, `@vitejs/plugin-react`, `@tailwindcss/vite`) |
| Styling | Tailwind CSS 4 (`@import "tailwindcss"`, `@theme`, `@layer`) |
| Routing | `react-router-dom` 7 (`BrowserRouter`, `Routes/Route`, `Outlet`, `NavLink`) |
| Server-state | TanStack Query 5 (`@tanstack/react-query`) |
| Client-state | Zustand 5 with `persist` middleware |
| HTTP | Axios 1 |
| Forms | `react-hook-form` 7 + `zod` 4 + `@hookform/resolvers` |
| Toasts | `react-hot-toast` |
| Icons | `lucide-react` |
| Utilities | `clsx`, `tailwind-merge` (declared but not used in current source) |
| Lint | oxlint (`npm run lint`) |
| Format | Prettier (`prettier.config.js`) |

Scripts (`frontend/package.json`): `dev` → `vite`, `build` → `vite build`, `lint` → `oxlint`, `preview` → `vite preview`.

---

## Top-Level Files

### `src/main.jsx`
The application entry point. Bootstraps everything:

- `React.StrictMode` — enables extra dev-time checks (double-invokes effects/renderers in dev).
- `BrowserRouter` — client-side routing for the whole app.
- `QueryClientProvider` with the shared `queryClient` from `src/api/queryClient`.
- `<App />` — the root component (see below).
- `<Toaster position="top-right" reverseOrder={false} />` — the global `react-hot-toast` host. Any `toast.success/error()` call anywhere in the app renders here.

Note the wrapping order: `BrowserRouter` → `QueryClientProvider` → `App` (+ `Toaster`). This lets every route and every query/mutation hook inside `App` use router and query context.

### `src/App.jsx`
A one-liner component that just renders `<AppRouter />` from `src/routes/AppRouter.jsx`. All routing logic lives in the router.

### `src/index.css`
The single global stylesheet. It is imported once in `main.jsx`.

**Fonts** — `@import` of Google Fonts: `Inter` (300–700) and `Outfit` (400–800).

**`@theme` block (Tailwind 4 design tokens)** — defines custom values that become real Tailwind utility classes:

- Fonts:
  - `--font-sans: "Inter", ...` → `font-sans`
  - `--font-display: "Outfit", ...` → `font-display`
- Colors:
  - `night-950 → #07090e`, `night-900 → #0b0e16`, `night-800 → #101524`, `night-700 → #1a2036`, `night-600 → #222a45` → `bg-night-900`, `border-night-700`, etc.
  - `primary-300 … primary-700` (indigo ramp) → `text-primary-400`, `bg-primary-500`, etc.
  - `accent-400 … accent-600` (purple ramp) → used as `bg-accent-500`, etc.
  - `pink-500: #ec4899` → `bg-pink-500`, `text-pink-500`.
- Shadows:
  - `--shadow-glow` → `shadow-glow` (indigo glow)
  - `--shadow-glow-lg` → `shadow-glow-lg`
  - `--shadow-card` → `shadow-card`

**`@layer base`** — global element styles:

- `*` gets a subtle white border color default (`rgba(255,255,255,0.08)`), so any `border` without an explicit color uses it.
- `body` — dark night-950 background with three fixed radial-gradient "glow" blobs (indigo top-left, purple top-right, pink bottom), `min-height: 100vh`, antialiased, slate-100 text.
- All headings (`h1`–`h6`) use `font-display` (Outfit) with tight letter-spacing.
- Custom WebKit scrollbar styling (8px, dark track/thumb).

**`@layer utilities`** — custom utility classes used across the app:

- `text-gradient` — indigo → purple → pink gradient clipped to text.
- `bg-gradient-brand` — same gradient as a background (used for primary buttons, logo tiles, active nav).
- `card` — dark translucent panel: white/8 border, `rgba(15,23,42,0.55)` fill, 14px radius.
- `card-hover` — smooth hover animation; lifts 4px, glows indigo, brightens border.
- `input` — full-width dark input/select: white/10 border, dark fill, focus ring in indigo (`border-primary` + 3px glow).
- `btn-primary` — brand gradient button, white bold text, hover glow + brightness, disabled state (50% opacity).
- `btn-secondary` — outlined translucent button, hover brightens background/border.
- `btn-danger` — translucent red button with red text.
- `chip` — small pill badge (used for categories, statuses).
- `chip-success` — green tinted pill (in-stock, active statuses).
- `chip-warning` — amber tinted pill (e.g. warning states).

---

## API Layer (`src/api/`)

### `src/api/queryClient.js`
Creates and exports a single `QueryClient`:

- `queries.retry: 1` — one retry on failed queries.
- `queries.refetchOnWindowFocus: false` — no auto-refetch when the tab regains focus.
- `mutations.retry: 1` — mutations also retry once.

### `src/api/apiClient.js`
A configured Axios instance, the single HTTP client used by every service.

- `baseURL: import.meta.env.VITE_API_BASE_URL` — read from `frontend/.env`.
- Default `Content-Type: application/json`.

**Request interceptor** (the active one):
- Reads the JWT from Zustand store (`useAuthStore.getState().token`).
- Skips the `Authorization` header for "public" URLs:
  - any URL starting with `/api/tenants` (public tenant/brand listing)
  - any URL starting with `/api/public/`
- Otherwise, if a token exists, attaches `Authorization: Bearer <token>`.

(There is a commented-out simpler interceptor above it that attached the token to every request; the active one was added to support public endpoints.)

**Response interceptor**:
- Passes successful responses through.
- On `401 Unauthorized`: only triggers auto-logout when (a) a token was actually sent with the request, and (b) the store still thinks the user is authenticated. Logs a warning and calls `logout()`. This avoids logging users out when an unauthenticated request hits a protected endpoint.

---

## Global State (`src/store/`)

### `src/store/authStore.js`
A Zustand store wrapped in `persist` middleware. Persisted to `localStorage` under key **`auth-storage`**.

State shape:
- `token` — JWT string (or `null`).
- `user` — the authenticated user object (or `null`).
- `isAuthenticated` — boolean.

Actions:
- `login(token, user)` — sets all three fields, marks authenticated. (Login form passes the whole response as `user`; register form destructures `token` out first.)
- `logout()` — resets everything to null/false.
- `updateUser(user)` — replaces just the `user` object (keeps the rest of state).

Because it's persisted, a page refresh keeps the user logged in.

---

## Utils (`src/utils/`)

### `src/utils/httpStatus.js`
Named constant map of HTTP status codes used across the frontend:
`OK 200`, `CREATED 201`, `NO_CONTENT 204`, `BAD_REQUEST 400`, `UNAUTHORIZED 401`, `FORBIDDEN 403`, `NOT_FOUND 404`, `INTERNAL_SERVER_ERROR 500`.

Currently consumed by `apiClient.js` for the 401 check, but kept as a central place for any component that needs a status code.

---

## Services (`src/services/`)

Thin wrappers around `apiClient` — one file per backend domain. All return `response.data`.

### `src/services/authService.js`
- `login(loginData)` → `POST /api/auth/login`
- `register(registerData)` → `POST /api/auth/register`

### `src/services/productService.js`
Base URL helper: `PRODUCT_BASE_URL(tenantSlug)` returns `/${tenantSlug}/products` if a slug is given, otherwise `/api/public/products`. So brand-scoped product reads are relative (behind the tenant path) while global storefront reads use the public API.

- `getProducts(tenantSlug, searchParams)` → `GET <base>` — filters out `undefined`/`null`/`""` params so they aren't sent.
- `getProductById(tenantSlug, productId)` → `GET <base>/<productId>`
- `createProduct(tenantSlug, product)` → `POST /<tenantSlug>/products`
- `updateProduct(tenantSlug, productId, product)` → `PUT /<tenantSlug>/products/<productId>`
- `updateProductStock(tenantSlug, productId, stock)` → `PATCH /<tenantSlug>/products/<productId>/stock`
- `deleteProduct(tenantSlug, productId)` → `DELETE /<tenantSlug>/products/<productId>` (returns nothing)

### `src/services/cartService.js`
Base URL: `/${tenantSlug}/cart`.

- `getCart(tenantSlug)` → `GET`
- `addToCart(tenantSlug, item)` → `POST` (item = `{ productId, quantity }`)
- `updateCartItem(tenantSlug, itemId, quantity)` → `PUT /<itemId>` with body `{ quantity }`
- `removeCartItem(tenantSlug, itemId)` → `DELETE /<itemId>`
- `clearCart(tenantSlug)` → `DELETE` (on the collection)

### `src/services/orderService.js`
Base URL: `/${tenantSlug}/orders`.

- `createOrder(tenantSlug, order)` → `POST` (order = `{ items: [{ productId, quantity }] }`)
- `getOrders(tenantSlug)` → `GET /my-history` — the customer's own order history for a brand ("global" uses the global cart order history).
- `getTenantOrders(tenantSlug)` → `GET /<base>` — orders placed against the tenant's own products (tenant-admin view).
- `getAllOrders()` → `GET /api/platform/orders` — all orders across all brands (platform admin view).

### `src/services/favouriteService.js`
Base URL: `/${tenantSlug}/favourites`.

- `getFavourites(tenantSlug)` → `GET`
- `addFavourite(tenantSlug, productId)` → `POST /<productId>`
- `removeFavourite(tenantSlug, productId)` → `DELETE /<productId>`

### `src/services/profileService.js`
- `getProfile()` → `GET /api/auth/me` — returns the authenticated user's profile.

### `src/services/tenantService.js`
- `getTenants(page = 0, size = 12)` → `GET /api/tenants` with `page`/`size` params (public — brands page).
- `createTenant(tenant)` → `POST /api/platform/tenants` (admin).
- `deleteTenant(tenantId)` → `DELETE /api/platform/tenants/<tenantId>` (admin).

### `src/services/userService.js`
- `getUsers(page = 0, size = 100)` → `GET /api/platform/users` with `role: "USER"` filter (admin).
- `assignTenantToUser(userId, tenantId)` → `PUT /api/platform/users/<userId>/tenant` with body `{ tenantId }` (makes a user a tenant admin).

### `src/services/notificationService.js`
- `getNotifications()` → `GET /api/notifications`
- `markNotificationAsRead(notificationId)` → `PUT /api/notifications/<id>/read`
- `markAllNotificationsAsRead()` → `PUT /api/notifications/read-all`

---

## Hooks (`src/hooks/`)

All data-fetching hooks are thin wrappers around the services using TanStack Query. They follow consistent conventions:

- Read hooks → `useQuery`, named `useX`.
- Write hooks → `useMutation`, named `useX`.
- Every mutation invalidates its relevant query key(s) in `onSuccess` so the UI refreshes.
- Most show `react-hot-toast` errors using `error.response?.data?.message ?? error.message ?? "fallback"`.
- Many take `tenantSlug` and are disabled (`enabled: !!tenantSlug`) until a slug exists.

### Generic
- `useDebounce(value, delay = 500)` — returns a delayed copy of `value`; used to debounce search input so queries don't fire on every keystroke.

### Products
- `useProducts(tenantSlug, searchParams)` — `queryKey: ["products", tenantSlug, JSON.stringify(searchParams)]`. The JSON-stringified params make the key change whenever filters change, so React Query re-fetches on new filters. Always enabled.
- `useProduct(tenantSlug, productId)` — `queryKey: ["product", tenantSlug, productId]`; enabled only when both slug and id exist.
- `useCreateProduct()` — mutation; invalidates `["products", tenantSlug]` (via `variables`).
- `useUpdateProduct()` — mutation; invalidates `["products", tenantSlug]`.
- `useUpdateProductStock()` — mutation (PATCH stock); invalidates `["products", tenantSlug]`.
- `useDeleteProduct()` — mutation; invalidates `["products", tenantSlug]`.

### Cart
- `useCart(tenantSlug)` — `["cart", tenantSlug]`, enabled when slug present.
- `useAddToCart()` — mutation; invalidates `["cart"]`; success toast `"<productName> added to cart"`.
- `useUpdateCartItem()` — mutation; invalidates `["cart"]`.
- `useRemoveCartItem()` — mutation; invalidates `["cart"]`; toast "Item removed from cart".
- `useClearCart()` — mutation; invalidates `["cart"]`.

### Orders
- `useOrders(tenantSlug)` — `["orders", tenantSlug]`, user's own history.
- `useTenantOrders(tenantSlug)` — `["tenant-orders", tenantSlug]`, tenant admin's incoming orders.
- `useAllOrders()` — `["all-orders"]`, platform-wide orders.
- `useCreateOrder()` — mutation; invalidates `["orders"]`, `["products"]`, and `["cart"]` (stock and cart changed).

### Favourites
- `useFavourites(tenantSlug)` — `["favourites", tenantSlug]`.
- `useAddFavourite()` — mutation; invalidates `["products"]`, `["product"]`, and `["favourites"]` (hearts on cards depend on the `isFavourite` flag in product payloads).
- `useRemoveFavourite()` — same invalidation set.

### Profile & Users
- `useProfile()` — `["profile"]`.
- `useUsers(page, size)` — `["users", page, size]`.
- `useAssignTenant()` — mutation; invalidates `["users"]`.

### Tenants
- `useTenants(page, size)` — `["tenants", page, size]`.
- `useCreateTenant()` — mutation; invalidates `["tenants"]`.
- `useDeleteTenant()` — mutation; invalidates `["tenants"]`.

### Notifications
- `useNotifications()` — `["notifications"]`; enabled only when authenticated; `refetchInterval: 10000` — polls every 10 seconds so the bell/alert stay fresh.
- `useMarkNotificationAsRead()` — mutation; invalidates `["notifications"]`.
- `useMarkAllNotificationsAsRead()` — mutation; invalidates `["notifications"]`.

---

## Routing (`src/routes/`)

### `src/routes/AppRouter.jsx`
The single source of truth for every route. Layout + guard structure:

**Public (under `MainLayout`):**
- `/` → `ProductsPage`
- `/login`, `/register` → wrapped in `GuestRoute`
- `/products` → `ProductsPage`
- `/:tenantSlug/products` → `ProductsPage`
- `/:tenantSlug/products/:productId` → `ProductDetailsPage`
- `/brands` → `BrandsPage`

**Authenticated (under `MainLayout` + `ProtectedRoute`):**
- `/profile` → `ProfilePage`
- `/cart` → `CartPage`
- `/orders` → `OrdersPage`
- Under `CustomerRoute` (USER or TENANT_ADMIN): `/favourites`, `/:tenantSlug/favourites`
- `/:tenantSlug/cart`, `/:tenantSlug/orders`

**Tenant admin (under `MainLayout` + `TenantAdminRoute` + `TenantDashboardLayout`):**
- `/tenant/dashboard` → `TenantDashboardHome`
- `/tenant/dashboard/products` → `TenantProductsPage`
- `/tenant/dashboard/orders` → `TenantOrdersPage`

**Platform admin (under `MainLayout` + `AdminRoute`):**
- `/admin/dashboard` → `AdminDashboardPage`

**Catch-all:** `*` → `NotFoundPage`

### Route guards (all read from `authStore` and render `<Outlet />` or `<Navigate>`)
- `ProtectedRoute.jsx` — requires `isAuthenticated`, else redirects to `/login`.
- `GuestRoute.jsx` — the inverse: if authenticated, redirects to `/` (keeps logged-in users off login/register).
- `CustomerRoute.jsx` — requires `user.role === "USER" || "TENANT_ADMIN"` (anyone who can shop), else redirects to `/`.
- `TenantAdminRoute.jsx` — requires `user.role === "TENANT_ADMIN"`, else `/`.
- `AdminRoute.jsx` — requires `user.role === "ADMIN"`, else `/`.

Guards are used as layout-style routes: `<Route element={<ProtectedRoute />}>` with child routes rendering through `<Outlet />`.

---

## Layouts (`src/layouts/`)

### `src/layouts/MainLayout.jsx`
The storefront shell rendered around public + authenticated storefront routes:

- `<NotificationAlert />` — floating toast alerts for notifications.
- `<Navbar />` — sticky top nav.
- `<main class="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">` — page content via `<Outlet />`.
- `<Footer />`.

### `src/layouts/TenantDashboardLayout.jsx`
Two-column dashboard shell (sidebar + content), used by all tenant-admin routes:

- **Sidebar** (`aside`): "Tenant Dashboard" heading and a `NavLink` nav with:
  - Dashboard → `/tenant/dashboard` (with `end` so it only matches exactly)
  - Products → `/tenant/dashboard/products`
  - Orders → `/tenant/dashboard/orders`
- Active links get `bg-gradient-brand text-white shadow-glow`; inactive get muted slate with hover.
- On mobile the sidebar is a horizontal scrolling top bar; on `md+` it becomes a fixed-width vertical column (`md:w-64`, right border).
- Content area renders via `<Outlet />`.

---

## Components (`src/components/`)

### `src/components/layout/`

**`Navbar.jsx`**
Sticky header (`sticky top-0 z-50`, translucent night-900 + backdrop blur).

- **Logo** — gradient tile with 🛍 emoji + "eCommerce" wordmark linking to `/`.
- **Desktop nav** (`hidden md:flex`): `Products` and `Brands` links.
- **Right side (desktop, authenticated):**
  - `NotificationBell`
  - Heart link to `/favourites` (hidden for `ADMIN`)
  - Cart link to `/cart` with a gradient count badge (hidden for `ADMIN`). Cart count is computed from `useCart("global")` — the navbar always tracks the global cart (`cartTenantSlug = "global"`).
  - `Orders` button (USER or TENANT_ADMIN) → `/orders`
  - `Dashboard` button (TENANT_ADMIN) → `/tenant/dashboard`
  - `Admin` button (ADMIN) → `/admin/dashboard`
  - User icon link → `/profile`
  - `Logout` button → calls `logout()`
- **Right side (guest):** `Login` link + `Register` primary button.
- **Mobile:** hamburger (`Menu`/`X`) + bell; toggles a slide-down `<nav>` with all the same links (role-aware), closing the menu on click.
- Note: mobile menu includes a `/admin/orders` link for ADMIN which has no matching route (dead link — would hit `NotFoundPage`); desktop uses `/orders` only.

**`Footer.jsx`**
Minimal footer: logo tile + "eCommerce" and `© {year} eCommerce. All rights reserved.` (year computed at render).

**`NotificationAlert.jsx`**
Renders nothing (`return null`); it's a side-effect component. It subscribes to `useNotifications()` and, for each new **unread** notification not yet seen this session (tracked via a `useRef` `Set` of ids), fires a `react-hot-toast` styled card with a gradient Crown avatar, "TENANT ADMIN" label, message, `Dismiss` button, and `8s` duration. Read notifications are skipped.

**`NotificationBell.jsx`**
A dropdown notifications bell:

- Polls notifications via `useNotifications()`; shows an unread count badge on the bell (pink).
- Clicking toggles the dropdown (with a full-screen invisible backdrop to close).
- Header has "Notifications" and a "Mark all as read" button (`CheckCheck`) when there are unread items.
- List items show a status dot (pink = unread, slate = read), the message, and a formatted timestamp (`formatTimestamp` → e.g. "Aug 6, 2:30 PM").
- Clicking an unread item calls `markAsRead.mutate(id)` and closes the dropdown.
- Loading / empty states included.

### `src/components/common/`

**`Pagination.jsx`**
Uncontrolled-by-data pagination bar. Props: `page`, `totalPages`, `onPageChange`. Renders nothing if `totalPages <= 1`. Provides `Previous`/`Next` buttons (disabled at bounds) and a numbered button per page; the current page uses the gradient brand style.

### `src/components/product/`

**`ProductCard.jsx`**
A single product card (used in grids):

- Image (`h-56 object-cover`) with an absolute "In Stock"/"Out of Stock" chip (green pill vs red pill by `availableQuantity > 0`).
- Body: name (2-line clamp), `FavouriteButton` (hidden for ADMIN), category `chip`, 2-line description, price as `text-gradient ₹{price}`.
- Footer: "View Details" (`btn-secondary`) linking to `/{tenantSlug}/products/{id}` and `AddToCartButton` (`variant="button"`, qty 1).
- Reads `user` from authStore to hide the favourite control for admins.

**`ProductGrid.jsx`**
Responsive grid (`1 → 2 → 3 → 4` columns) mapping products to `ProductCard`. Renders a friendly "No Products Found" empty state when the list is empty.

**`SearchBar.jsx`**
Controlled text input with a `Search` icon overlay; props `value`, `onChange`. Styled with `.input pl-12`.

**`CategoryFilter.jsx`**
Controlled `<select>`; props `value`, `onChange`, `categories`. Has an "All Categories" option plus one option per category.

**`PriceFilter.jsx`**
Price-range selector. Exports:
- `PRICE_RANGES` — a map of key → `{ minPrice, maxPrice }` (some `undefined`):
  - `all`, `under-100` (max 100), `100-500`, `500-1000`, `1000-5000`, `over-5000` (min 5000).
- `PRICE_OPTIONS` — the dropdown labels (all in ₹, e.g. "₹100 - ₹500").
- Default component is a controlled `<select>` with `aria-label="Price filter"`.

**`AddToCartButton.jsx`**
Props: `tenantSlug`, `product`, `quantity = 1`, `variant = "icon"` (`"icon"` or `"button"`).

- Returns `null` for ADMIN users.
- If the product is out of stock (`availableQuantity <= 0`), disables the button and (in button variant) shows "Out of Stock" with secondary styling.
- On click: redirects to `/login` if not authenticated; otherwise fires `useAddToCart()`.
- Icon variant: a cart icon that scales on hover. Button variant: full primary button with `ShoppingCart` icon + "Add to Cart".

**`FavouriteButton.jsx`**
Props: `tenantSlug`, `product`, `variant = "icon"`.

- Toggles via `useAddFavourite()` / `useRemoveFavourite()` depending on `product.isFavourite`.
- Icon variant: heart icon, filled red when favourited, grey → red on hover otherwise.
- Button variant: "Add to Favourites" (primary) or "Remove from Favourites" (red-tinted).
- Disabled while either mutation is pending.

### `src/components/tenant/`

**`TenantProductTable.jsx`**
Table of a tenant's products for the tenant dashboard. Columns: Product (image + name + `#id`), Category, Price (`₹{price}`), Stock, Actions. Actions per row: `Edit` (calls `onEdit(product)`), `Stock` (calls `onStock(product)`, yellow styling), and `DeleteProductButton`. Accepts `products`, `onEdit`, `_onDelete` (unused), `onStock`. Empty state panel if no products.

**`TenantOrderTable.jsx`**
Table of orders received by a tenant (tenant dashboard). Columns: Order (`#id` + chevron), Customer (`userFullName`), Date, Status (green `chip`), Quantity (`totalQuantity`), Total (`₹{totalAmount}`).

- **Expandable rows** (the order-details feature): clicking a row toggles `expandedOrderId` (single open at a time via `useState`). The chevron flips up/down. When expanded, an extra `<tr>` (built from `<Fragment key>`) renders `<OrderDetails items={order.items} />` across all columns (`colSpan={6}`).
- Empty state if no orders.

**`AdminTenantTable.jsx`**
Table of brands for the platform admin dashboard. Columns: Brand (logo image or gradient initial avatar + name + `#id`), Slug, Description, Actions (only `DeleteTenantButton`). Empty state if none.

**`AdminOrderTable.jsx`**
Read-only table of all orders across brands (platform admin). Columns: Order (`#id`), Brand (`tenantName`), Customer, Date, Status, Quantity, Total. Not expandable.

**`BrandCard.jsx`**
A card linking to `/{tenant.slug}/products`. Shows the tenant logo (rounded-full), name, and description. Uses `card card-hover`.

**`BrandGrid.jsx`**
Responsive grid of `BrandCard`s (same breakpoints as `ProductGrid`). Empty state: "No brands available."

**`DeleteTenantButton.jsx`**
Red "Delete" button for a brand. Uses `window.confirm` ("Delete brand \"...\"? This will also remove its products and orders."), then `useDeleteTenant()`. Shows "Deleting..." while pending; toasts success/error.

### `src/components/order/`

**`OrderDetails.jsx`**
Shared, itemized order panel (used by both the customer orders page and the tenant orders table).

- Props: `items = []`.
- Empty state: "No items in this order."
- Renders a heading "ITEMS ORDERED" then one row per item:
  - Product image (`item.productImageUrl`) or a `Package` icon placeholder in a tile when no image.
  - Product name (`item.productName`), category (`item.productCategory`), `Unit Price: ₹{item.unitPrice}`.
  - Right-aligned `Qty: {item.quantity}` and a `text-gradient ₹{item.subtotal}`.
- Keys rows by `item.id`.

---

## Features (`src/features/`)

Feature-sliced modules: each has `components/` and `schemas/`.

### Auth (`src/features/auth/`)

**`schemas/loginSchema.js`**
Zod object: `username` (trimmed, required), `password` (required).

**`schemas/registerSchema.js`**
Zod object + refinement:
- `name` — trimmed, min 2.
- `username` — trimmed, min 3.
- `email` — valid email.
- `password` — min 8 + regex requiring lowercase, uppercase, digit.
- `confirmPassword` — any string; `.refine()` enforces it matches `password` (error on `confirmPassword`).
- `tenantSlug` — optional, must match `^[a-z0-9-]*$`, empty string transformed to `undefined`.

**`components/LoginForm.jsx`**
- `useForm` + `zodResolver(loginSchema)`.
- Fields: username, password. Per-field error messages under inputs.
- `useMutation(login)`; on success → `loginStore(data.token, data)` (stores whole response as user), toast "Login successful!", navigate `/`. On error → toast backend message.
- Submit button shows "Logging in..." while pending.

**`components/RegisterForm.jsx`**
- `useForm` + `zodResolver(registerSchema)`.
- Fields: Full Name, Username, Email, Password, Confirm Password, Tenant Slug (optional, with helper text: leave empty for customer, or enter a valid tenant slug to become a tenant admin).
- `useMutation(register)`; on success → destructures `{ token, ...user }`, calls `loginStore(token, user)`, toast, navigate `/`.
- Submit button shows "Creating Account..." while pending.

### Products (`src/features/products/`)

**`schemas/productSchema.js`**
Zod: `name` min 3, `description` min 10, `price` coerced number positive, `availableQuantity` coerced number min 0, `category` min 2, `imageUrl` valid URL.

**`components/ProductForm.jsx`**
Reusable form used by create & edit modals. Props: `defaultValues`, `onSubmit`, `submitText = "Save"`.
- Uses `react-hook-form` with the product schema.
- Fields: Product Name, Description (textarea), Price + Stock (2-col grid), Category, Image URL.
- Shows inline field errors; submit button uses `submitText`.

**`components/CreateProductModal.jsx`**
- Reads `tenantSlug` from `authStore.user?.tenantSlug`.
- Renders a titled card with `ProductForm` (`submitText="Create Product"`).
- Submits via `useCreateProduct()`; toasts "Product created successfully!", calls `onSuccess?.()`.

**`components/EditProductModal.jsx`**
- Same pattern with `useUpdateProduct()`, `defaultValues={product}`, `submitText="Update Product"`, toast "Product updated successfully!".

**`components/UpdateStockForm.jsx`**
- Small form (no zod) to set `availableQuantity` for a product; `useForm` with `defaultValues.availableQuantity`.
- Uses `valueAsNumber: true`; submits via `useUpdateProductStock()`. Yellow-styled submit button.

**`components/DeleteProductButton.jsx`**
- Red "Delete" button; `window.confirm` then `useDeleteProduct()` using the tenant slug from the auth store; toasts success/error.

### Tenants (`src/features/tenants/`)

**`schemas/tenantSchema.js`**
Zod: `name` (trim, 2–100), `slug` (trim, 2–50, `^[a-z0-9-]+$`), `description` (max 1000, optional or ""), `logoUrl` (valid URL, optional or "").

**`components/CreateTenantForm.jsx`**
- `useForm` + `tenantSchema`, defaults all empty strings.
- Fields: Brand Name + Slug (2-col), Description (textarea), Logo URL.
- Submits via `useCreateTenant()` (sends `undefined` for empty description/logoUrl), toasts "Brand created successfully!", `reset()`s the form, calls `onSuccess?.()`.

**`components/AssignTenantForm.jsx`**
- Loads users (`useUsers(0, 100)`) and tenants (`useTenants(0, 100)`) into two selects; local `useState` holds the selections.
- Validates both are chosen; submits `assignTenant.mutate({ userId, tenantId })` (coerced to `Number`).
- Toasts "User is now a brand admin." and clears the selects on success.
- Shows "Loading users..." / "Loading brands..." hints and disables selects while loading.

---

## Pages (`src/pages/`)

### Storefront pages

**`ProductsPage.jsx`**
The main storefront listing (serves `/`, `/products`, `/:tenantSlug/products` — the `tenantSlug` from `useParams` is undefined for the global storefront).

- Local `filters` state: `{ page, size: 12, search, category, priceRange: "all" }`.
- Search is debounced with `useDebounce(filters.search, 500)`.
- Price filter maps the selected key through `PRICE_RANGES` to `minPrice`/`maxPrice`.
- Calls `useProducts(tenantSlug, {...})` with all filters.
- Hard-coded `categories` list: Smartphones, Footwear, Audio, Apparel, Furniture, Gaming.
- Loading/error states; filter bar (`SearchBar`, `CategoryFilter`, `PriceFilter`) in a rounded panel; `ProductGrid` from `data.content`; `Pagination` bound to `data.number` / `data.totalPages`.
- Changing search/category/price resets `page` to 0.

**`ProductDetailsPage.jsx`**
Serves `/:tenantSlug/products/:productId`.

- `useProduct(tenantSlug, productId)`.
- Layout: image card (left) + details (right).
- Details: category chip, name, description, `₹{price}` in gradient, "Available Stock", a quantity number input (clamped to 1..`availableQuantity` via `handleQuantityChange`), `AddToCartButton` (button variant, uses chosen quantity), and a `FavouriteButton` (button variant, hidden for ADMIN).

**`BrandsPage.jsx`**
- `useTenants()` (default page 0, size 12).
- Header + `BrandGrid` fed from `data.content`. Loading/error states.

**`CartPage.jsx`**
Serves `/cart` and `/:tenantSlug/cart` (`tenantSlug` defaults to `"global"`).

- If ADMIN: shows a friendly "Admin accounts cannot add products to a cart or place orders." panel.
- `useCart(tenantSlug)` plus `useUpdateCartItem`, `useRemoveCartItem`, `useClearCart`, `useCreateOrder`.
- Item cards: image, name, category, `Unit Price: ₹{unitPrice}`, quantity stepper (`−`/`+`, respecting stock max), `₹{subtotal}` gradient, trash to remove.
- **Order Summary** card: total items, total amount (`toFixed(2)`), and "Place Order".
- `handlePlaceOrder`:
  1. Groups cart items by `item.tenantSlug` into per-brand `{ items: [{ productId, quantity }] }`.
  2. `await createOrder.mutateAsync(...)` **per brand** (one order per brand).
  3. Clears the global cart (`clearCart.mutate("global")`).
  4. Toasts success, navigates to `/orders`. Errors toast the backend message.

**`OrdersPage.jsx`**
Serves `/orders` and `/:tenantSlug/orders` (`tenantSlug` defaults to `"global"`). Customer's own order history.

- If ADMIN: "Admin accounts cannot place orders." panel.
- `useOrders(tenantSlug)`.
- **Expandable order cards** (the order-details feature):
  - `expandedOrderId` state (single open at a time).
  - Each card has `onClick`, `role="button"`, `tabIndex={0}`, `aria-expanded`, and keyboard handling (Enter/Space toggles, `preventDefault` for Space).
  - Header: "Order #{id}" + status chip + chevron (up/down).
  - Summary grid: Total Items, Total Amount (`₹{totalAmount}`), Order Date (raw string from API).
  - When expanded: a divider then `<OrderDetails items={order.items} />`.

**`FavouritesPage.jsx`**
- `useFavourites(tenantSlug)` (default `"global"`).
- Header + `ProductGrid` fed directly from the favourites array (no pagination).

**`ProfilePage.jsx`**
- `useProfile()` (`GET /api/auth/me`).
- Card grid showing Name, Username, Email, Role, and (if present) Brand (`user.tenantName`).

**`LoginPage.jsx`** — centers `<LoginForm />` in a `max-w-md` container.
**`RegisterPage.jsx`** — centers `<RegisterForm />` in a `max-w-lg` container.
**`NotFoundPage.jsx`** — big gradient "404", "Not Found!", message, and a "Back to Products" primary button.

### Tenant pages (`src/pages/tenant/`)

**`TenantDashboardHome.jsx`**
Placeholder welcome: "Dashboard" heading + "Welcome back!".

**`TenantProductsPage.jsx`**
Product management for the tenant admin (under `TenantDashboardLayout`).

- `tenantSlug` from `authStore.user?.tenantSlug`.
- Static filters `useMemo` → `{ page: 0, size: 100, search: "", category: "" }`; `useProducts(tenantSlug, filters)`.
- Header with "Add Product" button (opens create form).
- `TenantProductTable`:
  - `onEdit(product)` → hides create form, sets `editingProduct` → shows `EditProductModal`.
  - `onDelete` → just `console.log` (unused; actual delete is in the row's `DeleteProductButton`).
  - `onStock(product)` → clears edit, sets `stockProduct` → shows `UpdateStockForm`.
- Shows create/edit/stock forms conditionally below the table; each closes on success.

**`TenantOrdersPage.jsx`**
Order management for the tenant admin.

- `tenantSlug` from authStore; `useTenantOrders(tenantSlug)`.
- Header + `TenantOrderTable` (expandable rows).

### Admin page

**`AdminDashboardPage.jsx`**
Platform admin dashboard (only `ADMIN` can reach it).

- `useTenants(0, 100)` and `useAllOrders()`.
- **Brands section:** "Add Brand" toggle button that shows `CreateTenantForm` in a card, and `AdminTenantTable`.
- **Assign Brand Admin section:** `AssignTenantForm` in a card.
- **All Orders section:** `AdminOrderTable` fed by `useAllOrders()`.
- Per-section loading and error handling.

---

## Assets (`src/assets/`)

Static images bundled by Vite. Not documented in detail per scope, but for reference:
- `react.svg`, `vite.svg` — Vite/React boilerplate icons (unused by current components).
- `hero.png` — an unused hero image (no source references it).

---

## Conventions & Architecture Summary

1. **Layering:** Pages → (compose) Components + Hooks → Hooks call Services → Services call `apiClient` (Axios) → Backend REST API.
2. **State:** Server data lives in React Query cache keyed by `["domain", tenantSlug, ...params]`. Auth lives in the persisted Zustand `auth-storage` store. All other UI state is local `useState`.
3. **Cache invalidation:** every mutation invalidates its domain keys in `onSuccess`; order creation invalidates `orders`, `products`, and `cart` together.
4. **Tenancy:** nearly every domain endpoint is namespaced by `tenantSlug` (`/{slug}/products`, `/{slug}/cart`, ...), except public/platform endpoints (`/api/public/...`, `/api/tenants`, `/api/platform/...`, `/api/auth/...`). Guards keep role-specific pages accessible only to the right role.
5. **Design:** everything uses the Tailwind `@theme` tokens and the custom utilities in `index.css` (`.card`, `.input`, `.btn-primary`, `.chip`, `.text-gradient`, `.bg-gradient-brand`, `.shadow-glow`). Prices are always displayed as `₹{value}`.
6. **Currency note:** product prices are stored/returned as integers in Indian Rupees (e.g. `129999`); the UI prints them directly after `₹` without formatting/grouping.
