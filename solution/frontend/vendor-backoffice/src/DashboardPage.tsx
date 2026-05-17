import { useTranslation } from 'react-i18next'
import { Button, Card, PackageIcon } from '@workspace/theme'

const ORDERS = [
  { id: 'CMD-001', buyer: 'Marie Dupont', items: 3, total: '14,70 €', status: 'En attente' },
  { id: 'CMD-002', buyer: 'Jean Martin', items: 1, total: '4,90 €', status: 'Expédié' },
  { id: 'CMD-003', buyer: 'Sophie Bernard', items: 5, total: '24,50 €', status: 'Livré' },
]

const statusColor: Record<string, string> = {
  'En attente': '#b8431a',
  'Expédié': '#2563eb',
  'Livré': '#16a34a',
}

export default function DashboardPage() {
  const { t } = useTranslation()

  const stats = [
    { label: t('stats.pendingOrders'), value: '3', icon: <PackageIcon size={20} /> },
    { label: t('stats.monthlyRevenue'), value: '147,00 €', icon: null },
    { label: t('stats.activeProducts'), value: '24', icon: null },
  ]

  const tableHeaders = [
    t('orders.ref'), t('orders.buyer'), t('orders.items'),
    t('orders.total'), t('orders.status'), '',
  ]

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px 64px' }}>
      <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, marginBottom: 32 }}>
        {t('dashboard.title')}
      </h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 20, marginBottom: 48 }}>
        {stats.map(stat => (
          <Card key={stat.label} style={{ padding: '20px 24px' }}>
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
              {stat.icon}{stat.label}
            </div>
            <div style={{ fontSize: 28, fontWeight: 700 }}>{stat.value}</div>
          </Card>
        ))}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }} id="orders">
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 22, fontWeight: 700 }}>{t('orders.recent')}</h2>
        <Button variant="secondary" size="sm">{t('orders.viewAll')}</Button>
      </div>

      <Card>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border)' }}>
              {tableHeaders.map((h, i) => (
                <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {ORDERS.map(o => (
              <tr key={o.id} style={{ borderBottom: '1px solid var(--border)' }}>
                <td style={{ padding: '14px 16px', fontWeight: 600 }}>{o.id}</td>
                <td style={{ padding: '14px 16px' }}>{o.buyer}</td>
                <td style={{ padding: '14px 16px' }}>{o.items}</td>
                <td style={{ padding: '14px 16px', fontWeight: 600 }}>{o.total}</td>
                <td style={{ padding: '14px 16px' }}>
                  <span style={{ color: statusColor[o.status], fontWeight: 600, fontSize: 13 }}>{o.status}</span>
                </td>
                <td style={{ padding: '14px 16px' }}>
                  <Button variant="ghost" size="sm">{t('orders.details')}</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
