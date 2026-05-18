import { getSession } from './authApi'

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
  lines: OrderLineData[]
  createdAt: string
  updatedAt: string
}

function authHeaders(): Record<string, string> {
  const session = getSession()
  if (!session) throw new Error('Not authenticated')
  return { Authorization: `Bearer ${session.token}`, 'Content-Type': 'application/json' }
}

/** Returns all orders for the authenticated vendor. */
export async function listVendorOrders(): Promise<OrderData[]> {
  const res = await fetch('/api/vendor/orders', { headers: authHeaders() })
  if (!res.ok) throw new Error('Failed to load orders')
  return res.json()
}

/** Returns a single vendor order by ID. */
export async function getVendorOrder(orderId: string): Promise<OrderData> {
  const res = await fetch(`/api/vendor/orders/${orderId}`, { headers: authHeaders() })
  if (res.status === 404) throw Object.assign(new Error('Order not found'), { code: 'NOT_FOUND' })
  if (!res.ok) throw new Error('Failed to load order')
  return res.json()
}

/** Confirms receipt of a wire transfer payment. */
export async function confirmWirePayment(orderId: string): Promise<OrderData> {
  const res = await fetch(`/api/vendor/orders/${orderId}/confirm-wire`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to confirm wire payment')
  return res.json()
}

/** Rejects a wire transfer payment and cancels the order. */
export async function rejectWirePayment(orderId: string): Promise<OrderData> {
  const res = await fetch(`/api/vendor/orders/${orderId}/reject-wire`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to reject wire payment')
  return res.json()
}

/** Declares order shipment with a carrier tracking number. */
export async function shipOrder(orderId: string, trackingNumber: string): Promise<OrderData> {
  const res = await fetch(`/api/vendor/orders/${orderId}/ship`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ trackingNumber }),
  })
  if (res.status === 409) throw Object.assign(new Error('Invalid order state'), { code: 'INVALID_STATE' })
  if (!res.ok) throw new Error('Failed to ship order')
  return res.json()
}
