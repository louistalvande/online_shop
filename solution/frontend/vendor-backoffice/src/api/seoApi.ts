import { authedFetch } from './authApi'

export interface ShopSeoConfig {
  seoTitle: string | null
  seoDescription: string | null
  seoKeywords: string | null
  ogImageUrl: string | null
  canonicalUrl: string | null
  robotsDisallowPaths: string | null
  sitemapChangefreq: string
  indexProducts: boolean
  indexCatalog: boolean
  googleVerification: string | null
  ga4Id: string | null
  bingVerification: string | null
}

export interface ProductSeoOverride {
  productId: string
  seoTitle: string | null
  seoDescription: string | null
  seoKeywords: string | null
  ogImageUrl: string | null
}

/** Fetches the shop-wide SEO configuration. */
export async function getShopSeo(): Promise<ShopSeoConfig> {
  const res = await authedFetch('/api/vendor/seo')
  if (!res.ok) throw new Error('Failed to fetch shop SEO')
  return res.json()
}

/** Saves the shop-wide SEO configuration. */
export async function saveShopSeo(payload: Partial<ShopSeoConfig>): Promise<ShopSeoConfig> {
  const res = await authedFetch('/api/vendor/seo', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to save shop SEO')
  return res.json()
}

/** Fetches the SEO override for a specific product. Returns null if none exists. */
export async function getProductSeo(productId: string): Promise<ProductSeoOverride | null> {
  const res = await authedFetch(`/api/vendor/seo/products/${productId}`)
  if (res.status === 404) return null
  if (!res.ok) throw new Error('Failed to fetch product SEO')
  return res.json()
}

/** Saves the SEO override for a specific product. */
export async function saveProductSeo(productId: string, payload: Omit<ProductSeoOverride, 'productId'>): Promise<ProductSeoOverride> {
  const res = await authedFetch(`/api/vendor/seo/products/${productId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to save product SEO')
  return res.json()
}

/** Deletes the SEO override for a specific product. */
export async function deleteProductSeo(productId: string): Promise<void> {
  const res = await authedFetch(`/api/vendor/seo/products/${productId}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete product SEO')
}
