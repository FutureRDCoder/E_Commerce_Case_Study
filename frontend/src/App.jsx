import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AlertCircle,
  CheckCircle2,
  Clock,
  CreditCard,
  Filter,
  Heart,
  Layers,
  LogOut,
  Package,
  Plus,
  Search,
  ShieldCheck,
  ShoppingBag,
  Sparkles,
  Star,
  Store,
  Truck,
  User,
} from 'lucide-react'
import { apiClient, bootstrapAuthToken, getApiBaseUrl, setAuthToken } from './api'
import './App.css'

const emptyRegisterForm = {
  name: '',
  username: '',
  email: '',
  password: '',
  role: 'USER',
  tenantSlug: '',
}

const emptyLoginForm = {
  username: '',
  password: '',
}

const emptyProductForm = {
  name: '',
  description: '',
  price: '',
  category: '',
  availableQuantity: '',
  imageUrl: '',
}

const emptyTenantForm = {
  name: '',
  slug: '',
  description: '',
  logoUrl: '',
}

function App() {
  const [tenants, setTenants] = useState([])
  const [selectedTenant, setSelectedTenant] = useState('')
  const [products, setProducts] = useState([])
  const [favourites, setFavourites] = useState([])
  const [orders, setOrders] = useState([])
  const [activeTab, setActiveTab] = useState('catalog')
  const [authMode, setAuthMode] = useState('login')
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  const [registerForm, setRegisterForm] = useState(emptyRegisterForm)
  const [loginForm, setLoginForm] = useState(emptyLoginForm)
  const [productForm, setProductForm] = useState(emptyProductForm)
  const [tenantForm, setTenantForm] = useState(emptyTenantForm)
  const [selectedProductForOrder, setSelectedProductForOrder] = useState(null)
  const [orderQuantity, setOrderQuantity] = useState(1)
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const isManager = useMemo(
    () => currentUser?.role === 'ADMIN' || currentUser?.role === 'TENANT_ADMIN',
    [currentUser]
  )

  const fetchTenants = useCallback(async () => {
    try {
      const response = await apiClient.get('/api/platform/tenants')
      setTenants(response.data)
      if (response.data.length > 0 && !selectedTenant) {
        setSelectedTenant(response.data[0].slug)
      }
    } catch {
      setError('Unable to load platform store tenants.')
    }
  }, [selectedTenant])

  const fetchCurrentUser = useCallback(async () => {
    try {
      const response = await apiClient.get('/api/auth/me')
      setCurrentUser(response.data)
    } catch {
      setAuthToken(null)
      setCurrentUser(null)
    }
  }, [])

  const fetchProducts = useCallback(async (tenantSlug, category = '', search = '') => {
    if (!tenantSlug) return
    try {
      let url = `/${tenantSlug}/products?page=0&size=50`
      if (category) url += `&category=${encodeURIComponent(category)}`
      if (search) url += `&search=${encodeURIComponent(search)}`
      const response = await apiClient.get(url)
      setProducts(response.data.content || [])
    } catch {
      setError('Unable to load product catalog.')
    }
  }, [])

  const fetchFavourites = useCallback(async (tenantSlug) => {
    if (!currentUser || !tenantSlug) return
    try {
      const response = await apiClient.get(`/${tenantSlug}/favourites`)
      setFavourites(response.data)
    } catch {
      setError('Unable to load favourite items.')
    }
  }, [currentUser])

  const fetchOrders = useCallback(async (tenantSlug) => {
    if (!currentUser || !tenantSlug) return
    try {
      const isMgr = currentUser?.role === 'ADMIN' || currentUser?.role === 'TENANT_ADMIN'
      const url = isMgr ? `/${tenantSlug}/orders` : `/${tenantSlug}/orders/my-history`
      const response = await apiClient.get(url)
      setOrders(response.data)
    } catch {
      setError('Unable to load order history.')
    }
  }, [currentUser])

  useEffect(() => {
    const token = bootstrapAuthToken()
    fetchTenants()
    if (token) {
      fetchCurrentUser()
    }
  }, [fetchCurrentUser, fetchTenants])

  useEffect(() => {
    if (selectedTenant) {
      fetchProducts(selectedTenant, selectedCategory, searchQuery)
      if (currentUser) {
        fetchFavourites(selectedTenant)
        fetchOrders(selectedTenant)
      }
    }
  }, [selectedTenant, currentUser, selectedCategory, searchQuery, fetchProducts, fetchFavourites, fetchOrders])

  const handleRegister = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    setMessage('')
    try {
      const computedRole = registerForm.tenantSlug ? 'TENANT_ADMIN' : registerForm.role
      const payload = {
        ...registerForm,
        role: computedRole,
        tenantSlug: registerForm.tenantSlug || null,
      }
      const response = await apiClient.post('/api/auth/register', payload)
      setAuthToken(response.data.token)
      setCurrentUser(response.data)
      setRegisterForm(emptyRegisterForm)
      setMessage('Account created! Welcome, ' + (response.data.name || response.data.username) + ' (' + response.data.role + ')')
      setActiveTab('catalog')
      if (selectedTenant) {
        fetchFavourites(selectedTenant)
        fetchOrders(selectedTenant)
      }
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    setMessage('')
    try {
      const response = await apiClient.post('/api/auth/login', loginForm)
      setAuthToken(response.data.token)
      setCurrentUser(response.data)
      setLoginForm(emptyLoginForm)
      setMessage('Welcome back, ' + response.data.username + '! (' + response.data.role + ')')
      setActiveTab('catalog')
      if (selectedTenant) {
        fetchFavourites(selectedTenant)
        fetchOrders(selectedTenant)
      }
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Login failed.')
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    setAuthToken(null)
    setCurrentUser(null)
    setFavourites([])
    setOrders([])
    setMessage('Logged out successfully.')
  }

  const toggleFavourite = async (productId, isFavourite) => {
    if (!currentUser || !selectedTenant) return
    setError('')
    try {
      if (isFavourite) {
        await apiClient.delete(`/${selectedTenant}/favourites/${productId}`)
      } else {
        await apiClient.post(`/${selectedTenant}/favourites/${productId}`)
      }
      await fetchProducts(selectedTenant, selectedCategory, searchQuery)
      await fetchFavourites(selectedTenant)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Could not update favourite status.')
    }
  }

  const submitOrder = async (event) => {
    event.preventDefault()
    if (!selectedTenant || !selectedProductForOrder) return
    setError('')
    setMessage('')
    try {
      await apiClient.post(`/${selectedTenant}/orders`, {
        items: [{ productId: Number(selectedProductForOrder.id), quantity: Number(orderQuantity) }],
      })
      setMessage(`Order #${Math.floor(Math.random() * 8000 + 1000)} placed for ${orderQuantity}x ${selectedProductForOrder.name}!`)
      setSelectedProductForOrder(null)
      setOrderQuantity(1)
      await fetchProducts(selectedTenant, selectedCategory, searchQuery)
      await fetchOrders(selectedTenant)
      setActiveTab('orders')
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Order creation failed.')
    }
  }

  const createProduct = async (event) => {
    event.preventDefault()
    if (!selectedTenant || !isManager) return
    setError('')
    setMessage('')
    try {
      await apiClient.post(`/${selectedTenant}/products`, {
        ...productForm,
        price: Number(productForm.price),
        availableQuantity: Number(productForm.availableQuantity),
      })
      setProductForm(emptyProductForm)
      setMessage('Product added to store catalog!')
      await fetchProducts(selectedTenant, selectedCategory, searchQuery)
      setActiveTab('catalog')
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Product creation failed.')
    }
  }

  const createTenant = async (event) => {
    event.preventDefault()
    if (currentUser?.role !== 'ADMIN') return
    setError('')
    setMessage('')
    try {
      await apiClient.post('/api/platform/tenants', tenantForm)
      setTenantForm(emptyTenantForm)
      setMessage(`Tenant store '${tenantForm.name}' published successfully!`)
      await fetchTenants()
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Tenant creation failed.')
    }
  }

  const favouriteIds = useMemo(() => new Set(favourites.map((item) => item.id)), [favourites])

  const categories = useMemo(() => {
    const set = new Set(products.map((p) => p.category).filter(Boolean))
    return Array.from(set)
  }, [products])

  const currentStore = useMemo(
    () => tenants.find((t) => t.slug === selectedTenant),
    [tenants, selectedTenant]
  )

  return (
    <div className="app-container">
      {/* Top Navbar */}
      <nav className="navbar">
        <a href="#" className="brand-logo">
          <div className="brand-icon">
            <Store size={24} />
          </div>
          <div className="brand-text">
            <h1>OmniStore</h1>
            <p>Multi-Tenant Retail Platform</p>
          </div>
        </a>

        <div className="nav-actions">
          <div className="tenant-selector-pill">
            <Layers size={16} />
            <span>Select Brand:</span>
            <select value={selectedTenant} onChange={(e) => setSelectedTenant(e.target.value)}>
              {tenants.map((t) => (
                <option key={t.id} value={t.slug}>
                  {t.name} ({t.slug})
                </option>
              ))}
            </select>
          </div>

          <div className="status-badge">
            <span className="status-dot"></span>
            Live Backend
          </div>

          {currentUser ? (
            <div className="user-badge">
              <User size={15} />
              <span>{currentUser.username}</span>
              <span className="user-role-pill">{currentUser.role}</span>
              <button type="button" className="btn-logout" onClick={handleLogout} title="Logout">
                <LogOut size={16} />
              </button>
            </div>
          ) : null}
        </div>
      </nav>

      {/* Store Banner */}
      <section className="hero-banner">
        <div className="hero-content">
          <h2>{currentStore ? currentStore.name : 'OmniStore Flagship Store'}</h2>
          <p>{currentStore ? currentStore.description : 'Browse curated flagship products, place instant orders, and save favourite picks.'}</p>
          <div className="store-perks">
            <span className="perk-item">
              <Sparkles size={14} /> 100% Authentic Guarantee
            </span>
            <span className="perk-item">
              <Truck size={14} /> Free Express Shipping
            </span>
            <span className="perk-item">
              <ShieldCheck size={14} /> Secure Keycloak OAuth2
            </span>
          </div>
        </div>
      </section>

      {/* Notifications */}
      {message ? (
        <div className="status-msg ok">
          <CheckCircle2 size={18} />
          <span>{message}</span>
        </div>
      ) : null}
      {error ? (
        <div className="status-msg error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      ) : null}

      {/* Navigation Tabs */}
      <div className="tabs-header">
        <button
          type="button"
          className={`tab-btn ${activeTab === 'catalog' ? 'active' : ''}`}
          onClick={() => setActiveTab('catalog')}
        >
          <ShoppingBag size={16} />
          Store Catalog ({products.length})
        </button>

        {currentUser ? (
          <>
            <button
              type="button"
              className={`tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
              onClick={() => setActiveTab('orders')}
            >
              <Package size={16} />
              My Orders ({orders.length})
            </button>

            <button
              type="button"
              className={`tab-btn ${activeTab === 'favourites' ? 'active' : ''}`}
              onClick={() => setActiveTab('favourites')}
            >
              <Heart size={16} />
              Favourites ({favourites.length})
            </button>
          </>
        ) : null}

        {isManager ? (
          <button
            type="button"
            className={`tab-btn ${activeTab === 'manage-products' ? 'active' : ''}`}
            onClick={() => setActiveTab('manage-products')}
          >
            <Plus size={16} />
            Add Product
          </button>
        ) : null}

        {currentUser?.role === 'ADMIN' ? (
          <button
            type="button"
            className={`tab-btn ${activeTab === 'admin-tenants' ? 'active' : ''}`}
            onClick={() => setActiveTab('admin-tenants')}
          >
            <ShieldCheck size={16} />
            Platform Admin Console ({tenants.length})
          </button>
        ) : null}

        {!currentUser ? (
          <button
            type="button"
            className={`tab-btn ${activeTab === 'auth' ? 'active' : ''}`}
            onClick={() => setActiveTab('auth')}
          >
            <User size={16} />
            Sign In / Register
          </button>
        ) : null}
      </div>

      {/* TAB 1: Catalog */}
      {activeTab === 'catalog' ? (
        <section className="glass-panel">
          <div className="panel-header">
            <h3 className="panel-title">
              <ShoppingBag size={22} />
              {currentStore ? currentStore.name : 'Store'} Catalog
            </h3>
          </div>

          <div className="filter-bar">
            <div className="search-input-wrapper">
              <Search size={18} />
              <input
                type="text"
                className="search-input"
                placeholder="Search products by name..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            <div className="category-pills">
              <button
                type="button"
                className={`cat-pill ${selectedCategory === '' ? 'active' : ''}`}
                onClick={() => setSelectedCategory('')}
              >
                All Categories
              </button>
              {categories.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  className={`cat-pill ${selectedCategory === cat ? 'active' : ''}`}
                  onClick={() => setSelectedCategory(cat)}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {products.length === 0 ? (
            <div className="empty-state">
              <Package size={52} />
              <p>No products found matching criteria for this store.</p>
            </div>
          ) : (
            <div className="products-grid">
              {products.map((p) => {
                const isFav = favouriteIds.has(p.id)
                return (
                  <article key={p.id} className="product-card">
                    <div className="product-image-container">
                      {p.imageUrl ? (
                        <img src={p.imageUrl} alt={p.name} />
                      ) : (
                        <div className="product-image-placeholder">
                          <Package size={36} />
                          <span>Official Product Image</span>
                        </div>
                      )}
                      {currentUser ? (
                        <button
                          type="button"
                          className={`fav-btn-top ${isFav ? 'is-fav' : ''}`}
                          onClick={() => toggleFavourite(p.id, isFav)}
                          title={isFav ? 'Remove Favourite' : 'Mark Favourite'}
                        >
                          <Heart size={18} fill={isFav ? '#ec4899' : 'none'} />
                        </button>
                      ) : null}
                    </div>

                    <div>
                      <div className="product-rating">
                        <Star size={12} fill="#fbbf24" color="#fbbf24" />
                        <Star size={12} fill="#fbbf24" color="#fbbf24" />
                        <Star size={12} fill="#fbbf24" color="#fbbf24" />
                        <Star size={12} fill="#fbbf24" color="#fbbf24" />
                        <Star size={12} fill="#fbbf24" color="#fbbf24" />
                        <span style={{ color: 'var(--text-muted)', marginLeft: '4px' }}>4.9 (128)</span>
                      </div>
                      <span className="product-category-tag">{p.category}</span>
                      <h4 className="product-title">{p.name}</h4>
                      <p className="product-desc">{p.description || 'Premium store merchandise.'}</p>
                    </div>

                    <div>
                      <div className="product-footer">
                        <div className="product-price-box">
                          <span className="product-price">₹{p.price}</span>
                          <span className="product-original-price">₹{(p.price * 1.2).toFixed(2)}</span>
                        </div>
                        <span
                          className={`stock-badge ${
                            p.availableQuantity > 10
                              ? 'in-stock'
                              : p.availableQuantity > 0
                              ? 'low-stock'
                              : 'out-of-stock'
                          }`}
                        >
                          {p.availableQuantity > 0 ? `${p.availableQuantity} in stock` : 'Out of Stock'}
                        </span>
                      </div>

                      {currentUser ? (
                        <button
                          type="button"
                          className="btn-primary"
                          disabled={p.availableQuantity <= 0}
                          onClick={() => setSelectedProductForOrder(p)}
                        >
                          <ShoppingBag size={16} />
                          {p.availableQuantity > 0 ? 'Buy / Order Now' : 'Sold Out'}
                        </button>
                      ) : (
                        <button
                          type="button"
                          className="btn-secondary"
                          style={{ width: '100%', marginTop: '12px' }}
                          onClick={() => setActiveTab('auth')}
                        >
                          Sign In to Buy
                        </button>
                      )}
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </section>
      ) : null}

      {/* Order Modal */}
      {selectedProductForOrder ? (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="panel-header">
              <h3 className="panel-title">
                <ShoppingBag size={22} />
                Order Checkout: {selectedProductForOrder.name}
              </h3>
            </div>
            <form onSubmit={submitOrder} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label>Selected Product</label>
                <input className="input-field" value={`${selectedProductForOrder.name} (${selectedProductForOrder.category})`} disabled />
              </div>
              <div className="form-group">
                <label>Unit Price</label>
                <input className="input-field" value={`₹${selectedProductForOrder.price}`} disabled />
              </div>
              <div className="form-group">
                <label>Quantity (Available Stock: {selectedProductForOrder.availableQuantity})</label>
                <input
                  type="number"
                  className="input-field"
                  min="1"
                  max={selectedProductForOrder.availableQuantity}
                  value={orderQuantity}
                  onChange={(e) => setOrderQuantity(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Total Price</label>
                <input
                  className="input-field"
                  style={{ fontWeight: '800', color: 'var(--success)' }}
                  value={`₹${(selectedProductForOrder.price * (orderQuantity || 1)).toFixed(2)}`}
                  disabled
                />
              </div>

              <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
                <button type="submit" className="btn-primary" style={{ flex: 1 }}>
                  Confirm & Place Order
                </button>
                <button type="button" className="btn-secondary" onClick={() => setSelectedProductForOrder(null)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {/* TAB 2: Orders with FULL ITEM BREAKDOWN & RECEIPT */}
      {activeTab === 'orders' && currentUser ? (
        <section className="glass-panel">
          <div className="panel-header">
            <h3 className="panel-title">
              <Package size={22} />
              My Order History & Receipts ({orders.length})
            </h3>
          </div>
          {orders.length === 0 ? (
            <div className="empty-state">
              <Package size={52} />
              <p>You haven't placed any orders under {selectedTenant} yet.</p>
            </div>
          ) : (
            orders.map((o) => (
              <article key={o.id} className="order-card">
                <div className="order-header">
                  <div className="order-id-info">
                    <Package size={20} style={{ color: 'var(--accent-primary)' }} />
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                        <h4>Order #{o.id}</h4>
                        {o.username ? (
                          <span className="customer-badge">
                            <User size={12} />
                            Customer: <strong>{o.userFullName || o.username}</strong> (@{o.username})
                          </span>
                        ) : null}
                      </div>
                      <span className="order-date">
                        <Clock size={12} style={{ verticalAlign: 'middle', marginRight: '4px' }} />
                        {new Date(o.orderDate).toLocaleString()}
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span className="user-role-pill" style={{ background: 'rgba(16, 185, 129, 0.2)', color: '#34d399' }}>
                      {o.status}
                    </span>
                  </div>
                </div>

                {/* Itemized Order Details */}
                <div className="order-items-list">
                  {o.items && o.items.length > 0 ? (
                    o.items.map((item) => (
                      <div key={item.id} className="order-item-row">
                        <div className="order-item-detail">
                          {item.productImageUrl ? (
                            <img src={item.productImageUrl} alt={item.productName} className="order-item-thumb" />
                          ) : (
                            <div className="order-item-thumb" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                              <Package size={20} color="var(--text-muted)" />
                            </div>
                          )}
                          <div>
                            <div className="order-item-title">{item.productName}</div>
                            <div className="order-item-meta">
                              Category: {item.productCategory} | Qty: {item.quantity} x ₹{item.unitPrice}
                            </div>
                          </div>
                        </div>
                        <div className="order-item-price">₹{item.subtotal}</div>
                      </div>
                    ))
                  ) : (
                    <div className="order-item-row">
                      <span className="order-item-meta">Standard Order ({o.totalQuantity} items)</span>
                      <span className="order-item-price">₹{o.totalAmount}</span>
                    </div>
                  )}
                </div>

                <div className="order-footer-summary">
                  <div style={{ fontSize: '13px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Truck size={15} /> Express Delivery • Total Items: {o.totalQuantity}
                  </div>
                  <div>
                    <span style={{ fontSize: '12px', color: 'var(--text-muted)', marginRight: '8px' }}>Total Amount Paid:</span>
                    <span className="total-amount-pill">₹{o.totalAmount}</span>
                  </div>
                </div>
              </article>
            ))
          )}
        </section>
      ) : null}

      {/* TAB 3: Favourites */}
      {activeTab === 'favourites' && currentUser ? (
        <section className="glass-panel">
          <div className="panel-header">
            <h3 className="panel-title">
              <Heart size={22} />
              Saved Favourite Products ({favourites.length})
            </h3>
          </div>
          {favourites.length === 0 ? (
            <div className="empty-state">
              <Heart size={52} />
              <p>You haven't marked any products as favourite yet.</p>
            </div>
          ) : (
            <div className="products-grid">
              {favourites.map((fav) => (
                <article key={fav.id} className="product-card">
                  <span className="product-category-tag">{fav.category}</span>
                  <h4 className="product-title">{fav.name}</h4>
                  <p className="product-desc">{fav.description}</p>
                  <div className="product-footer">
                    <span className="product-price">₹{fav.price}</span>
                    <button
                      type="button"
                      className="btn-danger"
                      onClick={() => toggleFavourite(fav.id, true)}
                    >
                      Remove
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      ) : null}

      {/* TAB 4: Manage Products */}
      {activeTab === 'manage-products' && isManager ? (
        <section className="glass-panel">
          <div className="panel-header">
            <h3 className="panel-title">
              <Plus size={22} />
              Add Product to {currentStore ? currentStore.name : selectedTenant} Catalog
            </h3>
          </div>
          <form onSubmit={createProduct} className="form-grid">
            <div className="form-group">
              <label>Product Name</label>
              <input
                type="text"
                className="input-field"
                placeholder="e.g. Air Max 270"
                value={productForm.name}
                onChange={(e) => setProductForm((p) => ({ ...p, name: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <input
                type="text"
                className="input-field"
                placeholder="Product specifications..."
                value={productForm.description}
                onChange={(e) => setProductForm((p) => ({ ...p, description: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Price (₹)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="input-field"
                placeholder="129.99"
                value={productForm.price}
                onChange={(e) => setProductForm((p) => ({ ...p, price: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Category</label>
              <input
                type="text"
                className="input-field"
                placeholder="Footwear, Apparel, Accessories"
                value={productForm.category}
                onChange={(e) => setProductForm((p) => ({ ...p, category: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Initial Stock Quantity</label>
              <input
                type="number"
                min="0"
                className="input-field"
                placeholder="50"
                value={productForm.availableQuantity}
                onChange={(e) => setProductForm((p) => ({ ...p, availableQuantity: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Image URL (Optional)</label>
              <input
                type="url"
                className="input-field"
                placeholder="https://images.unsplash.com/..."
                value={productForm.imageUrl}
                onChange={(e) => setProductForm((p) => ({ ...p, imageUrl: e.target.value }))}
              />
            </div>
            <div style={{ gridColumn: '1 / -1' }}>
              <button type="submit" className="btn-primary">
                Publish Product to Store
              </button>
            </div>
          </form>
        </section>
      ) : null}

      {/* TAB 5: Platform Admin Console */}
      {activeTab === 'admin-tenants' && currentUser?.role === 'ADMIN' ? (
        <section className="glass-panel">
          <div className="panel-header">
            <h3 className="panel-title">
              <ShieldCheck size={22} />
              Platform Admin Console - Manage Tenant Stores
            </h3>
          </div>

          <form onSubmit={createTenant} className="form-grid" style={{ marginBottom: '32px' }}>
            <div className="form-group">
              <label>Store Name</label>
              <input
                type="text"
                className="input-field"
                placeholder="Sony Official"
                value={tenantForm.name}
                onChange={(e) => setTenantForm((t) => ({ ...t, name: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Store Slug (URL Identifier)</label>
              <input
                type="text"
                className="input-field"
                placeholder="sony"
                value={tenantForm.slug}
                onChange={(e) => setTenantForm((t) => ({ ...t, slug: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <input
                type="text"
                className="input-field"
                placeholder="Store slogan..."
                value={tenantForm.description}
                onChange={(e) => setTenantForm((t) => ({ ...t, description: e.target.value }))}
                required
              />
            </div>
            <div style={{ gridColumn: '1 / -1' }}>
              <button type="submit" className="btn-primary">
                Create Brand Tenant Store
              </button>
            </div>
          </form>

          <h4>Active Multi-Tenant Stores ({tenants.length})</h4>
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Brand Name</th>
                <th>Domain Slug</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {tenants.map((t) => (
                <tr key={t.id}>
                  <td>#{t.id}</td>
                  <td>
                    <strong>{t.name}</strong>
                  </td>
                  <td>
                    <span className="user-role-pill">{t.slug}</span>
                  </td>
                  <td>{t.description || 'N/A'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      ) : null}

      {/* Auth Modal */}
      {activeTab === 'auth' && !currentUser ? (
        <section className="glass-panel" style={{ maxWidth: '460px', margin: '0 auto' }}>
          <div className="tabs-header" style={{ marginBottom: '24px' }}>
            <button
              type="button"
              className={`tab-btn ${authMode === 'login' ? 'active' : ''}`}
              onClick={() => setAuthMode('login')}
            >
              Sign In
            </button>
            <button
              type="button"
              className={`tab-btn ${authMode === 'register' ? 'active' : ''}`}
              onClick={() => setAuthMode('register')}
            >
              Create Account
            </button>
          </div>

          {authMode === 'login' ? (
            <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Enter your username"
                  value={loginForm.username}
                  onChange={(e) => setLoginForm((p) => ({ ...p, username: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  className="input-field"
                  placeholder="Enter your password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm((p) => ({ ...p, password: e.target.value }))}
                  required
                />
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Authenticating...' : 'Sign In'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label>Full Name</label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Full Name"
                  value={registerForm.name}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, name: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Unique Username"
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, username: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  className="input-field"
                  placeholder="user@example.com"
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, email: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  className="input-field"
                  placeholder="Password"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, password: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Account Role / Type</label>
                <select
                  className="input-field"
                  value={registerForm.role}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, role: e.target.value }))}
                >
                  <option value="USER">Regular Buyer / Customer</option>
                  <option value="TENANT_ADMIN">Tenant Brand Store Manager</option>
                </select>
              </div>
              <div className="form-group">
                <label>
                  Brand Store Slug{' '}
                  {registerForm.role === 'TENANT_ADMIN' || registerForm.tenantSlug
                    ? '(Store Manager Domain)'
                    : '(Optional)'}
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g. nike, adidas, puma, apple"
                  value={registerForm.tenantSlug}
                  onChange={(e) => setRegisterForm((p) => ({ ...p, tenantSlug: e.target.value }))}
                  required={registerForm.role === 'TENANT_ADMIN'}
                />
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating Account...' : 'Register Account'}
              </button>
            </form>
          )}
        </section>
      ) : null}
    </div>
  )
}

export default App
