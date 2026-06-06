import { authedFetch } from './authApi'

export interface ShopTheme {
  shopName: string | null
  accentColor: string
  bgColor: string
  logoUrl: string | null
  bannerUrl: string | null
  footerNotice: string | null
}

export async function getShopTheme(): Promise<ShopTheme> {
  const res = await fetch('/api/public/theme')
  if (!res.ok) throw new Error('Failed to fetch shop theme')
  return res.json()
}

export async function updateShopTheme(payload: { shopName?: string; accentColor?: string; bgColor?: string }): Promise<ShopTheme> {
  const res = await authedFetch('/api/vendor/shop/theme', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update shop theme')
  return res.json()
}

export interface LegalPage {
  key: string
  content: string
}

export async function getLegalPage(key: string): Promise<LegalPage> {
  const res = await fetch(`/api/public/legal/${key}`)
  if (!res.ok) throw new Error('Failed to fetch legal page')
  return res.json()
}

export async function updateLegalPage(key: string, content: string): Promise<LegalPage> {
  const res = await authedFetch(`/api/vendor/legal/${key}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  })
  if (!res.ok) throw new Error('Failed to update legal page')
  return res.json()
}
