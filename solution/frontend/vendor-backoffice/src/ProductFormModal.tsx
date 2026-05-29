import { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { createProduct, updateProduct, uploadProductImage, fetchDistinctTypes, fetchDistinctThemes, type Product, type CreateProductPayload } from './api/productApi'

interface Props {
  product?: Product | null
  onSave: (p: Product) => void
  onClose: () => void
}

interface FormState {
  name: string
  description: string
  priceExclTax: string
  category: string
  theme: string
  quantity: string
  stockAlertThreshold: string
  photoUrls: string[]
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
    photoUrls: [...p.photoUrls],
  }
}

const EMPTY: FormState = {
  name: '', description: '', priceExclTax: '', category: '',
  theme: '', quantity: '', stockAlertThreshold: '0', photoUrls: [],
}

/** Modal form for creating (US-CAT-01) and editing (US-CAT-02) a product, with image upload (US-CAT-09). */
export default function ProductFormModal({ product, onSave, onClose }: Props) {
  const { t } = useTranslation()
  const [form, setForm] = useState<FormState>(product ? toForm(product) : EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [existingTypes, setExistingTypes] = useState<string[]>([])
  const [existingThemes, setExistingThemes] = useState<string[]>([])
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    fetchDistinctTypes().then(setExistingTypes).catch(() => {})
    fetchDistinctThemes().then(setExistingThemes).catch(() => {})
  }, [])

  useEffect(() => {
    setForm(product ? toForm(product) : EMPTY)
    setError(null)
  }, [product])

  function set<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm(prev => ({ ...prev, [key]: value }))
  }

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    e.target.value = ''
    setUploading(true)
    setError(null)
    try {
      const url = await uploadProductImage(file)
      set('photoUrls', [...form.photoUrls, url])
    } catch {
      setError(t('catalog.form.uploadError'))
    } finally {
      setUploading(false)
    }
  }

  function removePhoto(index: number) {
    set('photoUrls', form.photoUrls.filter((_, i) => i !== index))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    const price = parseFloat(form.priceExclTax)
    const qty = parseInt(form.quantity, 10)
    const threshold = parseInt(form.stockAlertThreshold, 10)

    if (!form.name.trim()) { setError(t('catalog.error.nameRequired')); return }
    if (isNaN(price) || price <= 0) { setError(t('catalog.error.priceInvalid')); return }
    if (isNaN(qty) || qty < 0) { setError(t('catalog.error.quantityInvalid')); return }

    const payload: CreateProductPayload = {
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      priceExclTax: price,
      category: form.category.trim() || undefined,
      theme: form.theme.trim() || undefined,
      quantity: qty,
      stockAlertThreshold: isNaN(threshold) ? 0 : threshold,
      photoUrls: form.photoUrls,
    }

    setSaving(true)
    try {
      const saved = product
        ? await updateProduct(product.id, payload)
        : await createProduct(payload)
      onSave(saved)
    } catch (err: any) {
      setError(err.message ?? t('catalog.error.generic'))
    } finally {
      setSaving(false)
    }
  }

  const overlayStyle: React.CSSProperties = {
    position: 'fixed', inset: 0,
    background: 'rgba(0,0,0,0.35)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 100,
  }
  const dialogStyle: React.CSSProperties = {
    background: 'var(--surface)',
    borderRadius: 8,
    padding: '32px 36px',
    width: 520,
    maxWidth: '95vw',
    maxHeight: '90vh',
    overflowY: 'auto',
    boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
  }
  const labelStyle: React.CSSProperties = {
    display: 'block', fontSize: 13, fontWeight: 600,
    color: 'var(--text-muted)', marginBottom: 4,
  }
  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 12px', border: '1px solid var(--border)',
    borderRadius: 6, fontSize: 14, boxSizing: 'border-box',
    background: 'var(--surface)', color: 'var(--text)',
  }
  const photoItemStyle: React.CSSProperties = {
    display: 'flex', alignItems: 'center', gap: 10,
    padding: '6px 8px',
    border: '1px solid var(--border)',
    borderRadius: 6,
    marginBottom: 6,
    background: 'var(--surface)',
  }

  return (
    <div style={overlayStyle} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div style={dialogStyle}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
          {product ? t('catalog.form.titleEdit') : t('catalog.form.titleCreate')}
        </h2>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label htmlFor="pf-name" style={labelStyle}>{t('catalog.form.name')} *</label>
            <input id="pf-name" style={inputStyle} value={form.name}
              onChange={e => set('name', e.target.value)} maxLength={200} />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label htmlFor="pf-description" style={labelStyle}>{t('catalog.form.description')}</label>
            <textarea id="pf-description" style={{ ...inputStyle, minHeight: 80, resize: 'vertical' }}
              value={form.description}
              onChange={e => set('description', e.target.value)} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label htmlFor="pf-price" style={labelStyle}>{t('catalog.form.priceExclTax')} *</label>
              <input id="pf-price" style={inputStyle} type="number" step="0.01" min="0.01"
                value={form.priceExclTax}
                onChange={e => set('priceExclTax', e.target.value)} />
            </div>
            <div>
              <label htmlFor="pf-category" style={labelStyle}>{t('catalog.form.category')}</label>
              <input id="pf-category" style={inputStyle} list="pf-category-list"
                value={form.category} onChange={e => set('category', e.target.value)} maxLength={100} />
              <datalist id="pf-category-list">
                {existingTypes.map(v => <option key={v} value={v} />)}
              </datalist>
            </div>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label htmlFor="pf-theme" style={labelStyle}>{t('catalog.form.theme')}</label>
            <input id="pf-theme" style={inputStyle} list="pf-theme-list"
              value={form.theme} onChange={e => set('theme', e.target.value)} maxLength={100} />
            <datalist id="pf-theme-list">
              {existingThemes.map(th => <option key={th} value={th} />)}
            </datalist>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label htmlFor="pf-quantity" style={labelStyle}>{t('catalog.form.quantity')} *</label>
              <input id="pf-quantity" style={inputStyle} type="number" min="0"
                value={form.quantity}
                onChange={e => set('quantity', e.target.value)} />
            </div>
            <div>
              <label htmlFor="pf-threshold" style={labelStyle}>{t('catalog.form.stockAlertThreshold')}</label>
              <input id="pf-threshold" style={inputStyle} type="number" min="0"
                value={form.stockAlertThreshold}
                onChange={e => set('stockAlertThreshold', e.target.value)} />
            </div>
          </div>

          <div style={{ marginBottom: 24 }}>
            <label style={labelStyle}>{t('catalog.form.photos')}</label>

            {form.photoUrls.length > 0 && (
              <div style={{ marginBottom: 8 }}>
                {form.photoUrls.map((url, i) => (
                  <div key={i} style={photoItemStyle}>
                    <img
                      src={url}
                      alt=""
                      style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 4, flexShrink: 0 }}
                      onError={e => { (e.target as HTMLImageElement).style.display = 'none' }}
                    />
                    <span style={{ flex: 1, fontSize: 11, color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {url}
                    </span>
                    <button
                      type="button"
                      onClick={() => removePhoto(i)}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#c0392b', fontSize: 16, padding: '0 4px', flexShrink: 0 }}
                      aria-label={t('catalog.form.removePhoto')}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}

            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              style={{ display: 'none' }}
              onChange={handleFileChange}
            />
            <Button
              type="button"
              variant="secondary"
              disabled={uploading || saving}
              onClick={() => fileInputRef.current?.click()}
            >
              {uploading ? t('catalog.form.uploading') : t('catalog.form.addPhoto')}
            </Button>
            {form.photoUrls.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 6 }}>
                {t('catalog.form.noPhotos')}
              </p>
            )}
          </div>

          {error && (
            <p style={{ color: '#c0392b', fontSize: 13, marginBottom: 16 }}>{error}</p>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
            <Button type="button" variant="secondary" onClick={onClose} disabled={saving}>
              {t('catalog.form.cancel')}
            </Button>
            <Button type="submit" variant="primary" disabled={saving || uploading}>
              {saving ? t('catalog.form.saving') : t('catalog.form.save')}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
