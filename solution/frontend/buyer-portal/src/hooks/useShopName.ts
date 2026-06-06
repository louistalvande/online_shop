import { useState, useEffect } from 'react'
import { getShopTheme, getCachedShopName } from '../api/themeApi'

const DEFAULT = ''

/** Returns the shop name from platform settings, falling back to the default while loading. */
export function useShopName(): string {
  const [name, setName] = useState(() => getCachedShopName() ?? DEFAULT)

  useEffect(() => {
    if (getCachedShopName() !== null) return
    getShopTheme()
      .then(t => { if (t.shopName) setName(t.shopName) })
      .catch(() => {})
  }, [])

  return name
}
