export interface ShopTheme {
  shopName: string | null
  accentColor: string | null
  bgColor: string | null
  logoUrl: string | null
  bannerUrl: string | null
}

let _inflight: Promise<ShopTheme> | null = null
let _cached: ShopTheme | null = null

export async function getShopTheme(): Promise<ShopTheme> {
  if (_cached) return _cached
  if (!_inflight) {
    _inflight = fetch('/api/public/theme')
      .then(res => { if (!res.ok) throw new Error('Failed to fetch shop theme'); return res.json() as Promise<ShopTheme> })
      .then(t => { _cached = t; return t })
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
