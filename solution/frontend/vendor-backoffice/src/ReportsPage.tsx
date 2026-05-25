import { useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { getSalesReport, exportSalesCsv, type SalesReportResponse } from './api/reportApi'

function currentPeriod(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(value)
}

function formatPercent(value: number): string {
  return new Intl.NumberFormat('fr-FR', {
    style: 'percent',
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  }).format(value / 100)
}

export default function ReportsPage() {
  const { t } = useTranslation()
  const [period, setPeriod] = useState(currentPeriod())
  const [category, setCategory] = useState('')
  const [report, setReport] = useState<SalesReportResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)

  async function handleApply() {
    setLoading(true)
    setError(null)
    try {
      const data = await getSalesReport(period, category || undefined)
      setReport(data)
    } catch {
      setError(t('reports.error.load'))
    } finally {
      setLoading(false)
    }
  }

  async function handleExport() {
    setExporting(true)
    try {
      await exportSalesCsv(period, category || undefined)
    } catch {
      setError(t('reports.error.export'))
    } finally {
      setExporting(false)
    }
  }

  return (
    <div style={{ padding: '2rem', maxWidth: 900, margin: '0 auto' }}>
      <h1 style={{ marginBottom: '1.5rem' }}>{t('reports.title')}</h1>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: 4 }}>
            {t('reports.period')}
          </label>
          <input
            type="month"
            value={period}
            onChange={e => setPeriod(e.target.value)}
            style={{ padding: '0.4rem 0.6rem', borderRadius: 4, border: '1px solid #ccc' }}
          />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: 4 }}>
            {t('reports.category')}
          </label>
          <input
            type="text"
            value={category}
            onChange={e => setCategory(e.target.value)}
            placeholder={t('reports.categoryPlaceholder')}
            style={{ padding: '0.4rem 0.6rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 160 }}
          />
        </div>
        <button
          onClick={handleApply}
          disabled={loading}
          style={{
            padding: '0.45rem 1.2rem', borderRadius: 4, border: 'none',
            background: '#1a73e8', color: '#fff', cursor: 'pointer', fontWeight: 500,
          }}
        >
          {loading ? t('reports.loading') : t('reports.apply')}
        </button>
      </div>

      {error && (
        <div style={{ color: '#c62828', background: '#ffebee', padding: '0.75rem 1rem', borderRadius: 4, marginBottom: '1rem' }}>
          {error}
        </div>
      )}

      {report && (
        <>
          {/* KPI cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginBottom: '2rem' }}>
            <KpiCard label={t('reports.kpi.revenue')} value={formatCurrency(report.metrics.revenue)} />
            <KpiCard label={t('reports.kpi.orderCount')} value={String(report.metrics.orderCount)} />
            <KpiCard label={t('reports.kpi.avgCart')} value={formatCurrency(report.metrics.averageCartValue)} />
            <KpiCard label={t('reports.kpi.cancellationRate')} value={formatPercent(report.metrics.cancellationRate)} />
          </div>

          {/* Top products */}
          <h2 style={{ marginBottom: '0.75rem' }}>{t('reports.topProducts.title')}</h2>
          {report.topSellingProducts.length === 0 ? (
            <p style={{ color: '#666' }}>{t('reports.topProducts.empty')}</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '1.5rem' }}>
              <thead>
                <tr style={{ background: '#f5f5f5' }}>
                  <Th>{t('reports.topProducts.rank')}</Th>
                  <Th>{t('reports.topProducts.product')}</Th>
                  <Th align="right">{t('reports.topProducts.qtySold')}</Th>
                  <Th align="right">{t('reports.topProducts.revenue')}</Th>
                </tr>
              </thead>
              <tbody>
                {report.topSellingProducts.map(p => (
                  <tr key={p.rank} style={{ borderBottom: '1px solid #eee' }}>
                    <Td>{p.rank}</Td>
                    <Td>{p.productName}</Td>
                    <Td align="right">{p.quantitySold}</Td>
                    <Td align="right">{formatCurrency(p.revenueGenerated)}</Td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {/* Export */}
          <button
            onClick={handleExport}
            disabled={exporting}
            style={{
              padding: '0.45rem 1.2rem', borderRadius: 4, border: '1px solid #1a73e8',
              background: '#fff', color: '#1a73e8', cursor: 'pointer', fontWeight: 500,
            }}
          >
            {exporting ? t('reports.exporting') : t('reports.exportCsv')}
          </button>
        </>
      )}
    </div>
  )
}

function KpiCard({ label, value }: { label: string; value: string }) {
  return (
    <div style={{
      border: '1px solid #e0e0e0', borderRadius: 8, padding: '1rem',
      textAlign: 'center', background: '#fff',
    }}>
      <div style={{ fontSize: '0.8rem', color: '#666', marginBottom: 8 }}>{label}</div>
      <div style={{ fontSize: '1.4rem', fontWeight: 700 }}>{value}</div>
    </div>
  )
}

function Th({ children, align }: { children: ReactNode; align?: 'right' }) {
  return (
    <th style={{ padding: '0.5rem 0.75rem', textAlign: align ?? 'left', fontWeight: 600, fontSize: '0.85rem' }}>
      {children}
    </th>
  )
}

function Td({ children, align }: { children: ReactNode; align?: 'right' }) {
  return (
    <td style={{ padding: '0.5rem 0.75rem', textAlign: align ?? 'left' }}>
      {children}
    </td>
  )
}
