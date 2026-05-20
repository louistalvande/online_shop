import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import {
  CarrierData,
  CheckoutInitResponse,
  CountryData,
  OrderData,
  confirmCardPayment,
  initCheckout,
  listCarriers,
  listCountries,
} from './api/orderApi'
import { getSession } from './api/authApi'

type Step = 'address' | 'payment' | 'confirmation'

export default function CheckoutPage() {
  const { t, i18n } = useTranslation()
  const session = getSession()

  // Step state
  const [step, setStep] = useState<Step>('address')

  // Address form
  const [addressLine, setAddressLine] = useState('')
  const [city, setCity] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [countryCode, setCountryCode] = useState('')
  const [carrierId, setCarrierId] = useState('')
  const [countries, setCountries] = useState<CountryData[]>([])
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
    listCountries().then(setCountries).catch(() => {})
  }, [])

  useEffect(() => {
    if (countryCode) {
      listCarriers(countryCode).then(c => {
        setCarriers(c)
        setCarrierId(c[0]?.id ?? '')
      }).catch(() => setCarriers([]))
    } else {
      setCarriers([])
      setCarrierId('')
    }
  }, [countryCode])

  if (!session) {
    return (
      <div>
        <Header />
        <main style={{ maxWidth: 600, margin: '2rem auto', padding: '0 1rem' }}>
          <p>{t('cart.loginRequired')}</p>
        </main>
      </div>
    )
  }

  async function handleAddressSubmit(e: React.FormEvent) {
    e.preventDefault()
    setAddressError('')
    if (!carrierId) {
      setAddressError(t('checkout.error.noCarrier'))
      return
    }
    setStep('payment')
  }

  async function handlePaymentSubmit(e: React.FormEvent) {
    e.preventDefault()
    setPaymentError('')
    setSubmitting(true)
    try {
      const init = await initCheckout({
        deliveryAddressLine: addressLine,
        deliveryCity: city,
        deliveryPostalCode: postalCode,
        deliveryCountryCode: countryCode,
        carrierId,
        paymentMethod,
      })
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

      // Confirm server-side
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
  const locale = i18n.language

  return (
    <div>
      <Header />
      <main style={{ maxWidth: 700, margin: '2rem auto', padding: '0 1rem' }}>
        <h1>{t('checkout.title')}</h1>

        {/* ─── STEP 1: ADDRESS ─── */}
        {step === 'address' && (
          <form onSubmit={handleAddressSubmit}>
            <h2>{t('checkout.address.title')}</h2>

            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="addressLine">{t('checkout.address.line')}</label>
              <input
                id="addressLine"
                type="text"
                value={addressLine}
                onChange={e => setAddressLine(e.target.value)}
                required
                style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
              />
            </div>

            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
              <div style={{ flex: 1 }}>
                <label htmlFor="postalCode">{t('checkout.address.postalCode')}</label>
                <input
                  id="postalCode"
                  type="text"
                  value={postalCode}
                  onChange={e => setPostalCode(e.target.value)}
                  required
                  style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
                />
              </div>
              <div style={{ flex: 2 }}>
                <label htmlFor="city">{t('checkout.address.city')}</label>
                <input
                  id="city"
                  type="text"
                  value={city}
                  onChange={e => setCity(e.target.value)}
                  required
                  style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
                />
              </div>
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <label htmlFor="country">{t('checkout.address.country')}</label>
              <select
                id="country"
                value={countryCode}
                onChange={e => setCountryCode(e.target.value)}
                required
                style={{ display: 'block', width: '100%', marginTop: '0.25rem' }}
              >
                <option value="">{t('checkout.address.countryPlaceholder')}</option>
                {countries.map(c => (
                  <option key={c.code} value={c.code}>
                    {locale === 'en' ? c.nameEn : c.nameFr} ({c.code})
                  </option>
                ))}
              </select>
            </div>

            {countryCode && (
              <div style={{ marginBottom: '1rem' }}>
                <label>{t('checkout.carrier.title')}</label>
                {carriers.length === 0 ? (
                  <p style={{ color: '#e53e3e' }}>{t('checkout.carrier.none')}</p>
                ) : (
                  carriers.map(c => (
                    <label key={c.id} style={{ display: 'block', margin: '0.5rem 0' }}>
                      <input
                        type="radio"
                        name="carrier"
                        value={c.id}
                        checked={carrierId === c.id}
                        onChange={() => setCarrierId(c.id)}
                        style={{ marginRight: '0.5rem' }}
                      />
                      {c.name}
                    </label>
                  ))
                )}
              </div>
            )}

            {addressError && <p style={{ color: '#e53e3e' }}>{addressError}</p>}

            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button type="button" onClick={() => { window.location.href = '/cart' }}>
                {t('checkout.backToCart')}
              </button>
              <button type="submit" disabled={!carrierId}>
                {t('checkout.continueToPayment')}
              </button>
            </div>
          </form>
        )}

        {/* ─── STEP 2: PAYMENT ─── */}
        {step === 'payment' && (
          <form onSubmit={handlePaymentSubmit}>
            <h2>{t('checkout.payment.title')}</h2>

            <div style={{ background: '#f7fafc', padding: '1rem', borderRadius: 8, marginBottom: '1.5rem' }}>
              <h3 style={{ margin: '0 0 0.5rem' }}>{t('checkout.summary.title')}</h3>
              <p style={{ margin: 0 }}>
                {t('checkout.summary.carrier')}: {selectedCarrier?.name}
              </p>
              <p style={{ margin: 0 }}>
                {t('checkout.summary.address')}: {addressLine}, {postalCode} {city}, {countryCode}
              </p>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 600 }}>
                {t('checkout.payment.method')}
              </label>
              <label style={{ display: 'block', marginBottom: '0.5rem' }}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="CARD"
                  checked={paymentMethod === 'CARD'}
                  onChange={() => setPaymentMethod('CARD')}
                  style={{ marginRight: '0.5rem' }}
                />
                {t('checkout.payment.card')}
              </label>
              <label style={{ display: 'block' }}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="WIRE_TRANSFER"
                  checked={paymentMethod === 'WIRE_TRANSFER'}
                  onChange={() => setPaymentMethod('WIRE_TRANSFER')}
                  style={{ marginRight: '0.5rem' }}
                />
                {t('checkout.payment.wire')}
              </label>
            </div>

            {paymentMethod === 'WIRE_TRANSFER' && (
              <p style={{ color: '#718096', fontStyle: 'italic', marginBottom: '1rem' }}>
                {t('checkout.payment.wireNote')}
              </p>
            )}

            {paymentError && <p style={{ color: '#e53e3e' }}>{paymentError}</p>}

            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
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
            <p style={{ color: '#38a169', fontWeight: 600 }}>
              {t('checkout.confirmation.orderNumber')}: {checkoutInit.orderNumber}
            </p>

            {paymentMethod === 'CARD' && confirmedOrder && (
              <p>{t('checkout.confirmation.cardSuccess')}</p>
            )}

            {paymentMethod === 'WIRE_TRANSFER' && (
              <div style={{ background: '#fff3cd', padding: '1rem', borderRadius: 8, marginBottom: '1rem' }}>
                <h3>{t('checkout.confirmation.wireTitle')}</h3>
                <p><strong>{t('checkout.confirmation.wireIban')}:</strong> {checkoutInit.bankIban}</p>
                <p><strong>{t('checkout.confirmation.wireBic')}:</strong> {checkoutInit.bankBic}</p>
                <p><strong>{t('checkout.confirmation.wireReference')}:</strong> {checkoutInit.paymentReference}</p>
                <p><strong>{t('checkout.confirmation.wireAmount')}:</strong> {checkoutInit.totalAmountTtc?.toFixed(2)} €</p>
                <p style={{ fontStyle: 'italic', color: '#718096' }}>{t('checkout.confirmation.wireNote')}</p>
              </div>
            )}

            <p>{t('checkout.confirmation.emailSent')}</p>

            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button onClick={() => { window.location.href = '/catalog' }}>
                {t('checkout.confirmation.backToCatalog')}
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
