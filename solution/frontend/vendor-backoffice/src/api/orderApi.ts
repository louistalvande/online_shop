import { authedFetch } from './authApi'

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

export type PaymentMethod = 'CARD' | 'WIRE_TRANSFER'

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

const JSON_HEADERS = { 'Content-Type': 'application/json' }

/** Returns all orders for the authenticated vendor. */
export async function listVendorOrders(): Promise<OrderData[]> {
  const res = await authedFetch('/api/vendor/orders')
  if (!res.ok) throw new Error('Failed to load orders')
  return res.json()
}

/** Returns a single vendor order by ID. */
export async function getVendorOrder(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}`)
  if (res.status === 404) throw Object.assign(new Error('Order not found'), { code: 'NOT_FOUND' })
  if (!res.ok) throw new Error('Failed to load order')
  return res.json()
}

/** Confirms receipt of a wire transfer payment. */
export async function confirmWirePayment(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/confirm-wire`, { method: 'POST' })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to confirm wire payment')
  return res.json()
}

/** Rejects a wire transfer payment and cancels the order. */
export async function rejectWirePayment(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/reject-wire`, { method: 'POST' })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to reject wire payment')
  return res.json()
}

/** Declares order shipment with a carrier tracking number. */
export async function shipOrder(orderId: string, trackingNumber: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/ship`, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ trackingNumber }),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to ship order')
  return res.json()
}

/** Accepts post-shipment cancellation requiring parcel return (US-CAN-03). */
export async function acceptReturn(orderId: string, buyerIban?: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/accept-return`, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ buyerIban: buyerIban ?? null }),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (res.status === 422) throw Object.assign(new Error('Buyer IBAN required'), { code: 'MISSING_IBAN' })
  if (!res.ok) throw new Error('Failed to accept return')
  return res.json()
}

/** Confirms receipt of the returned parcel and triggers refund (US-CAN-03). */
export async function confirmReturn(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/confirm-return`, { method: 'POST' })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to confirm return')
  return res.json()
}

/** Accepts post-shipment cancellation without requiring return (US-CAN-04). */
export async function waiveReturn(orderId: string, buyerIban?: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/waive-return`, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ buyerIban: buyerIban ?? null }),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (res.status === 422) throw Object.assign(new Error('Buyer IBAN required'), { code: 'MISSING_IBAN' })
  if (!res.ok) throw new Error('Failed to waive return')
  return res.json()
}

/** Confirms that the wire transfer refund has been sent to the buyer (US-CAN-05). */
export async function confirmWireRefund(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/confirm-wire-refund`, { method: 'POST' })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to confirm wire refund')
  return res.json()
}

/** Refuses the buyer's post-shipment cancellation request (US-CAN-06). */
export async function refuseCancellationRequest(orderId: string): Promise<OrderData> {
  const res = await authedFetch(`/api/vendor/orders/${orderId}/refuse-cancellation`, { method: 'POST' })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to refuse cancellation request')
  return res.json()
}
