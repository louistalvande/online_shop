import { useEffect, useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import Header from './Header'
import { getVendorOrder, confirmWirePayment, rejectWirePayment, markInPreparation, shipOrder, acceptReturn, confirmReturn, waiveReturn, confirmWireRefund, refuseCancellationRequest, type OrderData } from './api/orderApi'
import { getSession, logout } from './api/authApi'
import ConfirmModal from './ConfirmModal'

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

interface ModalConfig {
  title: string
  message: string
  confirmLabel: string
  cancelLabel: string
  variant: 'primary' | 'danger'
  onConfirm: () => void
}

interface Props {
  orderId: string
}

/** Vendor order detail page — US-VND-01 (view) and US-VND-02 (wire actions). */
export default function OrderDetailPage({ orderId }: Props) {
  const { t, i18n } = useTranslation()
  const session = getSession()
  const [order, setOrder] = useState<OrderData | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [trackingInput, setTrackingInput] = useState('')
  const [returnIban, setReturnIban] = useState('')
  const [modal, setModal] = useState<ModalConfig | null>(null)

  useEffect(() => {
    if (!session) return
    getVendorOrder(orderId)
      .then(setOrder)
      .catch(err => setError(err.code === 'NOT_FOUND' ? t('orders.error.notFound') : t('orders.error.load')))
      .finally(() => setLoading(false))
  }, [orderId])

  const closeModal = useCallback(() => setModal(null), [])

  function ask(config: Omit<ModalConfig, 'cancelLabel'> & { cancelLabel?: string }) {
    setModal({ cancelLabel: t('common.cancel'), ...config })
  }

  async function run(action: () => Promise<OrderData>, errorKey: string) {
    setModal(null)
    setActionLoading(true)
    setActionError(null)
    try {
      const updated = await action()
      setOrder(updated)
    } catch {
      setActionError(t(errorKey))
    } finally {
      setActionLoading(false)
    }
  }

  const onLogout = () => { logout(); window.location.href = import.meta.env.BASE_URL }

  if (!session) {
    return (
      <Header onLogout={onLogout}>
        <div style={{ padding: '2rem' }}><p>{t('orders.error.notAuthenticated')}</p></div>
      </Header>
    )
  }

  return (
    <Header onLogout={onLogout}>
      {modal && (
        <ConfirmModal
          title={modal.title}
          message={modal.message}
          confirmLabel={modal.confirmLabel}
          cancelLabel={modal.cancelLabel}
          variant={modal.variant}
          onConfirm={modal.onConfirm}
          onCancel={closeModal}
        />
      )}

      <main style={{ padding: '2rem', maxWidth: '900px', margin: '0 auto' }}>
        <p><a href={`${import.meta.env.BASE_URL}orders`}>← {t('orders.list.title')}</a></p>

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
              <p><strong>{order.buyerFirstName} {order.buyerLastName}</strong> — {order.buyerEmail}</p>
              <p><strong>{order.deliveryRecipientName}</strong></p>
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

            {/* Wire transfer confirmation — US-VND-02 */}
            {order.status === 'PAYMENT_PENDING_WIRE' && (
              <section style={{ background: '#fff8e1', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.wire.title')}</h2>
                <p>{t('orders.wire.description')}</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                  <button
                    onClick={() => ask({
                      title: t('orders.wire.confirmTitle'),
                      message: t('orders.wire.confirmPrompt'),
                      confirmLabel: t('orders.wire.confirm'),
                      variant: 'primary',
                      onConfirm: () => run(() => confirmWirePayment(orderId), 'orders.wire.confirmError'),
                    })}
                    disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#2e7d32', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.wire.confirming') : t('orders.wire.confirm')}
                  </button>
                  <button
                    onClick={() => ask({
                      title: t('orders.wire.rejectTitle'),
                      message: t('orders.wire.rejectPrompt'),
                      confirmLabel: t('orders.wire.reject'),
                      variant: 'danger',
                      onConfirm: () => run(() => rejectWirePayment(orderId), 'orders.wire.rejectError'),
                    })}
                    disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#c62828', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.wire.rejecting') : t('orders.wire.reject')}
                  </button>
                </div>
              </section>
            )}

            {/* Mark as in preparation — US-VND-01 */}
            {order.status === 'AWAITING_PROCESSING' && (
              <section style={{ background: '#e3f2fd', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
                <h2>{t('orders.prepare.title')}</h2>
                <p>{t('orders.prepare.description')}</p>
                {actionError && <p style={{ color: 'red' }}>{actionError}</p>}
                <div style={{ marginTop: '1rem' }}>
                  <button
                    onClick={() => ask({
                      title: t('orders.prepare.title'),
                      message: t('orders.prepare.confirmPrompt'),
                      confirmLabel: t('orders.prepare.submit'),
                      variant: 'primary',
                      onConfirm: () => run(() => markInPreparation(orderId), 'orders.prepare.error'),
                    })}
                    disabled={actionLoading}
                    style={{ padding: '0.5rem 1.2rem', background: '#1565c0', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.prepare.loading') : t('orders.prepare.submit')}
                  </button>
                </div>
              </section>
            )}

            {/* Declare shipment — US-EXP-01 */}
            {order.status === 'IN_PREPARATION' && (
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
                    onClick={() => {
                      if (!trackingInput.trim()) return
                      ask({
                        title: t('orders.ship.title'),
                        message: t('orders.ship.confirmPrompt'),
                        confirmLabel: t('orders.ship.submit'),
                        variant: 'primary',
                        onConfirm: () => run(async () => {
                          const updated = await shipOrder(orderId, trackingInput.trim())
                          setTrackingInput('')
                          return updated
                        }, 'orders.ship.error'),
                      })
                    }}
                    disabled={actionLoading || !trackingInput.trim()}
                    style={{ padding: '0.5rem 1.2rem', background: '#1565c0', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {actionLoading ? t('orders.ship.shipping') : t('orders.ship.submit')}
                  </button>
                </div>
              </section>
            )}

            {/* Post-shipment cancellation — US-CAN-03 / US-CAN-04 */}
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
                    onClick={() => ask({
                      title: t('orders.postShipCancel.acceptReturn'),
                      message: t('orders.postShipCancel.acceptReturnPrompt'),
                      confirmLabel: t('orders.postShipCancel.acceptReturn'),
                      variant: 'danger',
                      onConfirm: () => run(() => acceptReturn(orderId, order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined), 'orders.postShipCancel.error'),
                    })}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#e65100', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.acceptReturn')}
                  </button>
                  <button
                    onClick={() => ask({
                      title: t('orders.postShipCancel.waiveReturn'),
                      message: t('orders.postShipCancel.waiveReturnPrompt'),
                      confirmLabel: t('orders.postShipCancel.waiveReturn'),
                      variant: 'danger',
                      onConfirm: () => run(() => waiveReturn(orderId, order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined), 'orders.postShipCancel.error'),
                    })}
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
                    onClick={() => ask({
                      title: t('orders.postShipCancel.acceptReturn'),
                      message: t('orders.buyerCancelRequest.acceptReturnPrompt'),
                      confirmLabel: t('orders.postShipCancel.acceptReturn'),
                      variant: 'danger',
                      onConfirm: () => {
                        const iban = order.buyerIban ?? (order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        run(() => acceptReturn(orderId, iban), 'orders.postShipCancel.error')
                      },
                    })}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !order.buyerIban && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#e65100', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.acceptReturn')}
                  </button>
                  <button
                    onClick={() => ask({
                      title: t('orders.postShipCancel.waiveReturn'),
                      message: t('orders.buyerCancelRequest.waiveReturnPrompt'),
                      confirmLabel: t('orders.postShipCancel.waiveReturn'),
                      variant: 'danger',
                      onConfirm: () => {
                        const iban = order.buyerIban ?? (order.paymentMethod === 'WIRE_TRANSFER' ? returnIban : undefined)
                        run(() => waiveReturn(orderId, iban), 'orders.postShipCancel.error')
                      },
                    })}
                    disabled={actionLoading || (order.paymentMethod === 'WIRE_TRANSFER' && !order.buyerIban && !returnIban.trim())}
                    style={{ padding: '0.5rem 1.2rem', background: '#c62828', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    {t('orders.postShipCancel.waiveReturn')}
                  </button>
                  <button
                    onClick={() => ask({
                      title: t('orders.buyerCancelRequest.refuse'),
                      message: t('orders.buyerCancelRequest.refusePrompt'),
                      confirmLabel: t('orders.buyerCancelRequest.refuse'),
                      variant: 'primary',
                      onConfirm: () => run(() => refuseCancellationRequest(orderId), 'orders.buyerCancelRequest.refuseError'),
                    })}
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
                  onClick={() => ask({
                    title: t('orders.confirmReturn.title'),
                    message: t('orders.confirmReturn.prompt'),
                    confirmLabel: t('orders.confirmReturn.submit'),
                    variant: 'primary',
                    onConfirm: () => run(() => confirmReturn(orderId), 'orders.confirmReturn.error'),
                  })}
                  disabled={actionLoading}
                  style={{ padding: '0.5rem 1.2rem', background: '#2e7d32', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '1rem' }}>
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
                  onClick={() => ask({
                    title: t('orders.wireRefund.title'),
                    message: t('orders.wireRefund.prompt'),
                    confirmLabel: t('orders.wireRefund.submit'),
                    variant: 'primary',
                    onConfirm: () => run(() => confirmWireRefund(orderId), 'orders.wireRefund.error'),
                  })}
                  disabled={actionLoading}
                  style={{ padding: '0.5rem 1.2rem', background: '#1a237e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '1rem' }}>
                  {t('orders.wireRefund.submit')}
                </button>
              </section>
            )}
          </>
        )}
      </main>
    </Header>
  )
}
