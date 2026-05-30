import { authedFetch } from './authApi'

export interface ProfileData {
  id: string
  email: string
  firstName: string
  lastName: string
  phone: string | null
  language: 'FR' | 'EN' | 'ES'
  role: string
}

export interface UpdateProfilePayload {
  firstName?: string
  lastName?: string
  phone?: string
  language?: 'FR' | 'EN' | 'ES'
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}

export interface DeliveryAddressData {
  id: string
  label: string
  addressLine: string
  city: string
  postalCode: string
  countryCode: string
  default: boolean
}

export interface CreateDeliveryAddressPayload {
  label: string
  addressLine: string
  city: string
  postalCode: string
  countryCode: string
  makeDefault: boolean
}

export type UpdateDeliveryAddressPayload = CreateDeliveryAddressPayload

/** Fetches the authenticated buyer's profile. */
export async function getProfile(): Promise<ProfileData> {
  const res = await authedFetch('/api/me')
  if (!res.ok) throw new Error('Failed to load profile')
  return res.json()
}

/** Patches scalar profile fields or changes the password. */
export async function updateProfile(payload: UpdateProfilePayload): Promise<ProfileData> {
  const res = await authedFetch('/api/me', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 422) throw Object.assign(new Error('Wrong password'), { code: 'WRONG_PASSWORD' })
  if (!res.ok) throw new Error('Failed to update profile')
  return res.json()
}

const ADDR_BASE = '/api/profile/addresses'

/** Lists all active delivery addresses for the authenticated buyer. */
export async function listAddresses(): Promise<DeliveryAddressData[]> {
  const res = await authedFetch(ADDR_BASE)
  if (!res.ok) throw new Error('ADDR_LOAD_ERROR')
  return res.json()
}

/** Creates a new delivery address (CS-04: countryCode must be a Eurozone country). */
export async function createAddress(payload: CreateDeliveryAddressPayload): Promise<DeliveryAddressData> {
  const res = await authedFetch(ADDR_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 422) throw Object.assign(new Error('Invalid country'), { code: 'INVALID_COUNTRY' })
  if (!res.ok) throw new Error('ADDR_SAVE_ERROR')
  return res.json()
}

/** Replaces all fields of an existing delivery address. */
export async function updateAddress(id: string, payload: UpdateDeliveryAddressPayload): Promise<DeliveryAddressData> {
  const res = await authedFetch(`${ADDR_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 404) throw Object.assign(new Error('Not found'), { code: 'ADDR_NOT_FOUND' })
  if (res.status === 422) throw Object.assign(new Error('Invalid country'), { code: 'INVALID_COUNTRY' })
  if (!res.ok) throw new Error('ADDR_SAVE_ERROR')
  return res.json()
}

/** Soft-deletes a delivery address. Throws LAST_ACTIVE_ADDRESS if it is the only active one. */
export async function deleteAddress(id: string): Promise<void> {
  const res = await authedFetch(`${ADDR_BASE}/${id}`, { method: 'DELETE' })
  if (res.status === 409) throw Object.assign(new Error('Last active address'), { code: 'LAST_ACTIVE_ADDRESS' })
  if (res.status === 404) throw Object.assign(new Error('Not found'), { code: 'ADDR_NOT_FOUND' })
  if (!res.ok) throw new Error('ADDR_DELETE_ERROR')
}

/** Marks a delivery address as the default, clearing the previous default. */
export async function setDefaultAddress(id: string): Promise<DeliveryAddressData> {
  const res = await authedFetch(`${ADDR_BASE}/${id}/default`, { method: 'PATCH' })
  if (res.status === 404) throw Object.assign(new Error('Not found'), { code: 'ADDR_NOT_FOUND' })
  if (!res.ok) throw new Error('ADDR_DEFAULT_ERROR')
  return res.json()
}
