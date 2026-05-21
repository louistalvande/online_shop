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
      <Header session={null} onShowLogin={() => {}} onLogout={() => {}}>
        <div className="orders-login-required">
          <p>{t('orders.error.loginRequired')}</p>
        </div>
      </Header>
    )
  }

  return (
    <Header session={session} onShowLogin={() => {}} onLogout={() => { window.location.href = '/' }}>
      <div className="orders-container">
        <h1>{t('orders.list.title')}</h1>

        {loading && <p>{t('orders.loading')}</p>}
        {error && <p className="orders-error">{error}</p>}

        {!loading && !error && orders.length === 0 && <p>{t('orders.list.empty')}</p>}

        {!loading && !error && orders.length > 0 && (
          <table className="orders-table">
            <thead>
              <tr className="orders-thead-row">
                <th className="orders-th">{t('orders.ref')}</th>
                <th className="orders-th">{t('orders.status')}</th>
                <th className="orders-th">{t('orders.total')}</th>
                <th className="orders-th">{t('orders.date')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map(o => (
                <tr key={o.id} className="orders-tbody-row">
                  <td className="orders-td"><strong>{o.orderNumber}</strong></td>
                  <td className="orders-td">{t(STATUS_LABELS[o.status] ?? o.status)}</td>
                  <td className="orders-td">{o.totalAmountTtc.toFixed(2)} €</td>
                  <td className="orders-td">{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td className="orders-td">
                    <a href={`/my-orders/${o.id}`}>{t('orders.viewDetail')}</a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Header>
  )
}
