import { getSession } from './authApi'

const BASE = '/api/orders'

export type PaymentMethod = 'CARD' | 'WIRE_TRANSFER'

export interface CreateOrderRequest {
  deliveryAddressLine: string
  deliveryCity: string
  deliveryPostalCode: string
  deliveryCountryCode: string
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

function authHeaders(): Record<string, string> {
  const session = getSession()
  if (!session) throw new Error('NOT_AUTHENTICATED')
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${session.token}` }
}

/** Creates an order from the buyer's cart and initiates payment. */
export async function initCheckout(req: CreateOrderRequest): Promise<CheckoutInitResponse> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: authHeaders(),
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
  const res = await fetch(`${BASE}/${orderId}/confirm-payment`, {
    method: 'POST',
    headers: authHeaders(),
  })
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
  const res = await fetch(BASE, { headers: authHeaders() })
  if (!res.ok) throw new Error('ORDERS_LOAD_ERROR')
  return res.json()
}

/** Returns a single order for the authenticated buyer. */
export async function getMyOrder(orderId: string): Promise<OrderData> {
  const res = await fetch(`${BASE}/${orderId}`, { headers: authHeaders() })
  if (res.status === 404) throw new Error('ORDER_NOT_FOUND')
  if (!res.ok) throw new Error('ORDER_LOAD_ERROR')
  return res.json()
}
