export interface ShopSeoConfig {
  seoTitle: string | null
  seoDescription: string | null
  seoKeywords: string | null
  ogImageUrl: string | null
  canonicalUrl: string | null
  indexProducts: boolean
  indexCatalog: boolean
  indexAccount: boolean
  indexCart: boolean
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

/** In-flight promise shared across all callers within the same page load. */
let shopSeoPromise: Promise<ShopSeoConfig> | null = null

/** Fetches the shop-wide SEO configuration. Multiple callers share a single in-flight request. */
export function getShopSeo(): Promise<ShopSeoConfig> {
  if (!shopSeoPromise) {
    shopSeoPromise = fetch('/api/public/seo')
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch shop SEO')
        return res.json() as Promise<ShopSeoConfig>
      })
      .catch(err => {
        shopSeoPromise = null
        throw err
      })
  }
  return shopSeoPromise
}

/**
 * Fetches the per-product SEO override.
 * Returns null when no override exists (HTTP 204).
 */
export async function getProductSeo(productId: string): Promise<ProductSeoOverride | null> {
  const res = await fetch(`/api/public/seo/products/${productId}`)
  if (res.status === 204) return null
  if (!res.ok) throw new Error('Failed to fetch product SEO')
  return res.json()
}
