import { authedFetch } from './authApi'

const BASE = '/api/orders'

export type PaymentMethod = 'CARD' | 'WIRE_TRANSFER'

export interface CreateOrderRequest {
  addressId: string
  carrierId: string
  paymentMethod: PaymentMethod
}

export interface CheckoutInitResponse {
  orderId: string
  orderNumber: string
  paymentMethod: PaymentMethod
  totalAmountTtc: number
  /** Present for CARD payments — pass to Stripe.js */
  clientSecret?: string
  /** Present for WIRE_TRANSFER payments */
  bankIban?: string
  bankBic?: string
  paymentReference?: string
}

export type OrderStatus =
  | 'PAYMENT_PENDING_CARD'
  | 'PAYMENT_PENDING_WIRE'
  | 'AWAITING_PROCESSING'
  | 'IN_PREPARATION'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'PENDING_RETURN'
  | 'WIRE_REFUND_IN_PROGRESS'
  | 'CANCELLATION_REQUESTED_AFTER_SHIPMENT'

export interface OrderLineData {
  id: string
  productId: string | null
  productName: string
  unitPriceExclTax: number
  unitPriceTtc: number
  quantity: number
  lineTotalTtc: number
}

export interface OrderData {
  id: string
  orderNumber: string
  buyerId: string
  carrierId: string
  carrierName: string
  carrierTrackingUrl: string
  deliveryAddressLine: string
  deliveryCity: string
  deliveryPostalCode: string
  deliveryCountryCode: string
  paymentMethod: PaymentMethod
  status: OrderStatus
  totalAmountTtc: number
  trackingNumber: string | null
  buyerIban: string | null
  cancellationReason: string | null
  lines: OrderLineData[]
  createdAt: string
  updatedAt: string
}

export interface CarrierData {
  id: string
  name: string
  trackingUrl: string
  active: boolean
  supportedCountries: string[]
}

export interface CountryData {
  code: string
  nameFr: string
  nameEn: string
}

/** Creates an order from the buyer's cart and initiates payment. */
export async function initCheckout(req: CreateOrderRequest): Promise<CheckoutInitResponse> {
  const res = await authedFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (res.status === 400) throw new Error('EMPTY_CART')
  if (res.status === 422) throw new Error('INVALID_COUNTRY')
  if (res.status === 409) throw new Error('CARRIER_NOT_AVAILABLE')
  if (!res.ok) throw new Error('CHECKOUT_ERROR')
  return res.json()
}

/** Confirms that the Stripe card payment succeeded (call after Stripe.js confirmCardPayment). */
export async function confirmCardPayment(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`${BASE}/${orderId}/confirm-payment`, { method: 'POST' })
  if (res.status === 404) throw new Error('ORDER_NOT_FOUND')
  if (res.status === 402) throw new Error('PAYMENT_FAILED')
  if (res.status === 409) throw new Error('INVALID_ORDER_STATE')
  if (!res.ok) throw new Error('CONFIRM_ERROR')
  return res.json()
}

/** Lists active carriers for a given delivery country. */
export async function listCarriers(countryCode: string): Promise<CarrierData[]> {
  const res = await fetch(`/api/carriers?countryCode=${encodeURIComponent(countryCode)}`)
  if (!res.ok) throw new Error('CARRIERS_LOAD_ERROR')
  return res.json()
}

/** Lists Eurozone countries available for delivery. */
export async function listCountries(): Promise<CountryData[]> {
  const res = await fetch('/api/countries')
  if (!res.ok) throw new Error('COUNTRIES_LOAD_ERROR')
  return res.json()
}

/** Returns all orders for the authenticated buyer. */
export async function getMyOrders(): Promise<OrderData[]> {
  const res = await authedFetch(BASE)
  if (!res.ok) throw new Error('ORDERS_LOAD_ERROR')
  return res.json()
}

/** Returns a single order for the authenticated buyer. */
export async function getMyOrder(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`${BASE}/${orderId}`)
  if (res.status === 404) throw new Error('ORDER_NOT_FOUND')
  if (!res.ok) throw new Error('ORDER_LOAD_ERROR')
  return res.json()
}

/** Requests post-shipment cancellation (US-CAN-06). Pass buyerIban for wire transfer orders. */
export async function requestPostShipmentCancellation(
  orderId: string,
  reason: string,
  buyerIban?: string
): Promise<OrderData> {
  const res = await authedFetch(`${BASE}/${orderId}/request-post-shipment-cancellation`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ reason, buyerIban: buyerIban ?? null }),
  })
  if (res.status === 404) throw new Error('ORDER_NOT_FOUND')
  if (res.status === 409) throw new Error('INVALID_ORDER_STATE')
  if (res.status === 422) throw new Error('MISSING_BUYER_IBAN')
  if (!res.ok) throw new Error('CANCELLATION_REQUEST_ERROR')
  return res.json()
}

/** Cancels an order placed by the buyer (US-CAN-01). Pass buyerIban for wire transfer orders. */
export async function cancelOrder(orderId: string, buyerIban?: string): Promise<OrderData> {
  const res = await authedFetch(`${BASE}/${orderId}/cancel`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ buyerIban: buyerIban ?? null }),
  })
  if (res.status === 404) throw new Error('ORDER_NOT_FOUND')
  if (res.status === 409) throw new Error('INVALID_ORDER_STATE')
  if (res.status === 422) throw new Error('MISSING_BUYER_IBAN')
  if (!res.ok) throw new Error('CANCEL_ERROR')
  return res.json()
}
