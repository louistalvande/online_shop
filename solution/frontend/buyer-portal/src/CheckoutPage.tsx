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
  listCountries,
  type CountryData,
} from './api/orderApi'
import { getSession } from './api/authApi'
import { listAddresses, createAddress, type DeliveryAddressData, type CreateDeliveryAddressPayload } from './api/profileApi'

type Step = 'address' | 'payment' | 'confirmation'

interface AddressFormState {
  label: string
  recipientName: string
  addressLine: string
  city: string
  postalCode: string
  countryCode: string
  makeDefault: boolean
}

const EMPTY_ADDR_FORM: AddressFormState = {
  label: '', recipientName: '', addressLine: '', city: '', postalCode: '', countryCode: '', makeDefault: false,
}

export default function CheckoutPage() {
  const { t, i18n } = useTranslation()
  const session = getSession()
  const locale = i18n.language

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

  // Add address modal
  const [showAddressModal, setShowAddressModal] = useState(false)
  const [addrForm, setAddrForm] = useState<AddressFormState>(EMPTY_ADDR_FORM)
  const [addrSaving, setAddrSaving] = useState(false)
  const [addrFormError, setAddrFormError] = useState('')
  const [countries, setCountries] = useState<CountryData[]>([])

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

  function openAddressModal() {
    setAddrForm(EMPTY_ADDR_FORM)
    setAddrFormError('')
    if (countries.length === 0) listCountries().then(setCountries).catch(() => {})
    setShowAddressModal(true)
  }

  async function handleCreateAddress(e: React.FormEvent) {
    e.preventDefault()
    setAddrFormError('')
    setAddrSaving(true)
    try {
      const payload: CreateDeliveryAddressPayload = { ...addrForm }
      const created = await createAddress(payload)
      setAddresses(prev => {
        const cleared = payload.makeDefault ? prev.map(a => ({ ...a, default: false })) : prev
        return [...cleared, created]
      })
      setSelectedAddressId(created.id)
      setShowAddressModal(false)
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      setAddrFormError(code === 'INVALID_COUNTRY' ? t('profile.addresses.error.invalidCountry') : t('profile.addresses.error.save'))
    } finally {
      setAddrSaving(false)
    }
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
                <button type="button" onClick={openAddressModal}>
                  {t('checkout.address.addAddress')}
                </button>
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

      {/* ─── ADD ADDRESS MODAL ─── */}
      {showAddressModal && (
        <div className="profile-modal-overlay" role="dialog" aria-modal="true">
          <div className="profile-modal">
            <h2 className="profile-modal-title">{t('checkout.address.addTitle')}</h2>
            <form onSubmit={handleCreateAddress}>
              <div className="profile-field">
                <label htmlFor="addrLabel" className="profile-label">{t('profile.addresses.form.label')}</label>
                <input id="addrLabel" className="profile-input" value={addrForm.label} maxLength={100} required
                  onChange={e => setAddrForm(f => ({ ...f, label: e.target.value }))} />
              </div>
              <div className="profile-field">
                <label htmlFor="addrRecipient" className="profile-label">{t('profile.addresses.form.recipientName')}</label>
                <input id="addrRecipient" className="profile-input" value={addrForm.recipientName} maxLength={100} required
                  onChange={e => setAddrForm(f => ({ ...f, recipientName: e.target.value }))} />
              </div>
              <div className="profile-field">
                <label htmlFor="addrLine" className="profile-label">{t('profile.addresses.form.line')}</label>
                <input id="addrLine" className="profile-input" value={addrForm.addressLine} maxLength={255} required
                  onChange={e => setAddrForm(f => ({ ...f, addressLine: e.target.value }))} />
              </div>
              <div className="profile-row">
                <div className="profile-field">
                  <label htmlFor="addrPostal" className="profile-label">{t('profile.addresses.form.postalCode')}</label>
                  <input id="addrPostal" className="profile-input" value={addrForm.postalCode} maxLength={20} required
                    onChange={e => setAddrForm(f => ({ ...f, postalCode: e.target.value }))} />
                </div>
                <div className="profile-field">
                  <label htmlFor="addrCity" className="profile-label">{t('profile.addresses.form.city')}</label>
                  <input id="addrCity" className="profile-input" value={addrForm.city} maxLength={100} required
                    onChange={e => setAddrForm(f => ({ ...f, city: e.target.value }))} />
                </div>
              </div>
              <div className="profile-field">
                <label htmlFor="addrCountry" className="profile-label">{t('profile.addresses.form.country')}</label>
                <select id="addrCountry" className="profile-input" value={addrForm.countryCode} required
                  onChange={e => setAddrForm(f => ({ ...f, countryCode: e.target.value }))}>
                  <option value="">{t('checkout.address.countryPlaceholder')}</option>
                  {countries.map(c => (
                    <option key={c.code} value={c.code}>
                      {locale === 'en' ? c.nameEn : c.nameFr} ({c.code})
                    </option>
                  ))}
                </select>
              </div>
              <div className="profile-field">
                <label className="profile-checkbox-label">
                  <input type="checkbox" checked={addrForm.makeDefault}
                    onChange={e => setAddrForm(f => ({ ...f, makeDefault: e.target.checked }))} />
                  {' '}{t('profile.addresses.form.makeDefault')}
                </label>
              </div>

              {addrFormError && <p className="profile-alert-error">{addrFormError}</p>}

              <div className="profile-actions">
                <button type="button" onClick={() => setShowAddressModal(false)}>
                  {t('profile.addresses.form.cancel')}
                </button>
                <button type="submit" disabled={addrSaving}>
                  {addrSaving ? t('profile.saving') : t('profile.addresses.form.save')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Header>
  )
}
