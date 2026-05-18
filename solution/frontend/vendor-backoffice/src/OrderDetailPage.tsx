import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { getVendorOrder, confirmWirePayment, rejectWirePayment, type OrderData } from './api/orderApi'
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

/** Vendor order detail page — US-VND-01 (view) and US-VND-02 (wire actions). */
export default function OrderDetailPage({ orderId }: Props) {
  const { t } = useTranslation()
  const session = getSession()
  const [order, setOrder] = useState<OrderData | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) return
    getVendorOrder(orderId)
      .then(setOrder)
      .catch(err => setError(err.code === 'NOT_FOUND' ? t('orders.error.notFound') : t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [orderId])

  const handleConfirmWire = async () => {
    if (!window.confirm(t('orders.wire.confirmPrompt'))) return
    setActionLoading(true)
    setActionError(null)
    try {
      const updated = await confirmWirePayment(orderId)
      setOrder(updated)
    } catch {
      setActionError(t('orders.wire.confirmError'))
    } finally {
      setActionLoading(false)
    }
  }

  const handleRejectWire = async () => {
    if (!window.confirm(t('orders.wire.rejectPrompt'))) return
    setActionLoading(true)
    setActionError(null)
    try {
      const updated = await rejectWirePayment(orderId)
      setOrder(updated)
    } catch {
      setActionError(t('orders.wire.rejectError'))
    } finally {
      setActionLoading(false)
    }
  }

  if (!session) {
    return (
      <>
        <Header />
        <main style={{ padding: '2rem' }}>
          <p>{t('orders.error.notAuthenticated')}</p>
        </main>
      </>
    )
  }

  return (
    <>
      <Header />
      <main style={{ padding: '2rem', maxWidth: '900px', margin: '0 auto' }}>
        <p><a href="/orders">← {t('orders.list.title')}</a></p>

        {loading && <p>{t('catalog.loading')}</p>}
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
              {order.trackingNumber && (
                <p>
                  <strong>{t('orders.detail.tracking')}:</strong>{' '}
                  <a href={`${order.carrierTrackingUrl}${order.trackingNumber}`} target="_blank" rel="noreferrer">
                    {order.trackingNumber}
                  </a>
                </p>
              )}
            </section>

            <section style={{ marginBottom: '1.5rem' }}>
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

            {order.status === 'PAYMENT_PENDING_WIRE' && (
              <section style={{ background: '#fff8e1', padding: '1rem', borderRadius: '4px' }}>
                <h2>{t('orders.wire.title')}</h2>
                <p>{t('orders.wire.description')}</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                  <button onClick={handleConfirmWire} disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#2e7d32', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.wire.confirming') : t('orders.wire.confirm')}
                  </button>
                  <button onClick={handleRejectWire} disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#c62828', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.wire.rejecting') : t('orders.wire.reject')}
                  </button>
                </div>
              </section>
            )}
          </>
        )}
      </main>
    </>
  )
}
