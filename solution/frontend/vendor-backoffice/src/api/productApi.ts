import { getSession } from './authApi'

export interface Product {
  id: string
  vendorId: string
  name: string
  description: string | null
  priceExclTax: number
  category: string | null
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
  quantity: number
  stockAlertThreshold: number
  photoUrls: string[]
}

export interface UpdateStockPayload {
  quantity: number
  stockAlertThreshold: number
}

export interface CsvImportRowResult {
  lineNumber: number
  status: 'CREATED' | 'ERROR'
  message: string | null
  product: Product | null
}

export interface CsvImportResponse {
  rows: CsvImportRowResult[]
  totalCreated: number
  totalErrors: number
}

function authHeaders(): Record<string, string> {
  const session = getSession()
  return {
    'Content-Type': 'application/json',
    ...(session ? { Authorization: `Bearer ${session.token}` } : {}),
  }
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw Object.assign(new Error(body.message ?? 'Request failed'), { status: res.status, code: body.error })
  }
  return res.json()
}

/** Fetches all products for the authenticated vendor. */
export async function listProducts(): Promise<Product[]> {
  const res = await fetch('/api/vendor/products', { headers: authHeaders() })
  return handleResponse<Product[]>(res)
}

/** Creates a new product. */
export async function createProduct(payload: CreateProductPayload): Promise<Product> {
  const res = await fetch('/api/vendor/products', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  return handleResponse<Product>(res)
}

/** Updates an existing product. */
export async function updateProduct(id: string, payload: CreateProductPayload): Promise<Product> {
  const res = await fetch(`/api/vendor/products/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  return handleResponse<Product>(res)
}

/** Archives a product. */
export async function archiveProduct(id: string): Promise<Product> {
  const res = await fetch(`/api/vendor/products/${id}/archive`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<Product>(res)
}

/** Updates stock quantity and alert threshold for a product. */
export async function updateStock(id: string, payload: UpdateStockPayload): Promise<Product> {
  const res = await fetch(`/api/vendor/products/${id}/stock`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  return handleResponse<Product>(res)
}

/** Fetches all unacknowledged stock alerts for the vendor. */
export async function listPendingAlerts(): Promise<StockAlert[]> {
  const res = await fetch('/api/vendor/alerts', { headers: authHeaders() })
  return handleResponse<StockAlert[]>(res)
}

/** Acknowledges a stock alert. */
export async function acknowledgeAlert(alertId: string): Promise<StockAlert> {
  const res = await fetch(`/api/vendor/alerts/${alertId}/acknowledge`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  return handleResponse<StockAlert>(res)
}

/** Exports all vendor products as a UTF-8 CSV file and triggers a browser download (US-CAT-07). */
export async function exportProductsCsv(): Promise<void> {
  const session = getSession()
  const res = await fetch('/api/vendor/products/export', {
    headers: session ? { Authorization: `Bearer ${session.token}` } : {},
  })
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
  const session = getSession()
  const formData = new FormData()
  formData.append('file', file)
  const res = await fetch('/api/vendor/products/import', {
    method: 'POST',
    headers: session ? { Authorization: `Bearer ${session.token}` } : {},
    body: formData,
  })
  if (res.status === 400) throw Object.assign(new Error('CSV_HEADER_INVALID'), { code: 'CSV_HEADER_INVALID' })
  return handleResponse<CsvImportResponse>(res)
}
