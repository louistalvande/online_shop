import { getSession } from './authApi'

export interface ShopTheme {
  accentColor: string
  logoUrl: string | null
  bannerUrl: string | null
}

function authHeader(): Record<string, string> {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function getShopTheme(): Promise<ShopTheme> {
  const res = await fetch('/api/public/theme')
  if (!res.ok) throw new Error('Failed to fetch shop theme')
  return res.json()
}

export async function updateShopTheme(payload: { accentColor: string }): Promise<ShopTheme> {
  const res = await fetch('/api/vendor/shop/theme', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update shop theme')
  return res.json()
}
