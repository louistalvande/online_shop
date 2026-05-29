import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, LangToggle } from '@workspace/theme'
import { getProduct, updateProduct, archiveProduct, fetchDistinctTypes, fetchDistinctThemes, type Product, type CreateProductPayload } from './api/productApi'
import { getSession, logout } from './api/authApi'

interface FormState {
  name: string
  description: string
  priceExclTax: string
  category: string
  theme: string
  quantity: string
  stockAlertThreshold: string
  photoUrls: string
}

function toForm(p: Product): FormState {
  return {
    name: p.name,
    description: p.description ?? '',
    priceExclTax: String(p.priceExclTax),
    category: p.category ?? '',
    theme: p.theme ?? '',
    quantity: String(p.quantity),
    stockAlertThreshold: String(p.stockAlertThreshold),
    photoUrls: p.photoUrls.join('\n'),
  }
}

interface Props {
  productId: string
}

/** Vendor product detail and edit page — US-CAT-02. */
export default function VendorProductDetailPage({ productId }: Props) {
  const { t, i18n } = useTranslation()
  const session = getSession()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [form, setForm] = useState<FormState | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState(false)
  const [archiving, setArchiving] = useState(false)
  const [currentPhoto, setCurrentPhoto] = useState(0)
  const [existingTypes, setExistingTypes] = useState<string[]>([])
  const [existingThemes, setExistingThemes] = useState<string[]>([])

  useEffect(() => {
    fetchDistinctTypes().then(setExistingTypes).catch(() => {})
    fetchDistinctThemes().then(setExistingThemes).catch(() => {})
  }, [])

  useEffect(() => {
    if (!session) return
    getProduct(productId)
      .then(p => { setProduct(p); setForm(toForm(p)) })
      .catch(() => setLoadError(t('catalog.error.load')))
      .finally(() => setLoading(false))
  }, [productId])

  function setField(key: keyof FormState, value: string) {
    setForm(prev => prev ? { ...prev, [key]: value } : prev)
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault()
    if (!form) return
    setFormError(null)
    setSaveSuccess(false)

    const price = parseFloat(form.priceExclTax)
    const qty = parseInt(form.quantity, 10)
    const threshold = parseInt(form.stockAlertThreshold, 10)

    if (!form.name.trim()) { setFormError(t('catalog.error.nameRequired')); return }
    if (isNaN(price) || price <= 0) { setFormError(t('catalog.error.priceInvalid')); return }
    if (isNaN(qty) || qty < 0) { setFormError(t('catalog.error.quantityInvalid')); return }

    const payload: CreateProductPayload = {
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      priceExclTax: price,
      category: form.category.trim() || undefined,
      theme: form.theme.trim() || undefined,
      quantity: qty,
      stockAlertThreshold: isNaN(threshold) ? 0 : threshold,
      photoUrls: form.photoUrls.split('\n').map(u => u.trim()).filter(Boolean),
    }

    setSaving(true)
    try {
      const updated = await updateProduct(productId, payload)
      setProduct(updated)
      setForm(toForm(updated))
      setSaveSuccess(true)
      setTimeout(() => setSaveSuccess(false), 3000)
    } catch (err: any) {
      setFormError(err.message ?? t('catalog.error.generic'))
    } finally {
      setSaving(false)
    }
  }

  async function handleArchive() {
    if (!product) return
    if (!window.confirm(t('catalog.archiveConfirm', { name: product.name }))) return
    setArchiving(true)
    try {
      const updated = await archiveProduct(productId)
      setProduct(updated)
    } catch (err: any) {
      alert(err.message ?? t('catalog.error.generic'))
    } finally {
      setArchiving(false)
    }
  }

  const shellActions = (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
      <LangToggle lang={i18n.language} onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')} />
      <Button variant="ghost" size="sm" onClick={() => { logout(); window.location.href = import.meta.env.BASE_URL }}>
        {t('nav.logout')}
      </Button>
    </div>
  )

  const labelStyle: React.CSSProperties = { display: 'block', fontSize: 13, fontWeight: 600, color: 'var(--text-muted)', marginBottom: 4 }
  const inputStyle: React.CSSProperties = { width: '100%', padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 14, boxSizing: 'border-box', background: 'var(--surface)', color: 'var(--text)' }

  if (!session) {
    return (
      <AppShell appName={t('app.name')} navLinks={[{ label: t('catalog.title'), href: `${import.meta.env.BASE_URL}catalog` }]} actions={shellActions}>
        <main style={{ padding: '2rem' }}><p>{t('orders.error.notAuthenticated')}</p></main>
      </AppShell>
    )
  }

  const photoList = form?.photoUrls.split('\n').map(u => u.trim()).filter(Boolean) ?? []

  return (
    <AppShell appName={t('app.name')} navLinks={[{ label: t('catalog.title'), href: `${import.meta.env.BASE_URL}catalog` }]} actions={shellActions}>
      <div style={{ maxWidth: 960, margin: '0 auto', padding: '32px 24px 64px' }}>
        <a href={`${import.meta.env.BASE_URL}catalog`} style={{ fontSize: 14, color: 'var(--accent)', textDecoration: 'none' }}>
          ← {t('catalog.title')}
        </a>

        {loading && <p style={{ marginTop: 24, color: 'var(--text-muted)' }}>{t('catalog.loading')}</p>}
        {loadError && <p style={{ marginTop: 24, color: '#c0392b' }}>{loadError}</p>}

        {product && form && (
          <>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 24, marginBottom: 32 }}>
              <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 26, fontWeight: 700, margin: 0 }}>
                {product.name}
              </h1>
              {product.status === 'PUBLISHED' && (
                <Button variant="secondary" size="sm" disabled={archiving} onClick={handleArchive}>
                  {archiving ? '…' : t('catalog.action.archive')}
                </Button>
              )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr', gap: 48, alignItems: 'start' }}>
              {/* Photo preview */}
              <div>
                {photoList.length > 0 ? (
                  <>
                    <img
                      src={photoList[currentPhoto]}
                      alt={product.name}
                      style={{ width: '100%', aspectRatio: '1', objectFit: 'cover', borderRadius: 8, background: 'var(--surface)' }}
                    />
                    {photoList.length > 1 && (
                      <div style={{ display: 'flex', gap: 6, marginTop: 10, flexWrap: 'wrap' }}>
                        {photoList.map((url, i) => (
                          <img
                            key={i}
                            src={url}
                            alt=""
                            onClick={() => setCurrentPhoto(i)}
                            style={{
                              width: 52, height: 52, objectFit: 'cover', borderRadius: 4, cursor: 'pointer',
                              border: i === currentPhoto ? '2px solid var(--accent)' : '2px solid transparent',
                            }}
                          />
                        ))}
                      </div>
                    )}
                  </>
                ) : (
                  <div style={{ width: '100%', aspectRatio: '1', background: 'var(--surface)', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: 13 }}>
                    {t('catalog.form.photoUrls')}
                  </div>
                )}
                <div style={{ marginTop: 16, fontSize: 13 }}>
                  <span style={{
                    padding: '3px 10px', borderRadius: 12, fontWeight: 600, fontSize: 12,
                    background: product.status === 'PUBLISHED' ? '#e8f5e9' : '#f0f0f0',
                    color: product.status === 'PUBLISHED' ? '#1a7a2e' : '#888',
                  }}>
                    {t(`catalog.status.${product.status.toLowerCase()}`)}
                  </span>
                </div>
              </div>

              {/* Edit form */}
              <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                <div>
                  <label htmlFor="pd-name" style={labelStyle}>{t('catalog.form.name')} *</label>
                  <input id="pd-name" style={inputStyle} value={form.name} maxLength={200}
                    onChange={e => setField('name', e.target.value)} />
                </div>

                <div>
                  <label htmlFor="pd-description" style={labelStyle}>{t('catalog.form.description')}</label>
                  <textarea id="pd-description" style={{ ...inputStyle, minHeight: 80, resize: 'vertical' }}
                    value={form.description}
                    onChange={e => setField('description', e.target.value)} />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                  <div>
                    <label htmlFor="pd-price" style={labelStyle}>{t('catalog.form.priceExclTax')} *</label>
                    <input id="pd-price" style={inputStyle} type="number" step="0.01" min="0.01"
                      value={form.priceExclTax}
                      onChange={e => setField('priceExclTax', e.target.value)} />
                  </div>
                  <div>
                    <label htmlFor="pd-category" style={labelStyle}>{t('catalog.form.category')}</label>
                    <input id="pd-category" style={inputStyle} list="pd-category-list"
                      value={form.category} maxLength={100}
                      onChange={e => setField('category', e.target.value)} />
                    <datalist id="pd-category-list">
                      {existingTypes.map(v => <option key={v} value={v} />)}
                    </datalist>
                  </div>
                </div>

                <div>
                  <label htmlFor="pd-theme" style={labelStyle}>{t('catalog.form.theme')}</label>
                  <input id="pd-theme" style={inputStyle} list="pd-theme-list"
                    value={form.theme} maxLength={100}
                    onChange={e => setField('theme', e.target.value)} />
                  <datalist id="pd-theme-list">
                    {existingThemes.map(v => <option key={v} value={v} />)}
                  </datalist>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                  <div>
                    <label htmlFor="pd-quantity" style={labelStyle}>{t('catalog.form.quantity')} *</label>
                    <input id="pd-quantity" style={inputStyle} type="number" min="0"
                      value={form.quantity}
                      onChange={e => setField('quantity', e.target.value)} />
                  </div>
                  <div>
                    <label htmlFor="pd-threshold" style={labelStyle}>{t('catalog.form.stockAlertThreshold')}</label>
                    <input id="pd-threshold" style={inputStyle} type="number" min="0"
                      value={form.stockAlertThreshold}
                      onChange={e => setField('stockAlertThreshold', e.target.value)} />
                  </div>
                </div>

                <div>
                  <label htmlFor="pd-photos" style={labelStyle}>{t('catalog.form.photoUrls')}</label>
                  <textarea id="pd-photos" style={{ ...inputStyle, minHeight: 64, resize: 'vertical', fontSize: 12 }}
                    placeholder={t('catalog.form.photoUrlsPlaceholder')}
                    value={form.photoUrls}
                    onChange={e => { setField('photoUrls', e.target.value); setCurrentPhoto(0) }} />
                </div>

                {formError && <p style={{ color: '#c0392b', fontSize: 13, margin: 0 }}>{formError}</p>}
                {saveSuccess && (
                  <p style={{ color: '#1a7a2e', fontSize: 13, margin: 0 }}>{t('catalog.form.saveSuccess')}</p>
                )}

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
                  <Button type="button" variant="secondary"
                    onClick={() => { window.location.href = `${import.meta.env.BASE_URL}catalog` }}>
                    {t('catalog.form.cancel')}
                  </Button>
                  <Button type="submit" variant="primary" disabled={saving}>
                    {saving ? t('catalog.form.saving') : t('catalog.form.save')}
                  </Button>
                </div>
              </form>
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
