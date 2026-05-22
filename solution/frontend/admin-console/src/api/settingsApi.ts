import { getSession } from './authApi'

const BASE = '/api/admin/settings'

export interface MaintenanceStatusResponse {
  active: boolean
}

function authHeader(): HeadersInit {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function getMaintenanceStatus(): Promise<MaintenanceStatusResponse> {
  const res = await fetch(`${BASE}/maintenance`, { headers: authHeader() })
  if (!res.ok) throw new Error('Failed to fetch maintenance status')
  return res.json()
}

export async function setMaintenanceMode(active: boolean): Promise<MaintenanceStatusResponse> {
  const res = await fetch(`${BASE}/maintenance`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ active }),
  })
  if (!res.ok) throw new Error('Failed to update maintenance mode')
  return res.json()
}
