import { authedFetch } from './authApi'

const BASE = '/api/admin/carriers'

export interface CarrierResponse {
  id: string
  name: string
  trackingUrl: string
  active: boolean
  supportedCountries: string[]
  createdAt: string
}

export interface CreateCarrierPayload {
  name: string
  trackingUrl: string
  supportedCountries: string[]
}

export interface UpdateCarrierPayload {
  name: string
  trackingUrl: string
  supportedCountries: string[]
}

export async function listCarriers(): Promise<CarrierResponse[]> {
  const res = await authedFetch(BASE)
  if (!res.ok) throw new Error('Failed to fetch carriers')
  return res.json()
}

export async function createCarrier(payload: CreateCarrierPayload): Promise<CarrierResponse> {
  const res = await authedFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to create carrier')
  return res.json()
}

export async function updateCarrier(id: string, payload: UpdateCarrierPayload): Promise<CarrierResponse> {
  const res = await authedFetch(`${BASE}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update carrier')
  return res.json()
}

export async function deactivateCarrier(id: string): Promise<CarrierResponse> {
  const res = await authedFetch(`${BASE}/${id}/deactivate`, { method: 'PATCH' })
  if (!res.ok) throw new Error('Failed to deactivate carrier')
  return res.json()
}

export async function activateCarrier(id: string): Promise<CarrierResponse> {
  const res = await authedFetch(`${BASE}/${id}/activate`, { method: 'PATCH' })
  if (!res.ok) throw new Error('Failed to activate carrier')
  return res.json()
}

export async function deleteCarrier(id: string): Promise<void> {
  const res = await authedFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete carrier')
}
