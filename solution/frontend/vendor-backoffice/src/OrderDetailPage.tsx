import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { getVendorOrder, confirmWirePayment, rejectWirePayment, shipOrder, acceptReturn, confirmReturn, waiveReturn, confirmWireRefund, refuseCancellationRequest, type OrderData } from './api/orderApi'
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
  CANCELLATION_REQUESTED_AFTER_SHIPMENT: 'orders.status.cancellationRequested',
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
  const [trackingInput, setTrackingInput] = useState('')
  const [returnIban, setReturnIban] = useState('')

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
              <section style={{ background: '#fff8e1', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
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

            {(order.status === 'AWAITING_PROCESSING' || order.status === 'IN_PREPARATION') && (
              <section style={{ background: '#e8f5e9', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.ship.title')}</h2>
                <p>{t('orders.ship.description')}</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem', alignItems: 'center' }}>
                  <input
                    type="text"
                    value={trackingInput}
                    onChange={e => setTrackingInput(e.target.value)}
                    placeholder={t('orders.ship.trackingPlaceholder')}
                    style={{ padding: '0.4rem 0.8rem', border: '1px solid #ccc', borderRadius: '4px', flex: 1 }}
                  />
                  <button
                    onClick={async () => {
                      if (!trackingInput.trim()) return
                      if (!window.confirm(t('orders.ship.confirmPrompt'))) return
                      setActionLoading(true)
                      setActionError(null)
                      try {
                        const updated = await shipOrder(orderId, trackingInput.trim())
                        setOrder(updated)
                        setTrackingInput('')
                      } catch {
                        setActionError(t('orders.ship.error'))
                      } finally {
                        setActionLoading(false)
                      }
                    }}
                    disabled={actionLoading || !trackingInput.trim()}
                    style={{ padding: '0.5rem 1.2rem', background: '#1565c0', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.ship.shipping') : t('orders.ship.submit')}
                  </button>
                </div>
              </section>
            )}

            {/* Post-shipment cancellation — US-CAN-03 / US-CAN-04 (vendor initiated for SHIPPED orders) */}
            {order.status === 'SHIPPED' && (
              <section style={{ background: '#fff3e0', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.postShipCancel.title')}</h2>
                <p>{t('orders.postShipCancel.description')}</p>
                {order.paymentMethod === 'WIRE_TRANSFER' && (
                  <div style={{ marginBottom: '0.75rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.25rem' }}>{t('orders.postShipCancel.ibanLabel')} *</label>
                    <input
                      type="text"
                      value={returnIban}
                      onChange={e => setReturnIban(e.target.value)}
                      placeholder={t('orders.postShipCancel.ibanPlaceholder')}
                      style={{ padding: '0.4rem 0.8rem', border: '1px solid #ccc', borderRadius: '4px', width: '100%', maxWidth: '400px' }}
                    />
                  </div>
                )}
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                  <button
                    onClick={async () => {
                      if (!window.confirm(t('orders.postShipCancel.acceptReturnPrompt'))) return
                      setActionLoading(true); setActionError(null)
                      try {
                        const updated = await acceptReturn(orderId, order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        setOrder(updated)
                      } catch {
                        setActionError(t('orders.postShipCancel.error'))
                      } finally { setActionLoading(false) }
                    }}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#e65100', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.acceptReturn')}
                  </button>
                  <button
                    onClick={async () => {
                      if (!window.confirm(t('orders.postShipCancel.waiveReturnPrompt'))) return
                      setActionLoading(true); setActionError(null)
                      try {
                        const updated = await waiveReturn(orderId, order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        setOrder(updated)
                      } catch {
                        setActionError(t('orders.postShipCancel.error'))
                      } finally { setActionLoading(false) }
                    }}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#c62828', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.waiveReturn')}
                  </button>
                </div>
              </section>
            )}

            {/* Buyer's post-shipment cancellation request — US-CAN-06 */}
            {order.status === 'CANCELLATION_REQUESTED_AFTER_SHIPMENT' && (
              <section style={{ background: '#fbe9e7', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.buyerCancelRequest.title')}</h2>
                <p>{t('orders.buyerCancelRequest.description')}</p>
                {order.cancellationReason && (
                  <p><strong>{t('orders.buyerCancelRequest.reason')}:</strong> <em>{order.cancellationReason}</em></p>
                )}
                {order.buyerIban && (
                  <p><strong>{t('orders.buyerCancelRequest.buyerIban')}:</strong> <code>{order.buyerIban}</code></p>
                )}
                {order.paymentMethod === 'WIRE_TRANSFER' && !order.buyerIban && (
                  <div style={{ marginBottom: '0.75rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.25rem' }}>{t('orders.postShipCancel.ibanLabel')} *</label>
                    <input
                      type="text"
                      value={returnIban}
                      onChange={e => setReturnIban(e.target.value)}
                      placeholder={t('orders.postShipCancel.ibanPlaceholder')}
                      style={{ padding: '0.4rem 0.8rem', border: '1px solid #ccc', borderRadius: '4px', width: '100%', maxWidth: '400px' }}
                    />
                  </div>
                )}
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                  <button
                    onClick={async () => {
                      if (!window.confirm(t('orders.buyerCancelRequest.acceptReturnPrompt'))) return
                      setActionLoading(true); setActionError(null)
                      try {
                        const iban = order.buyerIban ?? (order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        const updated = await acceptReturn(orderId, iban)
                        setOrder(updated)
                      } catch {
                        setActionError(t('orders.postShipCancel.error'))
                      } finally { setActionLoading(false) }
                    }}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !order.buyerIban && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#e65100', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.acceptReturn')}
                  </button>
                  <button
                    onClick={async () => {
                      if (!window.confirm(t('orders.buyerCancelRequest.waiveReturnPrompt'))) return
                      setActionLoading(true); setActionError(null)
                      try {
                        const iban = order.buyerIban ?? (order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        const updated = await waiveReturn(orderId, iban)
                        setOrder(updated)
                      } catch {
                        setActionError(t('orders.postShipCancel.error'))
                      } finally { setActionLoading(false) }
                    }}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !order.buyerIban && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#c62828', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.waiveReturn')}
                  </button>
                  <button
                    onClick={async () => {
                      if (!window.confirm(t('orders.buyerCancelRequest.refusePrompt'))) return
                      setActionLoading(true); setActionError(null)
                      try {
                        const updated = await refuseCancellationRequest(orderId)
                        setOrder(updated)
                      } catch {
                        setActionError(t('orders.buyerCancelRequest.refuseError'))
                      } finally { setActionLoading(false) }
                    }}
                    disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#546e7a', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.buyerCancelRequest.refuse')}
                  </button>
                </div>
              </section>
            )}

            {/* Confirm return received — US-CAN-03 */}
            {order.status === 'PENDING_RETURN' && (
              <section style={{ background: '#fce4ec', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.confirmReturn.title')}</h2>
                <p>{t('orders.confirmReturn.description')}</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <button
                  onClick={async () => {
                    if (!window.confirm(t('orders.confirmReturn.prompt'))) return
                    setActionLoading(true); setActionError(null)
                    try {
                      const updated = await confirmReturn(orderId)
                      setOrder(updated)
                    } catch {
                      setActionError(t('orders.confirmReturn.error'))
                    } finally { setActionLoading(false) }
                  }}
                  disabled={actionLoading}
                  style={{ padding: '0.5rem 1.2rem', background: '#2e7d32', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                  {t('orders.confirmReturn.submit')}
                </button>
              </section>
            )}

            {/* Wire refund confirmation — US-CAN-05 */}
            {order.status === 'WIRE_REFUND_IN_PROGRESS' && (
              <section style={{ background: '#e8eaf6', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.wireRefund.title')}</h2>
                <p>{t('orders.wireRefund.description')}</p>
                {order.buyerIban && (
                  <p><strong>{t('orders.wireRefund.buyerIban')}:</strong> <code>{order.buyerIban}</code></p>
                )}
                <p><strong>{t('orders.wireRefund.amount')}:</strong> {order.totalAmountTtc.toFixed(2)} €</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <button
                  onClick={async () => {
                    if (!window.confirm(t('orders.wireRefund.prompt'))) return
                    setActionLoading(true); setActionError(null)
                    try {
                      const updated = await confirmWireRefund(orderId)
                      setOrder(updated)
                    } catch {
                      setActionError(t('orders.wireRefund.error'))
                    } finally { setActionLoading(false) }
                  }}
                  disabled={actionLoading}
                  style={{ padding: '0.5rem 1.2rem', background: '#1a237e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                  {t('orders.wireRefund.submit')}
                </button>
              </section>
            )}
          </>
        )}
      </main>
    </>
  )
}
