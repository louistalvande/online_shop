export interface ShopTheme {
  shopName: string | null
  accentColor: string | null
  bgColor: string | null
  logoUrl: string | null
  bannerUrl: string | null
  footerNotice: string | null
}

let _inflight: Promise<ShopTheme> | null = null
let _cached: ShopTheme | null = null

const STORAGE_KEY = 'shop_theme_v1'

export async function getShopTheme(): Promise<ShopTheme> {
  if (_cached) return _cached
  if (!_inflight) {
    _inflight = fetch('/api/public/theme')
      .then(res => { if (!res.ok) throw new Error('Failed to fetch shop theme'); return res.json() as Promise<ShopTheme> })
      .then(t => {
        _cached = t
        try { localStorage.setItem(STORAGE_KEY, JSON.stringify(t)) } catch {}
        return t
      })
      .finally(() => { _inflight = null })
  }
  return _inflight
}

export function getCachedShopName(): string | null {
  return _cached?.shopName ?? null
}

export function getCachedLogoUrl(): string | null {
  return _cached?.logoUrl ?? null
}

export function getCachedFooterNotice(): string | null {
  return _cached?.footerNotice ?? null
}
