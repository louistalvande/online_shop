import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { getMyOrder, cancelOrder, requestPostShipmentCancellation, type OrderData } from './api/orderApi'
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

/** Buyer order detail — shows status, lines, and tracking info (US-EXP-02). */
export default function OrderDetailPage({ orderId }: Props) {
  const { t } = useTranslation()
  const session = getSession()
  const [order, setOrder] = useState<OrderData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [cancelLoading, setCancelLoading] = useState(false)
  const [cancelError, setCancelError] = useState<string | null>(null)
  const [buyerIban, setBuyerIban] = useState('')
  const [pscReason, setPscReason] = useState('')
  const [pscIban, setPscIban] = useState('')
  const [pscLoading, setPscLoading] = useState(false)
  const [pscError, setPscError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) return
    getMyOrder(orderId)
      .then(setOrder)
      .catch(err => setError(err.message === 'ORDER_NOT_FOUND' ? t('orders.error.notFound') : t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [orderId])

  if (!session) {
    return (
      <Header session={null} onShowLogin={() => {}} onLogout={() => {}}>
        <div className="order-detail-login-required">
          <p>{t('orders.error.loginRequired')}</p>
        </div>
      </Header>
    )
  }

  return (
    <Header session={session} onShowLogin={() => {}} onLogout={() => { window.location.href = '/' }}>
      <div className="order-detail-container">
        <p><a href="/my-orders">← {t('orders.list.title')}</a></p>

        {loading && <p>{t('orders.loading')}</p>}
        {error && <p className="order-detail-error">{error}</p>}

        {!loading && order && (
          <>
            <h1>{t('orders.detail.title', { number: order.orderNumber })}</h1>

            <section className="order-detail-section">
              <p><strong>{t('orders.status')}:</strong> {t(STATUS_LABELS[order.status] ?? order.status)}</p>
              <p><strong>{t('orders.paymentMethod')}:</strong> {t(`orders.paymentMethod.${order.paymentMethod}`)}</p>
              <p><strong>{t('orders.total')}:</strong> {order.totalAmountTtc.toFixed(2)} €</p>
              <p><strong>{t('orders.date')}:</strong> {new Date(order.createdAt).toLocaleString()}</p>
            </section>

            <section className="order-detail-section">
              <h2>{t('orders.detail.delivery')}</h2>
              <p>{order.deliveryAddressLine}, {order.deliveryPostalCode} {order.deliveryCity} ({order.deliveryCountryCode})</p>
              <p><strong>{t('orders.detail.carrier')}:</strong> {order.carrierName}</p>
            </section>

            {/* Tracking section — US-EXP-02 */}
            {(order.status === 'SHIPPED' || order.status === 'DELIVERED'
              || order.status === 'CANCELLATION_REQUESTED_AFTER_SHIPMENT') && (
              <section className="order-detail-tracking">
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
              <section className="order-detail-in-prep">
                <p>{t('orders.tracking.inPreparation')}</p>
              </section>
            )}

            {/* Post-shipment cancellation request — US-CAN-06 */}
            {order.status === 'SHIPPED' && (
              <section className="order-detail-cancel">
                <h2>{t('orders.psc.title')}</h2>
                <p>{t('orders.psc.description')}</p>
                <div style={{ marginBottom: '0.75rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.25rem' }}>{t('orders.psc.reasonLabel')} *</label>
                  <textarea
                    value={pscReason}
                    onChange={e => setPscReason(e.target.value)}
                    placeholder={t('orders.psc.reasonPlaceholder')}
                    maxLength={500}
                    rows={3}
                    style={{ padding: '0.4rem 0.8rem', border: '1px solid #ccc', borderRadius: '4px', width: '100%', maxWidth: '500px' }}
                  />
                </div>
                {order.paymentMethod === 'WIRE_TRANSFER' && (
                  <div style={{ marginBottom: '0.75rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.25rem' }}>{t('orders.psc.ibanLabel')} *</label>
                    <input
                      type="text"
                      value={pscIban}
                      onChange={e => setPscIban(e.target.value)}
                      placeholder={t('orders.psc.ibanPlaceholder')}
                      style={{ padding: '0.4rem 0.8rem', border: '1px solid #ccc', borderRadius: '4px', width: '100%', maxWidth: '400px' }}
                    />
                  </div>
                )}
                {pscError && <p className="order-detail-cancel-error">{pscError}</p>}
                <button
                  className="order-detail-cancel-btn"
                  onClick={async () => {
                    if (!window.confirm(t('orders.psc.confirmPrompt'))) return
                    setPscLoading(true)
                    setPscError(null)
                    try {
                      const updated = await requestPostShipmentCancellation(
                        orderId, pscReason,
                        order.paymentMethod === 'WIRE_TRANSFER' ? pscIban : undefined
                      )
                      setOrder(updated)
                    } catch (err: unknown) {
                      const code = (err as Error).message
                      if (code === 'MISSING_BUYER_IBAN') setPscError(t('orders.psc.ibanRequired'))
                      else if (code === 'INVALID_ORDER_STATE') setPscError(t('orders.psc.invalidState'))
                      else setPscError(t('orders.psc.error'))
                    } finally {
                      setPscLoading(false)
                    }
                  }}
                  disabled={pscLoading || !pscReason.trim() || (order.paymentMethod === 'WIRE_TRANSFER' && !pscIban.trim())}
                >
                  {pscLoading ? t('orders.psc.submitting') : t('orders.psc.submit')}
                </button>
              </section>
            )}

            {/* Cancellation request pending vendor decision — US-CAN-06 */}
            {order.status === 'CANCELLATION_REQUESTED_AFTER_SHIPMENT' && (
              <section className="order-detail-wire-refund">
                <p><strong>{t('orders.psc.pendingVendorDecision')}</strong></p>
                {order.cancellationReason && (
                  <p>{t('orders.psc.reasonLabel')}: <em>{order.cancellationReason}</em></p>
                )}
              </section>
            )}

            {/* Cancel section — US-CAN-01 */}
            {(order.status === 'AWAITING_PROCESSING' || order.status === 'IN_PREPARATION') && (
              <section className="order-detail-cancel">
                <h2>{t('orders.cancel.title')}</h2>
                <p>{t('orders.cancel.description')}</p>
                {order.paymentMethod === 'WIRE_TRANSFER' && (
                  <div className="order-detail-cancel-iban">
                    <label className="order-detail-cancel-iban-label">
                      {t('orders.cancel.ibanLabel')} *
                    </label>
                    <input
                      type="text"
                      value={buyerIban}
                      onChange={e => setBuyerIban(e.target.value)}
                      placeholder={t('orders.cancel.ibanPlaceholder')}
                      className="order-detail-cancel-iban-input"
                    />
                  </div>
                )}
                {cancelError && <p className="order-detail-cancel-error">{cancelError}</p>}
                <button
                  className="order-detail-cancel-btn"
                  onClick={async () => {
                    if (!window.confirm(t('orders.cancel.confirmPrompt'))) return
                    setCancelLoading(true)
                    setCancelError(null)
                    try {
                      const updated = await cancelOrder(orderId, order.paymentMethod === 'WIRE_TRANSFER' ? buyerIban : undefined)
                      setOrder(updated)
                    } catch (err: unknown) {
                      const code = (err as Error).message
                      if (code === 'MISSING_BUYER_IBAN') setCancelError(t('orders.cancel.ibanRequired'))
                      else if (code === 'INVALID_ORDER_STATE') setCancelError(t('orders.cancel.invalidState'))
                      else setCancelError(t('orders.cancel.error'))
                    } finally {
                      setCancelLoading(false)
                    }
                  }}
                  disabled={cancelLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !buyerIban.trim())}
                >
                  {cancelLoading ? t('orders.cancel.cancelling') : t('orders.cancel.submit')}
                </button>
              </section>
            )}

            {order.status === 'WIRE_REFUND_IN_PROGRESS' && (
              <section className="order-detail-wire-refund">
                <p><strong>{t('orders.cancel.wireRefundInProgress')}</strong></p>
                {order.buyerIban && <p>{t('orders.cancel.ibanUsed')}: <code>{order.buyerIban}</code></p>}
              </section>
            )}

            <section className="order-detail-support">
              <p>{t('orders.support.message')}</p>
            </section>

            <section>
              <h2>{t('orders.detail.lines')}</h2>
              <table className="order-detail-table">
                <thead>
                  <tr className="order-detail-thead-row">
                    <th className="order-detail-th--left">{t('orders.detail.product')}</th>
                    <th className="order-detail-th--right">{t('orders.detail.unitPrice')}</th>
                    <th className="order-detail-th--right">{t('orders.items')}</th>
                    <th className="order-detail-th--right">{t('orders.total')}</th>
                  </tr>
                </thead>
                <tbody>
                  {order.lines.map(line => (
                    <tr key={line.id} className="order-detail-tbody-row">
                      <td className="order-detail-td">{line.productName}</td>
                      <td className="order-detail-td--right">{line.unitPriceTtc.toFixed(2)} €</td>
                      <td className="order-detail-td--right">{line.quantity}</td>
                      <td className="order-detail-td--right">{line.lineTotalTtc.toFixed(2)} €</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          </>
        )}
      </div>
    </Header>
  )
}
