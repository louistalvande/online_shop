import { useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { getSalesReport, exportSalesCsv, exportMailingListCsv, type SalesReportResponse } from './api/reportApi'

function today(): string {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonth(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
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
  const [startDate, setStartDate] = useState(firstDayOfMonth())
  const [endDate, setEndDate] = useState(today())
  const [category, setCategory] = useState('')
  const [report, setReport] = useState<SalesReportResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)
  const [exportingMailing, setExportingMailing] = useState(false)
  const [mailingError, setMailingError] = useState<string | null>(null)

  async function handleApply() {
    setLoading(true)
    setError(null)
    try {
      const data = await getSalesReport(startDate, endDate, category || undefined)
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
      await exportSalesCsv(startDate, endDate, category || undefined)
    } catch {
      setError(t('reports.error.export'))
    } finally {
      setExporting(false)
    }
  }

  async function handleExportMailing() {
    setExportingMailing(true)
    setMailingError(null)
    try {
      await exportMailingListCsv()
    } catch {
      setMailingError(t('reports.mailing.error'))
    } finally {
      setExportingMailing(false)
    }
  }

  return (
    <div style={{ padding: '2rem', maxWidth: 900, margin: '0 auto' }}>
      <h1 style={{ marginBottom: '1.5rem' }}>{t('reports.title')}</h1>

      {/* Mailing list export — US-PRF-05 */}
      <section style={{
        border: '1px solid #e0e0e0', borderRadius: 8, padding: '1.25rem 1.5rem',
        marginBottom: '2rem', background: '#fff',
      }}>
        <h2 style={{ margin: '0 0 0.5rem', fontSize: '1rem', fontWeight: 600 }}>
          {t('reports.mailing.title')}
        </h2>
        <p style={{ margin: '0 0 1rem', color: '#555', fontSize: '0.9rem' }}>
          {t('reports.mailing.description')}
        </p>
        {mailingError && (
          <div style={{ color: '#c62828', background: '#ffebee', padding: '0.6rem 0.9rem', borderRadius: 4, marginBottom: '0.75rem', fontSize: '0.9rem' }}>
            {mailingError}
          </div>
        )}
        <button
          className="btn btn-primary btn-sm"
          onClick={handleExportMailing}
          disabled={exportingMailing}
        >
          {exportingMailing ? t('reports.mailing.exporting') : t('reports.mailing.export')}
        </button>
      </section>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: 4 }}>
            {t('reports.startDate')}
          </label>
          <input
            type="date"
            value={startDate}
            onChange={e => setStartDate(e.target.value)}
            style={{ padding: '0.4rem 0.6rem', borderRadius: 4, border: '1px solid #ccc' }}
          />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: 4 }}>
            {t('reports.endDate')}
          </label>
          <input
            type="date"
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
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
