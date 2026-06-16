import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { getShopSeo, saveShopSeo, getProductSeo, saveProductSeo, deleteProductSeo, type ShopSeoConfig, type ProductSeoOverride } from './api/seoApi'
import { listProducts, type Product } from './api/productApi'

type Tab = 'global' | 'products' | 'indexation' | 'tools'

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '8px 12px',
  border: '1px solid #d1d5db',
  borderRadius: 6,
  fontSize: 14,
  boxSizing: 'border-box',
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontWeight: 600,
  fontSize: 13,
  marginBottom: 4,
  color: '#374151',
}

const fieldStyle: React.CSSProperties = { marginBottom: 16 }

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div style={fieldStyle}>
      <label style={labelStyle}>{label}</label>
      {children}
    </div>
  )
}

export default function SeoPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<Tab>('global')

  // --- Global SEO state ---
  const [config, setConfig] = useState<ShopSeoConfig | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<'saved' | 'error' | null>(null)

  // --- Products tab state ---
  const [products, setProducts] = useState<Product[]>([])
  const [productsLoading, setProductsLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [productOverride, setProductOverride] = useState<Omit<ProductSeoOverride, 'productId'>>({
    seoTitle: null,
    seoDescription: null,
    seoKeywords: null,
    ogImageUrl: null,
  })
  const [productOverrides, setProductOverrides] = useState<Record<string, ProductSeoOverride | null>>({})
  const [productFeedback, setProductFeedback] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    getShopSeo()
      .then(setConfig)
      .catch(() => setFeedback('error'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (tab === 'products') {
      setProductsLoading(true)
      listProducts()
        .then(setProducts)
        .catch(() => {})
        .finally(() => setProductsLoading(false))
    }
  }, [tab])

  async function handleSaveGlobal() {
    if (!config) return
    setSaving(true)
    setFeedback(null)
    try {
      const saved = await saveShopSeo(config)
      setConfig(saved)
      setFeedback('saved')
    } catch {
      setFeedback('error')
    } finally {
      setSaving(false)
      setTimeout(() => setFeedback(null), 3000)
    }
  }

  async function openEditProduct(product: Product) {
    setEditingProduct(product)
    const existing = await getProductSeo(product.id)
    setProductOverride({
      seoTitle: existing?.seoTitle ?? null,
      seoDescription: existing?.seoDescription ?? null,
      seoKeywords: existing?.seoKeywords ?? null,
      ogImageUrl: existing?.ogImageUrl ?? null,
    })
  }

  async function handleSaveProductSeo() {
    if (!editingProduct) return
    try {
      const saved = await saveProductSeo(editingProduct.id, productOverride)
      setProductOverrides(prev => ({ ...prev, [editingProduct.id]: saved }))
      setProductFeedback(t('seo.products.saveDone'))
      setEditingProduct(null)
    } catch {
      setProductFeedback(t('seo.saveError'))
    } finally {
      setTimeout(() => setProductFeedback(null), 3000)
    }
  }

  async function handleResetProductSeo(productId: string) {
    if (!window.confirm(t('seo.products.resetConfirm'))) return
    try {
      await deleteProductSeo(productId)
      setProductOverrides(prev => ({ ...prev, [productId]: null }))
      setProductFeedback(t('seo.products.resetDone'))
    } catch {
      setProductFeedback(t('seo.saveError'))
    } finally {
      setTimeout(() => setProductFeedback(null), 3000)
    }
  }

  const filteredProducts = products.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase())
  )

  const tabStyle = (active: boolean): React.CSSProperties => ({
    padding: '8px 20px',
    border: 'none',
    borderBottom: active ? '2px solid var(--accent, #6366f1)' : '2px solid transparent',
    background: 'none',
    cursor: 'pointer',
    fontWeight: active ? 700 : 400,
    color: active ? 'var(--accent, #6366f1)' : '#6b7280',
    fontSize: 14,
  })

  if (loading) return <div style={{ padding: 32 }}>{t('seo.loading')}</div>

  return (
    <div style={{ padding: '32px 24px', maxWidth: 860, margin: '0 auto' }}>
      <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 24, fontWeight: 700, marginBottom: 24 }}>
        {t('seo.pageTitle')}
      </h1>

      {/* Tab bar */}
      <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: 24 }}>
        {(['global', 'products', 'indexation', 'tools'] as Tab[]).map(key => (
          <button key={key} style={tabStyle(tab === key)} onClick={() => setTab(key)}>
            {t(`seo.tab.${key}`)}
          </button>
        ))}
      </div>

      {/* --- Global SEO tab --- */}
      {tab === 'global' && config && (
        <Card>
          <div style={{ padding: 24 }}>
            <Field label={t('seo.title')}>
              <input
                style={inputStyle}
                value={config.seoTitle ?? ''}
                onChange={e => setConfig({ ...config, seoTitle: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.description')}>
              <textarea
                style={{ ...inputStyle, minHeight: 80, resize: 'vertical' }}
                value={config.seoDescription ?? ''}
                onChange={e => setConfig({ ...config, seoDescription: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.keywords')}>
              <input
                style={inputStyle}
                value={config.seoKeywords ?? ''}
                onChange={e => setConfig({ ...config, seoKeywords: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.ogImage')}>
              <input
                style={inputStyle}
                value={config.ogImageUrl ?? ''}
                onChange={e => setConfig({ ...config, ogImageUrl: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.canonical')}>
              <input
                style={inputStyle}
                value={config.canonicalUrl ?? ''}
                onChange={e => setConfig({ ...config, canonicalUrl: e.target.value || null })}
              />
            </Field>
            {feedback === 'saved' && (
              <p style={{ color: 'green', fontSize: 13, marginBottom: 8 }}>{t('seo.saved')}</p>
            )}
            {feedback === 'error' && (
              <p style={{ color: 'red', fontSize: 13, marginBottom: 8 }}>{t('seo.saveError')}</p>
            )}
            <Button onClick={handleSaveGlobal} disabled={saving}>
              {saving ? t('seo.saving') : t('seo.save')}
            </Button>
          </div>
        </Card>
      )}

      {/* --- Products tab --- */}
      {tab === 'products' && (
        <>
          {productFeedback && (
            <p style={{ color: 'green', fontSize: 13, marginBottom: 12 }}>{productFeedback}</p>
          )}
          {editingProduct ? (
            <Card>
              <div style={{ padding: 24 }}>
                <h2 style={{ fontSize: 17, fontWeight: 700, marginBottom: 16 }}>{editingProduct.name}</h2>
                <Field label={t('seo.title')}>
                  <input
                    style={inputStyle}
                    value={productOverride.seoTitle ?? ''}
                    onChange={e => setProductOverride({ ...productOverride, seoTitle: e.target.value || null })}
                  />
                </Field>
                <Field label={t('seo.description')}>
                  <textarea
                    style={{ ...inputStyle, minHeight: 80, resize: 'vertical' }}
                    value={productOverride.seoDescription ?? ''}
                    onChange={e => setProductOverride({ ...productOverride, seoDescription: e.target.value || null })}
                  />
                </Field>
                <Field label={t('seo.keywords')}>
                  <input
                    style={inputStyle}
                    value={productOverride.seoKeywords ?? ''}
                    onChange={e => setProductOverride({ ...productOverride, seoKeywords: e.target.value || null })}
                  />
                </Field>
                <Field label={t('seo.products.ogImage')}>
                  <input
                    style={inputStyle}
                    value={productOverride.ogImageUrl ?? ''}
                    onChange={e => setProductOverride({ ...productOverride, ogImageUrl: e.target.value || null })}
                  />
                </Field>
                <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                  <Button onClick={handleSaveProductSeo}>{t('seo.save')}</Button>
                  <Button onClick={() => setEditingProduct(null)}>{t('seo.tab.global')}</Button>
                </div>
              </div>
            </Card>
          ) : (
            <Card>
              <div style={{ padding: 24 }}>
                <input
                  style={{ ...inputStyle, marginBottom: 16 }}
                  placeholder={t('seo.products.search')}
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                />
                {productsLoading ? (
                  <p>{t('seo.loading')}</p>
                ) : (
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid #e5e7eb', textAlign: 'left' }}>
                        <th style={{ padding: '8px 0' }}>{t('seo.products.name')}</th>
                        <th style={{ padding: '8px 0' }}>{t('seo.products.status')}</th>
                        <th style={{ padding: '8px 0' }} />
                      </tr>
                    </thead>
                    <tbody>
                      {filteredProducts.map(p => {
                        const hasOverride = productOverrides[p.id] !== undefined
                          ? productOverrides[p.id] !== null
                          : false
                        return (
                          <tr key={p.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                            <td style={{ padding: '10px 0' }}>{p.name}</td>
                            <td style={{ padding: '10px 0', color: hasOverride ? 'green' : '#9ca3af' }}>
                              {hasOverride ? t('seo.products.customized') : t('seo.products.default')}
                            </td>
                            <td style={{ padding: '10px 0', textAlign: 'right' }}>
                              <button
                                onClick={() => openEditProduct(p)}
                                style={{ marginRight: 8, cursor: 'pointer', background: 'none', border: 'none', color: 'var(--accent, #6366f1)', fontWeight: 600 }}
                              >
                                {t('seo.products.edit')}
                              </button>
                              {hasOverride && (
                                <button
                                  onClick={() => handleResetProductSeo(p.id)}
                                  style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#ef4444', fontWeight: 600 }}
                                >
                                  {t('seo.products.reset')}
                                </button>
                              )}
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            </Card>
          )}
        </>
      )}

      {/* --- Indexation tab --- */}
      {tab === 'indexation' && config && (
        <Card>
          <div style={{ padding: 24 }}>
            <Field label={t('seo.indexProducts')}>
              <input
                type="checkbox"
                checked={config.indexProducts}
                onChange={e => setConfig({ ...config, indexProducts: e.target.checked })}
              />
            </Field>
            <Field label={t('seo.indexCatalog')}>
              <input
                type="checkbox"
                checked={config.indexCatalog}
                onChange={e => setConfig({ ...config, indexCatalog: e.target.checked })}
              />
            </Field>
            <Field label={t('seo.robotsDisallow')}>
              <textarea
                style={{ ...inputStyle, minHeight: 120, resize: 'vertical', fontFamily: 'monospace' }}
                value={config.robotsDisallowPaths ?? ''}
                onChange={e => setConfig({ ...config, robotsDisallowPaths: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.sitemapFreq')}>
              <select
                style={inputStyle}
                value={config.sitemapChangefreq}
                onChange={e => setConfig({ ...config, sitemapChangefreq: e.target.value })}
              >
                {['always', 'hourly', 'daily', 'weekly', 'monthly', 'yearly', 'never'].map(f => (
                  <option key={f} value={f}>{f}</option>
                ))}
              </select>
            </Field>
            {feedback === 'saved' && (
              <p style={{ color: 'green', fontSize: 13, marginBottom: 8 }}>{t('seo.saved')}</p>
            )}
            {feedback === 'error' && (
              <p style={{ color: 'red', fontSize: 13, marginBottom: 8 }}>{t('seo.saveError')}</p>
            )}
            <Button onClick={handleSaveGlobal} disabled={saving}>
              {saving ? t('seo.saving') : t('seo.save')}
            </Button>
          </div>
        </Card>
      )}

      {/* --- Third-party tools tab --- */}
      {tab === 'tools' && config && (
        <Card>
          <div style={{ padding: 24 }}>
            <Field label={t('seo.googleVerification')}>
              <input
                style={inputStyle}
                value={config.googleVerification ?? ''}
                onChange={e => setConfig({ ...config, googleVerification: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.ga4Id')}>
              <input
                style={inputStyle}
                placeholder="G-XXXXXXXXXX"
                value={config.ga4Id ?? ''}
                onChange={e => setConfig({ ...config, ga4Id: e.target.value || null })}
              />
            </Field>
            <Field label={t('seo.bingVerification')}>
              <input
                style={inputStyle}
                value={config.bingVerification ?? ''}
                onChange={e => setConfig({ ...config, bingVerification: e.target.value || null })}
              />
            </Field>
            {feedback === 'saved' && (
              <p style={{ color: 'green', fontSize: 13, marginBottom: 8 }}>{t('seo.saved')}</p>
            )}
            {feedback === 'error' && (
              <p style={{ color: 'red', fontSize: 13, marginBottom: 8 }}>{t('seo.saveError')}</p>
            )}
            <Button onClick={handleSaveGlobal} disabled={saving}>
              {saving ? t('seo.saving') : t('seo.save')}
            </Button>
          </div>
        </Card>
      )}
    </div>
  )
}
