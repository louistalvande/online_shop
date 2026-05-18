import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { getMyOrders, type OrderData } from './api/orderApi'
import { getSession } from './api/authApi'

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

/** Buyer order list — US-ORD-05 / US-EXP-02. */
export default function OrderListPage() {
  const { t } = useTranslation()
  const session = getSession()
  const [orders, setOrders] = useState<OrderData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) return
    getMyOrders()
      .then(setOrders)
      .catch(() => setError(t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [])

  if (!session) {
    return (
      <>
        <Header />
        <main style={{ padding: '2rem' }}>
          <p>{t('orders.error.loginRequired')}</p>
        </main>
      </>
    )
  }

  return (
    <>
      <Header />
      <main style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
        <h1>{t('orders.list.title')}</h1>

        {loading && <p>{t('orders.loading')}</p>}
        {error && <p style={{ color: 'red' }}>{error}</p>}

        {!loading && !error && orders.length === 0 && <p>{t('orders.list.empty')}</p>}

        {!loading && !error && orders.length > 0 && (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #ddd' }}>
                <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.ref')}</th>
                <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.status')}</th>
                <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.total')}</th>
                <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('orders.date')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map(o => (
                <tr key={o.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '0.5rem' }}><strong>{o.orderNumber}</strong></td>
                  <td style={{ padding: '0.5rem' }}>{t(STATUS_LABELS[o.status] ?? o.status)}</td>
                  <td style={{ padding: '0.5rem' }}>{o.totalAmountTtc.toFixed(2)} €</td>
                  <td style={{ padding: '0.5rem' }}>{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td style={{ padding: '0.5rem' }}>
                    <a href={`/my-orders/${o.id}`}>{t('orders.viewDetail')}</a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>
    </>
  )
}
