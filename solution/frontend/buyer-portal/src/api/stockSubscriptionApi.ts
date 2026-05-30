import { authedFetch } from './authApi'

export interface StockSubscription {
  id: string
  productId: string
  productName: string
  photoUrl: string | null
  priceTTC: number
  createdAt: string
}

/** Subscribes the authenticated buyer to a back-in-stock alert (US-SHP-03). */
export async function subscribeToRestock(productId: string): Promise<StockSubscription> {
  const res = await authedFetch('/api/profile/stock-subscriptions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId }),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    const err = new Error(body.message ?? 'stock.notify.error') as Error & { code?: string }
    err.code = body.error
    throw err
  }
  return res.json()
}

/** Cancels the buyer's active subscription for a product (US-SHP-03). */
export async function unsubscribeFromRestock(productId: string): Promise<void> {
  const res = await authedFetch(`/api/profile/stock-subscriptions/${productId}`, {
    method: 'DELETE',
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    const err = new Error(body.message ?? 'stock.notify.error') as Error & { code?: string }
    err.code = body.error
    throw err
  }
}

/** Lists the buyer's active back-in-stock subscriptions (US-SHP-03). */
export async function listSubscriptions(): Promise<StockSubscription[]> {
  const res = await authedFetch('/api/profile/stock-subscriptions')
  if (!res.ok) throw new Error('stock.notify.error')
  return res.json()
}
