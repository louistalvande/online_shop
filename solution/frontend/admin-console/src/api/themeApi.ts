export interface ShopTheme {
  logoUrl: string | null
  bannerUrl: string | null
  accentColor: string
  bgColor: string
}

/** Fetches the public shop theme (no auth required). */
export async function getShopTheme(): Promise<ShopTheme> {
  const res = await fetch('/api/public/theme')
  if (!res.ok) throw new Error('theme_load_failed')
  return res.json()
}
