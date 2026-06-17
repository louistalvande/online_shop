import { authedFetch } from './authApi'

export interface Product {
  id: string
  slug: string
  vendorId: string
  name: string
  description: string | null
  priceExclTax: number
  category: string | null
  theme: string | null
  quantity: number
  stockAlertThreshold: number
  status: 'PUBLISHED' | 'ARCHIVED'
  photoUrls: string[]
  outOfStock: boolean
  belowThreshold: boolean
  createdAt: string
  updatedAt: string
}

export interface StockAlert {
  id: string
  productId: string
  productName: string
  quantity: number
  stockAlertThreshold: number
  triggeredAt: string
  acknowledged: boolean
}

export interface CreateProductPayload {
  name: string
  description?: string
  priceExclTax: number
  category?: string
  theme?: string
  quantity: number
  stockAlertThreshold: number
  photoUrls: string[]
}

export interface UpdateStockPayload {
  quantity: number
  stockAlertThreshold: number
}

export interface BulkStockUpdateItem {
  productId: string
  quantity: number
  stockAlertThreshold: number
}

export interface BulkStockUpdatePayload {
  updates: BulkStockUpdateItem[]
}

export interface BulkStockUpdateResult {
  productId: string
  status: 'UPDATED' | 'ERROR'
  message: string | null
  product: Product | null
}

export interface BulkStockUpdateResponse {
  totalUpdated: number
  totalErrors: number
  results: BulkStockUpdateResult[]
}

export interface CsvImportRowResult {
  lineNumber: number
  status: 'CREATED' | 'UPDATED' | 'ERROR'
  message: string | null
  product: Product | null
}

export interface CsvImportResponse {
  rows: CsvImportRowResult[]
  totalCreated: number
  totalUpdated: number
  totalErrors: number
}

const JSON_HEADERS = { 'Content-Type': 'application/json' }

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw Object.assign(new Error(body.message ?? 'Request failed'), { status: res.status, code: body.error })
  }
  return res.json()
}

/** Fetches all products for the authenticated vendor. */
export async function listProducts(): Promise<Product[]> {
  return handleResponse<Product[]>(await authedFetch('/api/vendor/products'))
}

/** Fetches a single product by ID for the authenticated vendor. */
export async function getProduct(id: string): Promise<Product> {
  return handleResponse<Product>(await authedFetch(`/api/vendor/products/${id}`))
}

/** Creates a new product. */
export async function createProduct(payload: CreateProductPayload): Promise<Product> {
  return handleResponse<Product>(await authedFetch('/api/vendor/products', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  }))
}

/** Updates an existing product. */
export async function updateProduct(id: string, payload: CreateProductPayload): Promise<Product> {
  return handleResponse<Product>(await authedFetch(`/api/vendor/products/${id}`, {
    method: 'PUT',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  }))
}

/** Archives a product. */
export async function archiveProduct(id: string): Promise<Product> {
  return handleResponse<Product>(await authedFetch(`/api/vendor/products/${id}/archive`, { method: 'PATCH' }))
}

/** Updates stock quantity and alert threshold for a product. */
export async function updateStock(id: string, payload: UpdateStockPayload): Promise<Product> {
  return handleResponse<Product>(await authedFetch(`/api/vendor/products/${id}/stock`, {
    method: 'PATCH',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  }))
}

/** Fetches all unacknowledged stock alerts for the vendor. */
export async function listPendingAlerts(): Promise<StockAlert[]> {
  return handleResponse<StockAlert[]>(await authedFetch('/api/vendor/alerts'))
}

/** Acknowledges a stock alert. */
export async function acknowledgeAlert(alertId: string): Promise<StockAlert> {
  return handleResponse<StockAlert>(await authedFetch(`/api/vendor/alerts/${alertId}/acknowledge`, { method: 'PATCH' }))
}

/** Bulk-updates stock quantity and alert threshold for multiple products (US-CAT-08). */
export async function bulkUpdateStocks(payload: BulkStockUpdatePayload): Promise<BulkStockUpdateResponse> {
  return handleResponse<BulkStockUpdateResponse>(await authedFetch('/api/vendor/products/stocks', {
    method: 'PATCH',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  }))
}

/** Exports all vendor products as a UTF-8 CSV file and triggers a browser download (US-CAT-07). */
export async function exportProductsCsv(): Promise<void> {
  const res = await authedFetch('/api/vendor/products/export')
  if (!res.ok) throw new Error('EXPORT_ERROR')
  const blob = await res.blob()
  const disposition = res.headers.get('Content-Disposition') ?? ''
  const match = disposition.match(/filename="([^"]+)"/)
  const filename = match ? match[1] : 'catalogue_export.csv'
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

/** Imports products from a CSV file (US-CAT-06). */
export async function importProductsCsv(file: File): Promise<CsvImportResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await authedFetch('/api/vendor/products/import', { method: 'POST', body: formData })
  if (res.status === 400) throw Object.assign(new Error('CSV_HEADER_INVALID'), { code: 'CSV_HEADER_INVALID' })
  return handleResponse<CsvImportResponse>(res)
}

/** Returns existing distinct product types for combobox autocompletion (US-CAT-01). */
export async function fetchDistinctTypes(): Promise<string[]> {
  return handleResponse<string[]>(await authedFetch('/api/vendor/products/distinct-types'))
}

/** Returns existing distinct product themes for combobox autocompletion (US-CAT-01). */
export async function fetchDistinctThemes(): Promise<string[]> {
  return handleResponse<string[]>(await authedFetch('/api/vendor/products/distinct-themes'))
}

/** Uploads a product image file and returns the public URL assigned by the server (US-CAT-09). */
export async function uploadProductImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const data = await handleResponse<{ imageUrl: string }>(
    await authedFetch('/api/vendor/products/images', { method: 'POST', body: formData })
  )
  return data.imageUrl
}
