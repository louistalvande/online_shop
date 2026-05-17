import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import {
  listProducts, archiveProduct, listPendingAlerts, acknowledgeAlert,
  type Product, type StockAlert,
} from './api/productApi'
import ProductFormModal from './ProductFormModal'

/** Stock status indicator for a product row (US-CAT-04). */
function StockBadge({ product }: { product: Product }) {
  const { t } = useTranslation()
  if (product.outOfStock) {
    return (
      <span style={{ background: '#fde8e8', color: '#c0392b', padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600 }}>
        {t('catalog.stock.outOfStock')}
      </span>
    )
  }
  if (product.belowThreshold) {
    return (
      <span style={{ background: '#fef3e2', color: '#b8431a', padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600 }}>
        {t('catalog.stock.low')}
      </span>
    )
  }
  return (
    <span style={{ background: '#e8f5e9', color: '#1a7a2e', padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600 }}>
      {t('catalog.stock.ok')}
    </span>
  )
}

/** Vendor catalog management page (US-CAT-01 to US-CAT-05). */
export default function CatalogPage() {
  const { t } = useTranslation()
  const [products, setProducts] = useState<Product[]>([])
  const [alerts, setAlerts] = useState<StockAlert[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [modalProduct, setModalProduct] = useState<Product | null | undefined>(undefined)
  const [archivingId, setArchivingId] = useState<string | null>(null)
  const [showArchived, setShowArchived] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [prods, alrts] = await Promise.all([listProducts(), listPendingAlerts()])
      setProducts(prods)
      setAlerts(alrts)
    } catch {
      setError(t('catalog.error.load'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => { load() }, [load])

  async function handleArchive(product: Product) {
    if (!window.confirm(t('catalog.archiveConfirm', { name: product.name }))) return
    setArchivingId(product.id)
    try {
      const updated = await archiveProduct(product.id)
      setProducts(prev => prev.map(p => p.id === updated.id ? updated : p))
    } catch (err: any) {
      alert(err.message ?? t('catalog.error.generic'))
    } finally {
      setArchivingId(null)
    }
  }

  async function handleAcknowledge(alertId: string) {
    try {
      await acknowledgeAlert(alertId)
      setAlerts(prev => prev.filter(a => a.id !== alertId))
    } catch {
      // silently ignore
    }
  }

  function handleSaved(product: Product) {
    setProducts(prev => {
      const idx = prev.findIndex(p => p.id === product.id)
      return idx >= 0 ? prev.map(p => p.id === product.id ? product : p) : [product, ...prev]
    })
    setModalProduct(undefined)
    load()
  }

  const visibleProducts = showArchived ? products : products.filter(p => p.status === 'PUBLISHED')

  const tableHeaders = [
    t('catalog.col.name'), t('catalog.col.category'),
    t('catalog.col.price'), t('catalog.col.quantity'),
    t('catalog.col.threshold'), t('catalog.col.stock'), t('catalog.col.status'), '',
  ]

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px 64px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 32 }}>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700 }}>
          {t('catalog.title')}
        </h1>
        <Button variant="primary" onClick={() => setModalProduct(null)}>
          {t('catalog.addProduct')}
        </Button>
      </div>

      {/* Stock alerts panel (US-CAT-05) */}
      {alerts.length > 0 && (
        <Card style={{ marginBottom: 32, padding: '16px 20px', background: '#fef3e2', border: '1px solid #f0a500' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#b8431a', marginBottom: 12 }}>
            {t('catalog.alerts.title')} ({alerts.length})
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {alerts.map(a => (
              <div key={a.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 14 }}>
                <span>
                  <strong>{a.productName}</strong> — {t('catalog.alerts.body', { qty: a.quantity, threshold: a.stockAlertThreshold })}
                </span>
                <Button variant="ghost" size="sm" onClick={() => handleAcknowledge(a.id)}>
                  {t('catalog.alerts.acknowledge')}
                </Button>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Show/hide archived toggle */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
        <label style={{ fontSize: 13, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer' }}>
          <input type="checkbox" checked={showArchived} onChange={e => setShowArchived(e.target.checked)} />
          {t('catalog.showArchived')}
        </label>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>{t('catalog.loading')}</p>}
      {error && (
        <div>
          <p style={{ color: '#c0392b' }}>{error}</p>
          <Button variant="secondary" size="sm" onClick={load}>{t('catalog.retry')}</Button>
        </div>
      )}

      {!loading && !error && (
        <Card>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)' }}>
                {tableHeaders.map((h, i) => (
                  <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visibleProducts.length === 0 && (
                <tr>
                  <td colSpan={tableHeaders.length} style={{ padding: '24px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                    {t('catalog.empty')}
                  </td>
                </tr>
              )}
              {visibleProducts.map(p => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--border)', opacity: p.status === 'ARCHIVED' ? 0.6 : 1 }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{p.name}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{p.category ?? '—'}</td>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{p.priceExclTax.toFixed(2)} €</td>
                  <td style={{ padding: '14px 16px' }}>{p.quantity}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{p.stockAlertThreshold}</td>
                  <td style={{ padding: '14px 16px' }}><StockBadge product={p} /></td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      fontSize: 12, fontWeight: 600,
                      color: p.status === 'PUBLISHED' ? '#1a7a2e' : '#888',
                    }}>
                      {t(`catalog.status.${p.status.toLowerCase()}`)}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px', display: 'flex', gap: 8 }}>
                    <Button variant="ghost" size="sm" onClick={() => setModalProduct(p)}>
                      {t('catalog.action.edit')}
                    </Button>
                    {p.status === 'PUBLISHED' && (
                      <Button
                        variant="ghost" size="sm"
                        disabled={archivingId === p.id}
                        onClick={() => handleArchive(p)}
                      >
                        {archivingId === p.id ? '…' : t('catalog.action.archive')}
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* Create / edit modal */}
      {modalProduct !== undefined && (
        <ProductFormModal
          product={modalProduct}
          onSave={handleSaved}
          onClose={() => setModalProduct(undefined)}
        />
      )}
    </div>
  )
}
