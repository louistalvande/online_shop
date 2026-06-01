import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, AppShell, LangToggle, CartIcon, UserMenu, Snackbar } from '@workspace/theme'
import { fetchProducts, type BuyerProduct, type CatalogFilters } from './api/catalogApi'
import { addToCart } from './api/cartApi'
import { getSession, logout, type BuyerSession } from './api/authApi'
import LoginModal from './LoginModal'
import { useCartCount } from './hooks/useCartCount'

const PAGE_SIZE = 20

function formatPrice(price: number): string {
  return price.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

export default function CatalogPage() {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState<BuyerSession | null>(getSession)

  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [theme, setTheme] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)
  const [page, setPage] = useState(0)

  const [products, setProducts] = useState<BuyerProduct[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)

  const [pendingSearch, setPendingSearch] = useState('')

  const [quantities, setQuantities] = useState<Record<string, number>>({})
  const [addingId, setAddingId] = useState<string | null>(null)
  const [cartFeedback, setCartFeedback] = useState<{ id: string; ok: boolean } | null>(null)
  const [showLogin, setShowLogin] = useState(false)
  const [pendingCartProductId, setPendingCartProductId] = useState<string | null>(null)
  const [snackbar, setSnackbar] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)
  const cartCount = useCartCount()

  useEffect(() => {
    if (session && pendingCartProductId) {
      const id = pendingCartProductId
      setPendingCartProductId(null)
      doAddToCart(id)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session])

  const load = useCallback(async (filters: CatalogFilters) => {
    setLoading(true)
    setError(false)
    try {
      const data = await fetchProducts({ ...filters, size: PAGE_SIZE })
      setProducts(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load({
      category: category || undefined,
      theme: theme || undefined,
      search: search || undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      inStockOnly: inStockOnly || undefined,
      page,
    })
  }, [category, theme, search, maxPrice, inStockOnly, page, load])

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSearch(pendingSearch)
    setPage(0)
  }

  function handleReset() {
    setCategory('')
    setTheme('')
    setSearch('')
    setPendingSearch('')
    setMaxPrice('')
    setInStockOnly(false)
    setPage(0)
  }

  function handleFilterChange() {
    setPage(0)
  }

  function getQty(id: string) { return quantities[id] ?? 1 }
  function setQty(id: string, val: number) {
    setQuantities(prev => ({ ...prev, [id]: Math.max(1, val) }))
  }

  async function doAddToCart(productId: string) {
    setAddingId(productId)
    setCartFeedback(null)
    try {
      await addToCart(productId, getQty(productId))
      window.dispatchEvent(new Event('cart-updated'))
      setCartFeedback({ id: productId, ok: true })
      setSnackbar({ message: t('cart.added'), variant: 'success' })
    } catch {
      setCartFeedback({ id: productId, ok: false })
      setSnackbar({ message: t('cart.error.add'), variant: 'error' })
    } finally {
      setAddingId(null)
      setTimeout(() => setCartFeedback(null), 2500)
    }
  }

  async function handleAddToCart(productId: string) {
    if (!session) { setPendingCartProductId(productId); setShowLogin(true); return }
    await doAddToCart(productId)
  }

  return (
    <>
    {snackbar && <Snackbar message={snackbar.message} variant={snackbar.variant} onDismiss={() => setSnackbar(null)} />}
    {showLogin && (
      <LoginModal
        onClose={() => { setShowLogin(false); setPendingCartProductId(null) }}
        onLogin={(s) => {
          setSession(s)
          setShowLogin(false)
          window.dispatchEvent(new Event('session-changed'))
        }}
      />
    )}
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.home'), href: '/' },
        { label: t('nav.catalog'), href: '/catalog' },
      ]}
      actions={
        <div className="header-actions">
          <LangToggle
            lang={i18n.language}
            onToggle={() => i18n.changeLanguage(({ fr: 'en', en: 'es', es: 'fr' } as Record<string, string>)[i18n.language] ?? 'fr')}
          />
          {session ? (
            <UserMenu
              label={t('nav.account')}
              email={session.email}
              settingsLabel={t('nav.profile')}
              logoutLabel={t('nav.logout')}
              onSettings={() => { window.location.href = '/profile' }}
              onLogout={() => { logout(); setSession(null); window.dispatchEvent(new Event('session-changed')) }}
            />
          ) : (
            <Button variant="ghost" size="sm" onClick={() => { window.location.href = '/login' }}>
              {t('nav.login')}
            </Button>
          )}
          <Button variant="ghost" size="sm" className="cart-icon-btn" aria-label={t('nav.cart')} onClick={() => { window.location.href = '/cart' }}>
            <span className="cart-btn-wrapper">
              <CartIcon size={22} />
              {cartCount > 0 && <span className="cart-badge">{cartCount > 99 ? '99+' : cartCount}</span>}
            </span>
          </Button>
        </div>
      }
    >
      <div className="catalog-layout">
        {/* Search bar */}
        <form onSubmit={handleSearchSubmit} className="catalog-search-form">
          <input
            type="search"
            value={pendingSearch}
            onChange={e => setPendingSearch(e.target.value)}
            placeholder={t('catalog.search.placeholder')}
            aria-label={t('catalog.search.placeholder')}
            className="catalog-search-input"
          />
          <Button type="submit">{t('catalog.search.submit')}</Button>
        </form>

        <div className="catalog-grid-layout">
          {/* Filters sidebar */}
          <aside>
            <Card className="catalog-filter-card">
              <h2 className="catalog-filter-title">{t('catalog.filters.title')}</h2>

              <label className="catalog-filter-label">
                <span className="catalog-filter-hint">{t('catalog.filters.category')}</span>
                <input
                  type="text"
                  value={category}
                  onChange={e => { setCategory(e.target.value); handleFilterChange() }}
                  placeholder={t('catalog.filters.categoryAll')}
                  className="catalog-filter-input"
                />
              </label>

              <label className="catalog-filter-label">
                <span className="catalog-filter-hint">{t('catalog.filters.theme')}</span>
                <input
                  type="text"
                  value={theme}
                  onChange={e => { setTheme(e.target.value); handleFilterChange() }}
                  placeholder={t('catalog.filters.themeAll')}
                  className="catalog-filter-input"
                />
              </label>

              <label className="catalog-filter-label">
                <span className="catalog-filter-hint">{t('catalog.filters.maxPrice')}</span>
                <input
                  type="number"
                  min={0}
                  value={maxPrice}
                  onChange={e => { setMaxPrice(e.target.value); handleFilterChange() }}
                  className="catalog-filter-input"
                />
              </label>

              <label className="catalog-filter-label catalog-filter-checkbox-label">
                <input
                  type="checkbox"
                  checked={inStockOnly}
                  onChange={e => { setInStockOnly(e.target.checked); handleFilterChange() }}
                />
                {t('catalog.filters.inStockOnly')}
              </label>

              <Button variant="secondary" size="sm" onClick={handleReset} className="catalog-filter-reset-btn">
                {t('catalog.filters.reset')}
              </Button>
            </Card>
          </aside>

          {/* Product grid */}
          <main>
            {loading && (
              <p className="catalog-status-text">{t('catalog.loading')}</p>
            )}
            {error && (
              <p className="catalog-status-text">{t('catalog.error.load')}</p>
            )}
            {!loading && !error && products.length === 0 && (
              <div className="catalog-empty">
                <p className="catalog-empty-text">{t('catalog.noResults')}</p>
                <Button variant="secondary" onClick={handleReset}>{t('catalog.filters.reset')}</Button>
              </div>
            )}
            {!loading && products.length > 0 && (
              <>
                <p className="catalog-result-count">
                  {t('catalog.resultCount', { count: totalElements })}
                </p>
                <div className="catalog-product-grid">
                  {products.map(p => (
                    <Card key={p.id}>
                      <a href={`/catalog/${p.id}`} style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}>
                        {p.photoUrls.length > 0 ? (
                          <img
                            src={p.photoUrls[0]}
                            alt={p.name}
                            className="catalog-product-image"
                          />
                        ) : (
                          <div className="catalog-product-placeholder" />
                        )}
                        <div className="catalog-product-body" style={{ paddingBottom: 0 }}>
                          {p.category && (
                            <span className="catalog-product-category">{p.category}</span>
                          )}
                          <h3 className="catalog-product-name">{p.name}</h3>
                        </div>
                      </a>
                      <div className="catalog-product-body" style={{ paddingTop: 0 }}>
                        <div className="catalog-product-footer">
                          <div>
                            <div className="catalog-product-price">{formatPrice(p.priceTTC)}</div>
                            <div className="catalog-product-excl-tax">
                              {t('catalog.priceExclTax', { price: formatPrice(p.priceExclTax) })}
                            </div>
                          </div>
                          {p.outOfStock ? (
                            <Button variant="secondary" size="sm" disabled>
                              {t('catalog.outOfStock')}
                            </Button>
                          ) : (
                            <div className="add-to-cart-compact">
                              <div className="qty-stepper qty-stepper--sm">
                                <button className="qty-btn qty-btn--sm" onClick={() => setQty(p.id, getQty(p.id) - 1)} aria-label="-">−</button>
                                <span className="qty-value qty-value--sm">{getQty(p.id)}</span>
                                <button className="qty-btn qty-btn--sm" onClick={() => setQty(p.id, getQty(p.id) + 1)} aria-label="+">+</button>
                              </div>
                              <Button
                                variant="secondary"
                                size="sm"
                                disabled={addingId === p.id}
                                onClick={() => handleAddToCart(p.id)}
                                aria-label={t('product.addToCart')}
                              >
                                {cartFeedback?.id === p.id && cartFeedback.ok
                                  ? '✓'
                                  : addingId === p.id
                                    ? '…'
                                    : <CartIcon size={15} />}
                              </Button>
                            </div>
                          )}
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="catalog-pagination">
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage(p => p - 1)}
                    >
                      {t('catalog.page.prev')}
                    </Button>
                    <span className="catalog-page-info">
                      {t('catalog.page.info', { current: page + 1, total: totalPages })}
                    </span>
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={page >= totalPages - 1}
                      onClick={() => setPage(p => p + 1)}
                    >
                      {t('catalog.page.next')}
                    </Button>
                  </div>
                )}
              </>
            )}
          </main>
        </div>
      </div>
    </AppShell>
    </>
  )
}
