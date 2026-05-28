import { useState, useEffect } from 'react'
import { getCart } from '../api/cartApi'
import { getSession } from '../api/authApi'

/** Returns the total item quantity in the buyer's cart and refreshes on 'cart-updated' events. */
export function useCartCount(): number {
  const [count, setCount] = useState(0)

  async function refresh() {
    if (!getSession()) { setCount(0); return }
    try {
      const cart = await getCart()
      setCount(cart.items.reduce((sum, item) => sum + item.quantity, 0))
    } catch {
      setCount(0)
    }
  }

  useEffect(() => {
    refresh()
    window.addEventListener('cart-updated', refresh)
    window.addEventListener('session-changed', refresh)
    return () => {
      window.removeEventListener('cart-updated', refresh)
      window.removeEventListener('session-changed', refresh)
    }
  }, [])

  return count
}
