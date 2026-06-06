import { useState, useEffect } from 'react'
import { getShopTheme, getCachedFooterNotice } from '../api/themeApi'

export function useFooterNotice(): string | undefined {
  const [notice, setNotice] = useState<string | undefined>(() => getCachedFooterNotice() ?? undefined)
  useEffect(() => {
    if (getCachedFooterNotice() !== null) return
    getShopTheme()
      .then(t => { if (t.footerNotice) setNotice(t.footerNotice) })
      .catch(() => {})
  }, [])
  return notice
}
