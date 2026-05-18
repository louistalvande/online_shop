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
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
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
      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 24px 64px' }}>
        {/* Search bar */}
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: 8, marginBottom: 32 }}>
          <input
            type="search"
            value={pendingSearch}
            onChange={e => setPendingSearch(e.target.value)}
            placeholder={t('catalog.search.placeholder')}
            aria-label={t('catalog.search.placeholder')}
            style={{
              flex: 1, padding: '8px 12px', border: '1px solid var(--border)',
              borderRadius: 6, fontSize: 14, background: 'var(--bg)',
            }}
          />
          <Button type="submit">{t('catalog.search.submit')}</Button>
        </form>

        <div style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: 32, alignItems: 'start' }}>
          {/* Filters sidebar */}
          <aside>
            <Card style={{ padding: '20px 18px' }}>
              <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>{t('catalog.filters.title')}</h2>

              <label style={{ display: 'block', marginBottom: 12 }}>
                <span style={{ fontSize: 13, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>
                  {t('catalog.filters.category')}
                </span>
                <input
                  type="text"
                  value={category}
                  onChange={e => { setCategory(e.target.value); handleFilterChange() }}
                  placeholder={t('catalog.filters.categoryAll')}
                  style={{
                    width: '100%', padding: '6px 10px', border: '1px solid var(--border)',
                    borderRadius: 4, fontSize: 13, background: 'var(--bg)',
                  }}
                />
              </label>

              <label style={{ display: 'block', marginBottom: 12 }}>
                <span style={{ fontSize: 13, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>
                  {t('catalog.filters.maxPrice')}
                </span>
                <input
                  type="number"
                  min={0}
                  step={0.01}
                  value={maxPrice}
                  onChange={e => { setMaxPrice(e.target.value); handleFilterChange() }}
                  placeholder="—"
                  style={{
                    width: '100%', padding: '6px 10px', border: '1px solid var(--border)',
                    borderRadius: 4, fontSize: 13, background: 'var(--bg)',
                  }}
                />
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16, cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={inStockOnly}
                  onChange={e => { setInStockOnly(e.target.checked); handleFilterChange() }}
                />
                <span style={{ fontSize: 13 }}>{t('catalog.filters.inStockOnly')}</span>
              </label>

              <Button variant="secondary" size="sm" onClick={handleReset} style={{ width: '100%' }}>
                {t('catalog.filters.reset')}
              </Button>
            </Card>
          </aside>

          {/* Product grid */}
          <main>
            {loading && (
              <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 40 }}>
                {t('catalog.loading')}
              </p>
            )}
            {error && (
              <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 40 }}>
                {t('catalog.error.load')}
              </p>
            )}
            {!loading && !error && products.length === 0 && (
              <div style={{ textAlign: 'center', padding: '48px 24px' }}>
                <p style={{ color: 'var(--text-muted)', marginBottom: 16 }}>{t('catalog.noResults')}</p>
                <Button variant="secondary" onClick={handleReset}>{t('catalog.filters.reset')}</Button>
              </div>
            )}
            {!loading && products.length > 0 && (
              <>
                <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 20 }}>
                  {t('catalog.resultCount', { count: totalElements })}
                </p>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 20 }}>
                  {products.map(p => (
                    <Card key={p.id}>
                      {p.photoUrls.length > 0 ? (
                        <img
                          src={p.photoUrls[0]}
                          alt={p.name}
                          style={{ width: '100%', height: 180, objectFit: 'cover' }}
                        />
                      ) : (
                        <div style={{ background: '#f0ece4', height: 180 }} />
                      )}
                      <div style={{ padding: '14px 16px 16px' }}>
                        {p.category && (
                          <span style={{ fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--accent)' }}>
                            {p.category}
                          </span>
                        )}
                        <h3 style={{ fontSize: 15, fontWeight: 600, margin: '6px 0 8px', lineHeight: 1.3 }}>{p.name}</h3>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 12 }}>
                          <div>
                            <div style={{ fontWeight: 700, fontSize: 15 }}>{formatPrice(p.priceTTC)}</div>
                            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>
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
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 12, marginTop: 32 }}>
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage(p => p - 1)}
                    >
                      {t('catalog.page.prev')}
                    </Button>
                    <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>
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
