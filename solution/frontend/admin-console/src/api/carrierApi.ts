import { getSession } from './authApi'

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

function authHeader(): HeadersInit {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function listCarriers(): Promise<CarrierResponse[]> {
  const res = await fetch(BASE, { headers: authHeader() })
  if (!res.ok) throw new Error('Failed to fetch carriers')
  return res.json()
}

export async function createCarrier(payload: CreateCarrierPayload): Promise<CarrierResponse> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to create carrier')
  return res.json()
}

export async function updateCarrier(id: string, payload: UpdateCarrierPayload): Promise<CarrierResponse> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update carrier')
  return res.json()
}

export async function deactivateCarrier(id: string): Promise<CarrierResponse> {
  const res = await fetch(`${BASE}/${id}/deactivate`, {
    method: 'PATCH',
    headers: authHeader(),
  })
  if (!res.ok) throw new Error('Failed to deactivate carrier')
  return res.json()
}

export async function activateCarrier(id: string): Promise<CarrierResponse> {
  const res = await fetch(`${BASE}/${id}/activate`, {
    method: 'PATCH',
    headers: authHeader(),
  })
  if (!res.ok) throw new Error('Failed to activate carrier')
  return res.json()
}

export async function deleteCarrier(id: string): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeader(),
  })
  if (!res.ok) throw new Error('Failed to delete carrier')
}
