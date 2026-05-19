import { getSession } from './authApi'

const BASE = '/api/cart'

export interface CartItemData {
  id: string
  productId: string
  productName: string
  priceExclTax: number
  priceTTC: number
  photoUrl: string | null
  inStock: boolean
  quantity: number
  lineTotal: number
}

export interface CartData {
  id: string
  buyerId: string
  items: CartItemData[]
  total: number
  updatedAt: string
}

function authHeaders(): Record<string, string> {
  const session = getSession()
  if (!session) throw new Error('NOT_AUTHENTICATED')
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${session.token}` }
}

/** Fetches the authenticated buyer's cart (creates one if none exists). */
export async function getCart(): Promise<CartData> {
  const res = await fetch(BASE, { headers: authHeaders() })
  if (!res.ok) throw new Error('CART_LOAD_ERROR')
  return res.json()
}

/** Adds a product to the cart; increments quantity if already present. */
export async function addToCart(productId: string, quantity: number): Promise<CartData> {
  const res = await fetch(`${BASE}/items`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ productId, quantity }),
  })
  if (res.status === 409) throw new Error('OUT_OF_STOCK')
  if (res.status === 404) throw new Error('PRODUCT_NOT_FOUND')
  if (!res.ok) throw new Error('CART_UPDATE_ERROR')
  return res.json()
}

/** Updates the quantity of an existing cart item. */
export async function updateCartItem(itemId: string, quantity: number): Promise<CartData> {
  const res = await fetch(`${BASE}/items/${itemId}`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify({ quantity }),
  })
  if (res.status === 409) throw new Error('OUT_OF_STOCK')
  if (res.status === 404) throw new Error('CART_ITEM_NOT_FOUND')
  if (!res.ok) throw new Error('CART_UPDATE_ERROR')
  return res.json()
}

/** Removes an item from the cart. */
export async function removeCartItem(itemId: string): Promise<CartData> {
  const res = await fetch(`${BASE}/items/${itemId}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (res.status === 404) throw new Error('CART_ITEM_NOT_FOUND')
  if (!res.ok) throw new Error('CART_UPDATE_ERROR')
  return res.json()
}
