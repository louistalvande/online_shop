import { authedFetch } from './authApi'

const BASE = '/api/admin/settings'

export interface MaintenanceStatusResponse {
  active: boolean
}

export async function getMaintenanceStatus(): Promise<MaintenanceStatusResponse> {
  const res = await authedFetch(`${BASE}/maintenance`)
  if (!res.ok) throw new Error('Failed to fetch maintenance status')
  return res.json()
}

export async function setMaintenanceMode(active: boolean): Promise<MaintenanceStatusResponse> {
  const res = await authedFetch(`${BASE}/maintenance`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  })
  if (!res.ok) throw new Error('Failed to update maintenance mode')
  return res.json()
}
