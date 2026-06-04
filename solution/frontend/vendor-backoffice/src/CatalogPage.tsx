import { useState, useEffect, useCallback, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import {
  listProducts, archiveProduct, listPendingAlerts, acknowledgeAlert,
  exportProductsCsv, importProductsCsv, bulkUpdateStocks,
  type Product, type StockAlert, type CsvImportResponse,
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

/** Vendor catalog management page (US-CAT-01 to US-CAT-06). */
export default function CatalogPage() {
  const { t } = useTranslation()
  const [products, setProducts] = useState<Product[]>([])
  const [alerts, setAlerts] = useState<StockAlert[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [modalProduct, setModalProduct] = useState<Product | null | undefined>(undefined)
  const [archivingId, setArchivingId] = useState<string | null>(null)
  const [showArchived, setShowArchived] = useState(false)

  // CSV export state (US-CAT-07)
  const [exporting, setExporting] = useState(false)

  // Bulk stock edit state (US-CAT-08)
  const [stockEdits, setStockEdits] = useState<Record<string, { quantity: number; stockAlertThreshold: number }>>({})
  const [bulkSaving, setBulkSaving] = useState(false)
  const [bulkResult, setBulkResult] = useState<{ updated: number; errors: number } | null>(null)

  // CSV import state (US-CAT-06)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [csvImporting, setCsvImporting] = useState(false)
  const [csvResult, setCsvResult] = useState<CsvImportResponse | null>(null)
  const [csvError, setCsvError] = useState<string | null>(null)
  const [showCsvModal, setShowCsvModal] = useState(false)

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

  function handleStockEdit(productId: string, field: 'quantity' | 'stockAlertThreshold', raw: string) {
    const parsed = parseInt(raw, 10)
    if (isNaN(parsed) || parsed < 0) return
    const product = products.find(p => p.id === productId)
    if (!product) return
    const current = stockEdits[productId] ?? { quantity: product.quantity, stockAlertThreshold: product.stockAlertThreshold }
    const updated = { ...current, [field]: parsed }
    if (updated.quantity === product.quantity && updated.stockAlertThreshold === product.stockAlertThreshold) {
      setStockEdits(prev => { const n = { ...prev }; delete n[productId]; return n })
    } else {
      setStockEdits(prev => ({ ...prev, [productId]: updated }))
    }
  }

  async function handleBulkSave() {
    const updates = Object.entries(stockEdits).map(([productId, vals]) => ({
      productId,
      quantity: vals.quantity,
      stockAlertThreshold: vals.stockAlertThreshold,
    }))
    if (updates.length === 0) return
    setBulkSaving(true)
    setBulkResult(null)
    try {
      const res = await bulkUpdateStocks({ updates })
      setStockEdits({})
      setBulkResult({ updated: res.totalUpdated, errors: res.totalErrors })
      await load()
    } catch {
      alert(t('catalog.stock.saveError'))
    } finally {
      setBulkSaving(false)
    }
  }

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

  async function handleExport() {
    setExporting(true)
    try {
      await exportProductsCsv()
    } catch {
      alert(t('catalog.csv.exportError'))
    } finally {
      setExporting(false)
    }
  }

  function openCsvModal() {
    setCsvResult(null)
    setCsvError(null)
    setShowCsvModal(true)
  }

  function closeCsvModal() {
    setShowCsvModal(false)
    if (csvResult && csvResult.totalCreated > 0) load()
  }

  async function handleCsvFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setCsvImporting(true)
    setCsvError(null)
    setCsvResult(null)
    try {
      const result = await importProductsCsv(file)
      setCsvResult(result)
    } catch (err: any) {
      if (err.code === 'CSV_HEADER_INVALID') {
        setCsvError(t('catalog.csv.error.invalidHeader'))
      } else {
        setCsvError(t('catalog.csv.error.generic'))
      }
    } finally {
      setCsvImporting(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
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
        <div style={{ display: 'flex', gap: 12 }}>
          <Button variant="secondary" disabled={exporting} onClick={handleExport}>
            {exporting ? '…' : t('catalog.csv.exportButton')}
          </Button>
          <Button variant="secondary" onClick={openCsvModal}>
            {t('catalog.csv.importButton')}
          </Button>
          <Button variant="primary" onClick={() => setModalProduct(null)}>
            {t('catalog.addProduct')}
          </Button>
        </div>
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

      {/* Bulk stock save banner (US-CAT-08) */}
      {Object.keys(stockEdits).length > 0 && (
        <div style={{
          background: '#fff8e1', border: '1px solid #f0a500', borderRadius: 8,
          padding: '12px 20px', display: 'flex', alignItems: 'center',
          justifyContent: 'space-between', marginBottom: 16,
        }}>
          <span style={{ fontSize: 14, color: '#b8431a', fontWeight: 600 }}>
            {t('catalog.stock.pendingChanges', { count: Object.keys(stockEdits).length })}
          </span>
          <div style={{ display: 'flex', gap: 12 }}>
            <Button variant="ghost" size="sm" onClick={() => setStockEdits({})}>
              {t('catalog.stock.discardChanges')}
            </Button>
            <Button variant="primary" size="sm" disabled={bulkSaving} onClick={handleBulkSave}>
              {bulkSaving ? t('catalog.stock.saving') : t('catalog.stock.saveChanges')}
            </Button>
          </div>
        </div>
      )}

      {/* Bulk save result feedback */}
      {bulkResult && (
        <div style={{
          background: bulkResult.errors > 0 ? '#fef3e2' : '#e8f5e9',
          border: `1px solid ${bulkResult.errors > 0 ? '#f0a500' : '#2e7d32'}`,
          borderRadius: 8, padding: '10px 20px', marginBottom: 16, fontSize: 14,
          color: bulkResult.errors > 0 ? '#b8431a' : '#1a7a2e', fontWeight: 600,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        }}>
          <span>
            {bulkResult.errors > 0
              ? t('catalog.stock.savePartial', { updated: bulkResult.updated, errors: bulkResult.errors })
              : t('catalog.stock.saveSuccess', { updated: bulkResult.updated })}
          </span>
          <Button variant="ghost" size="sm" onClick={() => setBulkResult(null)}>✕</Button>
        </div>
      )}

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
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>
                    <a href={`${import.meta.env.BASE_URL}catalog/${p.id}`} style={{ color: 'inherit', textDecoration: 'underline' }}>
                      {p.name}
                    </a>
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{p.category ?? '—'}</td>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{p.priceExclTax.toFixed(2)} €</td>
                  <td style={{ padding: '8px 16px' }}>
                    <input
                      type="number"
                      min={0}
                      value={stockEdits[p.id]?.quantity ?? p.quantity}
                      aria-label={t('catalog.col.quantity')}
                      onChange={e => handleStockEdit(p.id, 'quantity', e.target.value)}
                      style={{
                        width: 64, padding: '4px 8px', fontSize: 14,
                        border: `1px solid ${stockEdits[p.id]?.quantity !== undefined ? '#f0a500' : 'var(--border)'}`,
                        borderRadius: 6,
                        background: stockEdits[p.id]?.quantity !== undefined ? '#fff8e1' : undefined,
                      }}
                    />
                  </td>
                  <td style={{ padding: '8px 16px' }}>
                    <input
                      type="number"
                      min={0}
                      value={stockEdits[p.id]?.stockAlertThreshold ?? p.stockAlertThreshold}
                      aria-label={t('catalog.col.threshold')}
                      onChange={e => handleStockEdit(p.id, 'stockAlertThreshold', e.target.value)}
                      style={{
                        width: 64, padding: '4px 8px', fontSize: 14,
                        border: `1px solid ${stockEdits[p.id]?.stockAlertThreshold !== undefined ? '#f0a500' : 'var(--border)'}`,
                        borderRadius: 6,
                        background: stockEdits[p.id]?.stockAlertThreshold !== undefined ? '#fff8e1' : undefined,
                      }}
                    />
                  </td>
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

      {/* CSV import modal (US-CAT-06) */}
      {showCsvModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200,
        }}>
          <div style={{
            background: '#fff', borderRadius: 12, padding: '32px 36px',
            minWidth: 500, maxWidth: 680, width: '90%', maxHeight: '80vh',
            overflowY: 'auto', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
          }}>
            <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 8 }}>
              {t('catalog.csv.modalTitle')}
            </h2>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 20 }}>
              {t('catalog.csv.instructions')}
            </p>

            {!csvResult && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".csv,text/csv"
                  disabled={csvImporting}
                  onChange={handleCsvFileChange}
                  style={{ display: 'none' }}
                  id="csv-file-input"
                />
                <label htmlFor="csv-file-input">
                  <Button
                    variant="secondary"
                    disabled={csvImporting}
                    onClick={() => fileInputRef.current?.click()}
                  >
                    {csvImporting ? t('catalog.csv.importing') : t('catalog.csv.selectFile')}
                  </Button>
                </label>
              </div>
            )}

            {csvError && (
              <p style={{ color: '#c0392b', fontSize: 14, marginBottom: 16 }}>{csvError}</p>
            )}

            {csvResult && (
              <div>
                <p style={{ fontWeight: 600, marginBottom: 12 }}>
                  {t('catalog.csv.resultTitle')}
                </p>
                <p style={{
                  fontSize: 14, marginBottom: 16,
                  color: csvResult.totalErrors > 0 ? '#b8431a' : '#1a7a2e',
                }}>
                  {t('catalog.csv.summary', {
                    created: csvResult.totalCreated,
                    updated: csvResult.totalUpdated,
                    errors: csvResult.totalErrors,
                  })}
                </p>
                <table data-testid="csv-result-table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border)' }}>
                      <th style={{ padding: '6px 10px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)' }}>Ligne</th>
                      <th style={{ padding: '6px 10px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)' }}>Statut</th>
                      <th style={{ padding: '6px 10px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)' }}>Détail</th>
                    </tr>
                  </thead>
                  <tbody>
                    {csvResult.rows.map((row, i) => (
                      <tr key={i} style={{ borderBottom: '1px solid var(--border)' }}>
                        <td style={{ padding: '6px 10px', color: 'var(--text-muted)' }}>{row.lineNumber}</td>
                        <td style={{ padding: '6px 10px' }}>
                          <span style={{
                            fontSize: 11, fontWeight: 700, padding: '2px 8px', borderRadius: 10,
                            background: row.status === 'CREATED' ? '#e8f5e9' : row.status === 'UPDATED' ? '#e3f0fb' : '#fde8e8',
                            color: row.status === 'CREATED' ? '#1a7a2e' : row.status === 'UPDATED' ? '#1a4a7a' : '#c0392b',
                          }}>
                            {row.status === 'CREATED'
                              ? t('catalog.csv.row.created')
                              : row.status === 'UPDATED'
                                ? t('catalog.csv.row.updated')
                                : t('catalog.csv.row.error')}
                          </span>
                        </td>
                        <td style={{ padding: '6px 10px', color: row.status === 'ERROR' ? '#c0392b' : undefined }}>
                          {row.status !== 'ERROR' ? (row.product?.name ?? '') : row.message}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 24 }}>
              <Button variant="ghost" onClick={closeCsvModal}>
                {csvResult ? t('catalog.csv.close') : t('catalog.csv.cancel')}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
