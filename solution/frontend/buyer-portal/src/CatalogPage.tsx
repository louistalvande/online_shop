import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, AppShell, LangToggle, CartIcon, UserMenu } from '@workspace/theme'
import { fetchProducts, type BuyerProduct, type CatalogFilters } from './api/catalogApi'
import { getSession, logout, type BuyerSession } from './api/authApi'

const PAGE_SIZE = 20

function formatPrice(price: number): string {
  return price.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

export default function CatalogPage() {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState<BuyerSession | null>(getSession)

  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)
  const [page, setPage] = useState(0)

  const [products, setProducts] = useState<BuyerProduct[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)

  const [pendingSearch, setPendingSearch] = useState('')

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
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      inStockOnly,
      search: search || undefined,
      page,
    })
  }, [category, maxPrice, inStockOnly, search, page, load])

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSearch(pendingSearch)
    setPage(0)
  }

  function handleReset() {
    setCategory('')
    setMaxPrice('')
    setInStockOnly(false)
    setSearch('')
    setPendingSearch('')
    setPage(0)
  }

  function handleFilterChange() {
    setPage(0)
  }

  return (
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
            onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')}
          />
          {session ? (
            <UserMenu
              label={t('nav.account')}
              email={session.email}
              settingsLabel={t('nav.profile')}
              logoutLabel={t('nav.logout')}
              onSettings={() => { window.location.href = '/profile' }}
              onLogout={() => { logout(); setSession(null) }}
            />
          ) : (
            <Button variant="ghost" size="sm" onClick={() => { window.location.href = '/login' }}>
              {t('nav.login')}
            </Button>
          )}
          <Button variant="ghost" size="sm" aria-label={t('nav.cart')}>
            <CartIcon size={22} />
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
                <span className="catalog-filter-hint">{t('catalog.filters.maxPrice')}</span>
                <input
                  type="number"
                  min={0}
                  step={0.01}
                  value={maxPrice}
                  onChange={e => { setMaxPrice(e.target.value); handleFilterChange() }}
                  placeholder="—"
                  className="catalog-filter-input"
                />
              </label>

              <label className="catalog-filter-stock-label">
                <input
                  type="checkbox"
                  checked={inStockOnly}
                  onChange={e => { setInStockOnly(e.target.checked); handleFilterChange() }}
                />
                <span className="catalog-filter-stock-text">{t('catalog.filters.inStockOnly')}</span>
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
                      {p.photoUrls.length > 0 ? (
                        <img
                          src={p.photoUrls[0]}
                          alt={p.name}
                          className="catalog-product-image"
                        />
                      ) : (
                        <div className="catalog-product-placeholder" />
                      )}
                      <div className="catalog-product-body">
                        {p.category && (
                          <span className="catalog-product-category">{p.category}</span>
                        )}
                        <h3 className="catalog-product-name">{p.name}</h3>
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
                            <Button variant="secondary" size="sm">
                              {t('product.addToCart')}
                            </Button>
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
  )
}
