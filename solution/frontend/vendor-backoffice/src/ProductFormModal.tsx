import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { createProduct, updateProduct, type Product, type CreateProductPayload } from './api/productApi'

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
    quantity: String(p.quantity),
    stockAlertThreshold: String(p.stockAlertThreshold),
    photoUrls: p.photoUrls.join('\n'),
  }
}

const EMPTY: FormState = {
  name: '', description: '', priceExclTax: '', category: '',
  quantity: '', stockAlertThreshold: '0', photoUrls: '',
}

/** Modal form for creating (US-CAT-01) and editing (US-CAT-02) a product. */
export default function ProductFormModal({ product, onSave, onClose }: Props) {
  const { t } = useTranslation()
  const [form, setForm] = useState<FormState>(product ? toForm(product) : EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    setForm(product ? toForm(product) : EMPTY)
    setError(null)
  }, [product])

  function set(key: keyof FormState, value: string) {
    setForm(prev => ({ ...prev, [key]: value }))
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
      quantity: qty,
      stockAlertThreshold: isNaN(threshold) ? 0 : threshold,
      photoUrls: form.photoUrls.split('\n').map(u => u.trim()).filter(Boolean),
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

  return (
    <div style={overlayStyle} onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div style={dialogStyle}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
          {product ? t('catalog.form.titleEdit') : t('catalog.form.titleCreate')}
        </h2>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label style={labelStyle}>{t('catalog.form.name')} *</label>
            <input style={inputStyle} value={form.name}
              onChange={e => set('name', e.target.value)} maxLength={200} />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={labelStyle}>{t('catalog.form.description')}</label>
            <textarea style={{ ...inputStyle, minHeight: 80, resize: 'vertical' }}
              value={form.description}
              onChange={e => set('description', e.target.value)} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label style={labelStyle}>{t('catalog.form.priceExclTax')} *</label>
              <input style={inputStyle} type="number" step="0.01" min="0.01"
                value={form.priceExclTax}
                onChange={e => set('priceExclTax', e.target.value)} />
            </div>
            <div>
              <label style={labelStyle}>{t('catalog.form.category')}</label>
              <input style={inputStyle} value={form.category}
                onChange={e => set('category', e.target.value)} maxLength={100} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label style={labelStyle}>{t('catalog.form.quantity')} *</label>
              <input style={inputStyle} type="number" min="0"
                value={form.quantity}
                onChange={e => set('quantity', e.target.value)} />
            </div>
            <div>
              <label style={labelStyle}>{t('catalog.form.stockAlertThreshold')}</label>
              <input style={inputStyle} type="number" min="0"
                value={form.stockAlertThreshold}
                onChange={e => set('stockAlertThreshold', e.target.value)} />
            </div>
          </div>

          <div style={{ marginBottom: 24 }}>
            <label style={labelStyle}>{t('catalog.form.photoUrls')}</label>
            <textarea style={{ ...inputStyle, minHeight: 64, resize: 'vertical', fontSize: 12 }}
              placeholder={t('catalog.form.photoUrlsPlaceholder')}
              value={form.photoUrls}
              onChange={e => set('photoUrls', e.target.value)} />
          </div>

          {error && (
            <p style={{ color: '#c0392b', fontSize: 13, marginBottom: 16 }}>{error}</p>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
            <Button type="button" variant="secondary" onClick={onClose} disabled={saving}>
              {t('catalog.form.cancel')}
            </Button>
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? t('catalog.form.saving') : t('catalog.form.save')}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
