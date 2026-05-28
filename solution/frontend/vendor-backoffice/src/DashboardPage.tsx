import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, PackageIcon } from '@workspace/theme'
import { listVendorOrders, type OrderData, type OrderStatus } from './api/orderApi'

const STATUS_LABELS: Record<OrderStatus, string> = {
  PAYMENT_PENDING_CARD: 'orders.status.pendingCard',
  PAYMENT_PENDING_WIRE: 'orders.status.pendingWire',
  AWAITING_PROCESSING: 'orders.status.awaitingProcessing',
  IN_PREPARATION: 'orders.status.inPreparation',
  SHIPPED: 'orders.status.shipped',
  DELIVERED: 'orders.status.delivered',
  CANCELLED: 'orders.status.cancelled',
  PENDING_RETURN: 'orders.status.pendingReturn',
  WIRE_REFUND_IN_PROGRESS: 'orders.status.wireRefund',
  CANCELLATION_REQUESTED_AFTER_SHIPMENT: 'orders.status.cancellationRequested',
}

const PENDING_STATUSES: OrderStatus[] = ['PAYMENT_PENDING_WIRE', 'AWAITING_PROCESSING', 'IN_PREPARATION']

export default function DashboardPage() {
  const { t } = useTranslation()
  const [orders, setOrders] = useState<OrderData[]>([])

  useEffect(() => {
    listVendorOrders().then(setOrders).catch(() => {})
  }, [])

  const now = new Date()
  const pendingCount = orders.filter(o => PENDING_STATUSES.includes(o.status)).length
  const monthlyRevenue = orders
    .filter(o => {
      const d = new Date(o.createdAt)
      return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && o.status !== 'CANCELLED'
    })
    .reduce((sum, o) => sum + o.totalAmountTtc, 0)

  const stats = [
    { label: t('stats.pendingOrders'), value: String(pendingCount), icon: <PackageIcon size={20} /> },
    { label: t('stats.monthlyRevenue'), value: `${monthlyRevenue.toFixed(2)} €`, icon: null },
  ]

  const recent = orders.slice(0, 5)

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
        <Button variant="secondary" size="sm" onClick={() => { window.location.href = `${import.meta.env.BASE_URL}orders` }}>
          {t('orders.viewAll')}
        </Button>
      </div>

      <Card>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border)' }}>
              {[t('orders.ref'), t('orders.total'), t('orders.status'), t('orders.date'), ''].map((h, i) => (
                <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {recent.length === 0 && (
              <tr>
                <td colSpan={5} style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{t('orders.list.empty')}</td>
              </tr>
            )}
            {recent.map(o => (
              <tr key={o.id} style={{ borderBottom: '1px solid var(--border)' }}>
                <td style={{ padding: '14px 16px', fontWeight: 600 }}>{o.orderNumber}</td>
                <td style={{ padding: '14px 16px', fontWeight: 600 }}>{o.totalAmountTtc.toFixed(2)} €</td>
                <td style={{ padding: '14px 16px' }}>{t(STATUS_LABELS[o.status] ?? o.status)}</td>
                <td style={{ padding: '14px 16px' }}>{new Date(o.createdAt).toLocaleDateString()}</td>
                <td style={{ padding: '14px 16px' }}>
                  <Button variant="ghost" size="sm" onClick={() => { window.location.href = `${import.meta.env.BASE_URL}orders/${o.id}` }}>
                    {t('orders.details')}
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
