const BASE = '/api/public/maintenance'

export interface MaintenanceStatusResponse {
  active: boolean
}

export async function getMaintenanceStatus(): Promise<MaintenanceStatusResponse> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error('Failed to fetch maintenance status')
  return res.json()
}
