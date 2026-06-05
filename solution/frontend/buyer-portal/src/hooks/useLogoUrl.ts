import { useState, useEffect } from 'react'
import { getShopTheme, getCachedLogoUrl } from '../api/themeApi'

export function useLogoUrl(): string | undefined {
  const [url, setUrl] = useState<string | undefined>(() => getCachedLogoUrl() ?? undefined)

  useEffect(() => {
    if (getCachedLogoUrl() !== null) return
    getShopTheme()
      .then(t => { if (t.logoUrl) setUrl(t.logoUrl) })
      .catch(() => {})
  }, [])

  return url
}
