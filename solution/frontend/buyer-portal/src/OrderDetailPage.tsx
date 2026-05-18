import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { getMyOrder, type OrderData } from './api/orderApi'
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

interface Props {
  orderId: string
}

/** Buyer order detail — shows status, lines, and tracking info (US-EXP-02). */
export default function OrderDetailPage({ orderId }: Props) {
  const { t } = useTranslation()
  const session = getSession()
  const [order, setOrder] = useState<OrderData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) return
    getMyOrder(orderId)
      .then(setOrder)
      .catch(err => setError(err.message === 'ORDER_NOT_FOUND' ? t('orders.error.notFound') : t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [orderId])

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
      <main style={{ padding: '2rem', maxWidth: '900px', margin: '0 auto' }}>
        <p><a href="/my-orders">← {t('orders.list.title')}</a></p>

        {loading && <p>{t('orders.loading')}</p>}
        {error && <p style={{ color: 'red' }}>{error}</p>}

        {!loading && order && (
          <>
            <h1>{t('orders.detail.title', { number: order.orderNumber })}</h1>

            <section style={{ marginBottom: '1.5rem' }}>
              <p><strong>{t('orders.status')}:</strong> {t(STATUS_LABELS[order.status] ?? order.status)}</p>
              <p><strong>{t('orders.paymentMethod')}:</strong> {t(`orders.paymentMethod.${order.paymentMethod}`)}</p>
              <p><strong>{t('orders.total')}:</strong> {order.totalAmountTtc.toFixed(2)} €</p>
              <p><strong>{t('orders.date')}:</strong> {new Date(order.createdAt).toLocaleString()}</p>
            </section>

            <section style={{ marginBottom: '1.5rem' }}>
              <h2>{t('orders.detail.delivery')}</h2>
              <p>{order.deliveryAddressLine}, {order.deliveryPostalCode} {order.deliveryCity} ({order.deliveryCountryCode})</p>
              <p><strong>{t('orders.detail.carrier')}:</strong> {order.carrierName}</p>
            </section>

            {/* Tracking section — US-EXP-02 */}
            {(order.status === 'SHIPPED' || order.status === 'DELIVERED') && (
              <section style={{ background: '#e3f2fd', padding: '1rem', borderRadius: '4px', marginBottom: '1.5rem' }}>
                <h2>{t('orders.tracking.title')}</h2>
                {order.trackingNumber ? (
                  <p>
                    {t('orders.tracking.number')}: <strong>{order.trackingNumber}</strong>{' '}
                    — <a href={`${order.carrierTrackingUrl}${order.trackingNumber}`} target="_blank" rel="noreferrer">
                      {t('orders.tracking.link', { carrier: order.carrierName })}
                    </a>
                  </p>
                ) : (
                  <p>{t('orders.tracking.notAvailable')}</p>
                )}
              </section>
            )}

            {order.status === 'IN_PREPARATION' && (
              <section style={{ background: '#fff9c4', padding: '1rem', borderRadius: '4px', marginBottom: '1.5rem' }}>
                <p>{t('orders.tracking.inPreparation')}</p>
              </section>
            )}

            <section>
              <h2>{t('orders.detail.lines')}</h2>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #ddd' }}>
                    <th style={{ textAlign: 'left', padding: '0.4rem' }}>{t('orders.detail.product')}</th>
                    <th style={{ textAlign: 'right', padding: '0.4rem' }}>{t('orders.detail.unitPrice')}</th>
                    <th style={{ textAlign: 'right', padding: '0.4rem' }}>{t('orders.items')}</th>
                    <th style={{ textAlign: 'right', padding: '0.4rem' }}>{t('orders.total')}</th>
                  </tr>
                </thead>
                <tbody>
                  {order.lines.map(line => (
                    <tr key={line.id} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '0.4rem' }}>{line.productName}</td>
                      <td style={{ textAlign: 'right', padding: '0.4rem' }}>{line.unitPriceTtc.toFixed(2)} €</td>
                      <td style={{ textAlign: 'right', padding: '0.4rem' }}>{line.quantity}</td>
                      <td style={{ textAlign: 'right', padding: '0.4rem' }}>{line.lineTotalTtc.toFixed(2)} €</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          </>
        )}
      </main>
    </>
  )
}
