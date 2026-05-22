import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import {
  CarrierData,
  CheckoutInitResponse,
  OrderData,
  confirmCardPayment,
  initCheckout,
  listCarriers,
} from './api/orderApi'
import { getSession } from './api/authApi'
import { listAddresses, type DeliveryAddressData } from './api/profileApi'

type Step = 'address' | 'payment' | 'confirmation'

export default function CheckoutPage() {
  const { t } = useTranslation()
  const session = getSession()

  const [step, setStep] = useState<Step>('address')

  // Address picker
  const [addresses, setAddresses] = useState<DeliveryAddressData[]>([])
  const [addrLoading, setAddrLoading] = useState(true)
  const [selectedAddressId, setSelectedAddressId] = useState('')

  // Carrier selection
  const [carrierId, setCarrierId] = useState('')
  const [carriers, setCarriers] = useState<CarrierData[]>([])

  // Payment
  const [paymentMethod, setPaymentMethod] = useState<'CARD' | 'WIRE_TRANSFER'>('CARD')
  const [checkoutInit, setCheckoutInit] = useState<CheckoutInitResponse | null>(null)
  const [confirmedOrder, setConfirmedOrder] = useState<OrderData | null>(null)

  // UI state
  const [addressError, setAddressError] = useState('')
  const [paymentError, setPaymentError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!session) return
    listAddresses()
      .then(list => {
        setAddresses(list)
        const def = list.find(a => a.default) ?? list[0]
        if (def) setSelectedAddressId(def.id)
      })
      .catch(() => {})
      .finally(() => setAddrLoading(false))
  }, [])

  const selectedAddress = addresses.find(a => a.id === selectedAddressId)

  useEffect(() => {
    const country = selectedAddress?.countryCode ?? ''
    if (country) {
      listCarriers(country)
        .then(c => { setCarriers(c); setCarrierId(c[0]?.id ?? '') })
        .catch(() => setCarriers([]))
    } else {
      setCarriers([])
      setCarrierId('')
    }
  }, [selectedAddress?.countryCode])

  if (!session) {
    return (
      <Header session={null} onShowLogin={() => {}} onLogout={() => {}}>
        <div className="checkout-login-required">
          <p>{t('cart.loginRequired')}</p>
        </div>
      </Header>
    )
  }

  function handleAddressSubmit(e: React.FormEvent) {
    e.preventDefault()
    setAddressError('')
    if (!selectedAddressId) { setAddressError(t('checkout.error.noAddress')); return }
    if (!carrierId) { setAddressError(t('checkout.error.noCarrier')); return }
    setStep('payment')
  }

  async function handlePaymentSubmit(e: React.FormEvent) {
    e.preventDefault()
    setPaymentError('')
    setSubmitting(true)
    try {
      const init = await initCheckout({ addressId: selectedAddressId, carrierId, paymentMethod })
      setCheckoutInit(init)

      if (paymentMethod === 'WIRE_TRANSFER') {
        setStep('confirmation')
        return
      }

      // CARD: try Stripe.js if available, then confirm server-side
      const stripeKey = (window as any).__STRIPE_PUBLIC_KEY__
      if (stripeKey && init.clientSecret && (window as any).Stripe) {
        const stripe = (window as any).Stripe(stripeKey)
        const { error } = await stripe.confirmCardPayment(init.clientSecret)
        if (error) {
          setPaymentError(error.message ?? t('checkout.error.paymentFailed'))
          setSubmitting(false)
          return
        }
      }

      const order = await confirmCardPayment(init.orderId)
      setConfirmedOrder(order)
      setStep('confirmation')
    } catch (err: any) {
      if (err.message === 'EMPTY_CART') setPaymentError(t('checkout.error.emptyCart'))
      else if (err.message === 'INVALID_COUNTRY') setPaymentError(t('checkout.error.invalidCountry'))
      else if (err.message === 'CARRIER_NOT_AVAILABLE') setPaymentError(t('checkout.error.carrierNotAvailable'))
      else if (err.message === 'PAYMENT_FAILED') setPaymentError(t('checkout.error.paymentFailed'))
      else setPaymentError(t('checkout.error.generic'))
    } finally {
      setSubmitting(false)
    }
  }

  const selectedCarrier = carriers.find(c => c.id === carrierId)

  return (
    <Header session={session} onShowLogin={() => {}} onLogout={() => { window.location.href = '/' }}>
      <div className="checkout-main">
        <h1>{t('checkout.title')}</h1>

        {/* ─── STEP 1: ADDRESS & CARRIER ─── */}
        {step === 'address' && (
          <form onSubmit={handleAddressSubmit}>
            <h2>{t('checkout.address.title')}</h2>

            {addrLoading ? (
              <p>{t('orders.loading')}</p>
            ) : addresses.length === 0 ? (
              <div className="checkout-no-addresses">
                <p className="checkout-error">{t('checkout.address.noAddresses')}</p>
                <a href="/profile#addresses">{t('checkout.address.manageLink')}</a>
              </div>
            ) : (
              <div className="checkout-field">
                {addresses.map(addr => (
                  <label key={addr.id} className="checkout-carrier-label">
                    <input
                      type="radio"
                      name="address"
                      value={addr.id}
                      checked={selectedAddressId === addr.id}
                      onChange={() => setSelectedAddressId(addr.id)}
                      className="checkout-radio"
                    />
                    <span>
                      <strong>{addr.label}</strong>
                      {addr.default && <span className="checkout-address-default"> ★</span>}
                      {' — '}{addr.addressLine}, {addr.postalCode} {addr.city}, {addr.countryCode}
                    </span>
                  </label>
                ))}
              </div>
            )}

            {selectedAddressId && (
              <div className="checkout-field">
                <label>{t('checkout.carrier.title')}</label>
                {carriers.length === 0 ? (
                  <p className="checkout-error">{t('checkout.carrier.none')}</p>
                ) : (
                  carriers.map(c => (
                    <label key={c.id} className="checkout-carrier-label">
                      <input
                        type="radio"
                        name="carrier"
                        value={c.id}
                        checked={carrierId === c.id}
                        onChange={() => setCarrierId(c.id)}
                        className="checkout-radio"
                      />
                      {c.name}
                    </label>
                  ))
                )}
              </div>
            )}

            {addressError && <p className="checkout-error">{addressError}</p>}

            <div className="checkout-actions">
              <button type="button" onClick={() => { window.location.href = '/cart' }}>
                {t('checkout.backToCart')}
              </button>
              <button type="submit" disabled={!selectedAddressId || !carrierId}>
                {t('checkout.continueToPayment')}
              </button>
            </div>
          </form>
        )}

        {/* ─── STEP 2: PAYMENT ─── */}
        {step === 'payment' && (
          <form onSubmit={handlePaymentSubmit}>
            <h2>{t('checkout.payment.title')}</h2>

            <div className="checkout-summary">
              <h3 className="checkout-summary-title">{t('checkout.summary.title')}</h3>
              <p className="checkout-summary-text">
                {t('checkout.summary.carrier')}: {selectedCarrier?.name}
              </p>
              <p className="checkout-summary-text">
                {t('checkout.summary.address')}: {selectedAddress?.label} — {selectedAddress?.addressLine}, {selectedAddress?.postalCode} {selectedAddress?.city}, {selectedAddress?.countryCode}
              </p>
            </div>

            <div className="checkout-payment-label-group">
              <label className="checkout-payment-method-heading">
                {t('checkout.payment.method')}
              </label>
              <label className="checkout-payment-method-option">
                <input
                  type="radio"
                  name="paymentMethod"
                  value="CARD"
                  checked={paymentMethod === 'CARD'}
                  onChange={() => setPaymentMethod('CARD')}
                  className="checkout-radio"
                />
                {t('checkout.payment.card')}
              </label>
              <label className="checkout-payment-method-option">
                <input
                  type="radio"
                  name="paymentMethod"
                  value="WIRE_TRANSFER"
                  checked={paymentMethod === 'WIRE_TRANSFER'}
                  onChange={() => setPaymentMethod('WIRE_TRANSFER')}
                  className="checkout-radio"
                />
                {t('checkout.payment.wire')}
              </label>
            </div>

            {paymentMethod === 'WIRE_TRANSFER' && (
              <p className="checkout-wire-note">{t('checkout.payment.wireNote')}</p>
            )}

            {paymentError && <p className="checkout-error">{paymentError}</p>}

            <div className="checkout-actions">
              <button type="button" onClick={() => setStep('address')}>
                {t('checkout.back')}
              </button>
              <button type="submit" disabled={submitting}>
                {submitting ? t('checkout.payment.processing') : t('checkout.payment.confirm')}
              </button>
            </div>
          </form>
        )}

        {/* ─── STEP 3: CONFIRMATION ─── */}
        {step === 'confirmation' && checkoutInit && (
          <div>
            <h2>{t('checkout.confirmation.title')}</h2>
            <p className="checkout-confirmation-order">
              {t('checkout.confirmation.orderNumber')}: {checkoutInit.orderNumber}
            </p>

            {paymentMethod === 'CARD' && confirmedOrder && (
              <p>{t('checkout.confirmation.cardSuccess')}</p>
            )}

            {paymentMethod === 'WIRE_TRANSFER' && (
              <div className="checkout-wire-info">
                <h3>{t('checkout.confirmation.wireTitle')}</h3>
                <p><strong>{t('checkout.confirmation.wireIban')}:</strong> {checkoutInit.bankIban}</p>
                <p><strong>{t('checkout.confirmation.wireBic')}:</strong> {checkoutInit.bankBic}</p>
                <p><strong>{t('checkout.confirmation.wireReference')}:</strong> {checkoutInit.paymentReference}</p>
                <p><strong>{t('checkout.confirmation.wireAmount')}:</strong> {checkoutInit.totalAmountTtc?.toFixed(2)} €</p>
                <p className="checkout-wire-footer">{t('checkout.confirmation.wireNote')}</p>
              </div>
            )}

            <p>{t('checkout.confirmation.emailSent')}</p>

            <div className="checkout-actions">
              <button onClick={() => { window.location.href = '/catalog' }}>
                {t('checkout.confirmation.backToCatalog')}
              </button>
            </div>
          </div>
        )}
      </div>
    </Header>
  )
}
