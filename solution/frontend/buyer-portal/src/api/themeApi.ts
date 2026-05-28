export interface ShopTheme {
  accentColor: string | null
  logoUrl: string | null
  bannerUrl: string | null
}

export async function getShopTheme(): Promise<ShopTheme> {
  const res = await fetch('/api/public/theme')
  if (!res.ok) throw new Error('Failed to fetch shop theme')
  return res.json()
}
