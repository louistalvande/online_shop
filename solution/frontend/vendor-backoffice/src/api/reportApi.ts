import { getSession } from './authApi'

export interface SalesMetrics {
  revenue: number
  orderCount: number
  averageCartValue: number
  cancellationRate: number
}

export interface TopProduct {
  rank: number
  productName: string
  quantitySold: number
  revenueGenerated: number
}

export interface SalesReportResponse {
  period: string
  category: string | null
  metrics: SalesMetrics
  topSellingProducts: TopProduct[]
}

function authHeaders(): HeadersInit {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function getSalesReport(
  period: string,
  category?: string,
): Promise<SalesReportResponse> {
  const params = new URLSearchParams({ period })
  if (category) params.set('category', category)
  const res = await fetch(`/api/vendor/reports/sales?${params}`, {
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(`${res.status}`)
  return res.json()
}

export async function exportSalesCsv(period: string, category?: string): Promise<void> {
  const params = new URLSearchParams({ period })
  if (category) params.set('category', category)
  const res = await fetch(`/api/vendor/reports/sales/export?${params}`, {
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(`${res.status}`)
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `sales-report-${period}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
