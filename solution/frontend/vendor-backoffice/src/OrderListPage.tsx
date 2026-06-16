import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { listVendorOrders, type OrderData } from './api/orderApi'
import { getSession, logout } from './api/authApi'
import { getShopTheme } from './api/shopConfigApi'
import Header from './Header'

const STATUS_LABELS: Record<string, string> = {
  PAYMENT_PENDING_CARD: 'orders.status.pendingCard',
  PAYMENT_PENDING_WIRE: 'orders.status.pendingWire',
  AWAITING_PROCESSING: 'orders.status.awaitingProcessing',
  IN_PREPARATION: 'orders.status.inPreparation',
  SHIPPED: 'orders.status.shipped',
  DELIVERED: 'orders.status.delivered',
  CANCELLED: 'orders.status.cancelled',
  PENDING_RETURN: 'orders.status.pendingReturn',
  WIRE_REFUND_IN_PROGRESS: 'orders.status.wireRefund',
}

/** Vendor order list page — US-VND-01. */
export default function OrderListPage() {
  const { t, i18n } = useTranslation()
  const session = getSession()
  const [orders, setOrders] = useState<OrderData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [shopName, setShopName] = useState('')

  useEffect(() => {
    getShopTheme().then(t => { setLogoUrl(t.logoUrl ?? null); if (t.shopName) setShopName(t.shopName) }).catch(() => {})
    if (!session) return
    listVendorOrders()
      .then(setOrders)
      .catch(() => setError(t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [])

  const onLogout = () => { logout(); window.location.href = import.meta.env.BASE_URL }

  if (!session) {
    return (
      <Header onLogout={onLogout} logoUrl={logoUrl} shopName={shopName}>
        <div style={{ padding: '2rem' }}><p>{t('orders.error.notAuthenticated')}</p></div>
      </Header>
    )
  }

  return (
    <Header onLogout={onLogout} logoUrl={logoUrl} shopName={shopName}>
    <main style={{ padding: '2rem', maxWidth: '1100px', margin: '0 auto' }}>
      <h1>{t('orders.list.title')}</h1>

      {loading && <p>{t('catalog.loading')}</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {!loading && !error && orders.length === 0 && (
        <p>{t('orders.list.empty')}</p>
      )}

      {!loading && !error && orders.length > 0 && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #ddd' }}>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.ref')}</th>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.status')}</th>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.total')}</th>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.paymentMethod')}</th>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.date')}</th>
              <th style={{ textAlign: 'left', padding: '0.5rem' }}></th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => (
              <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: '0.5rem' }}><strong>{order.orderNumber}</strong></td>
                <td style={{ padding: '0.5rem' }}>{t(STATUS_LABELS[order.status] ?? order.status)}</td>
                <td style={{ padding: '0.5rem' }}>{order.totalAmountTtc.toFixed(2)} €</td>
                <td style={{ padding: '0.5rem' }}>{t(`orders.paymentMethod.${order.paymentMethod}`)}</td>
                <td style={{ padding: '0.5rem' }}>{new Date(order.createdAt).toLocaleDateString()}</td>
                <td style={{ padding: '0.5rem' }}>
                  <a href={`${import.meta.env.BASE_URL}orders/${order.id}`}>{t('orders.details')}</a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
    </Header>
  )
}
