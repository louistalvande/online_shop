export interface BuyerProduct {
  id: string
  name: string
  description: string | null
  priceExclTax: number
  priceTTC: number
  category: string | null
  photoUrls: string[]
  outOfStock: boolean
}

export interface ProductPage {
  content: BuyerProduct[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface CatalogFilters {
  category?: string
  maxPrice?: number
  inStockOnly?: boolean
  search?: string
  page?: number
  size?: number
}

/** Fetches a paginated list of published products with optional filters (US-SHP-01, US-SHP-02). */
export async function fetchProducts(filters: CatalogFilters = {}): Promise<ProductPage> {
  const params = new URLSearchParams()
  if (filters.category) params.set('category', filters.category)
  if (filters.maxPrice != null) params.set('maxPrice', String(filters.maxPrice))
  if (filters.inStockOnly) params.set('inStockOnly', 'true')
  if (filters.search) params.set('search', filters.search)
  params.set('page', String(filters.page ?? 0))
  params.set('size', String(filters.size ?? 20))

  const res = await fetch(`/api/buyer/products?${params}`)
  if (!res.ok) throw new Error('catalog.error.load')
  return res.json()
}

/** Fetches a single published product by ID (US-SHP-01). */
export async function fetchProduct(id: string): Promise<BuyerProduct> {
  const res = await fetch(`/api/buyer/products/${id}`)
  if (!res.ok) throw new Error('catalog.error.load')
  return res.json()
}

/** Returns a sorted deduplicated list of categories from a product page. */
export function extractCategories(products: BuyerProduct[]): string[] {
  const cats = products.map(p => p.category).filter((c): c is string => !!c)
  return [...new Set(cats)].sort()
}
