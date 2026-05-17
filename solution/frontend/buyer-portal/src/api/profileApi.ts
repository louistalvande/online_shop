import { getSession } from './authApi'

export interface ProfileData {
  id: string
  email: string
  firstName: string
  lastName: string
  phone: string | null
  addressLine: string | null
  city: string | null
  postalCode: string | null
  countryCode: string | null
  language: 'FR' | 'EN'
  role: string
}

export interface UpdateProfilePayload {
  firstName?: string
  lastName?: string
  phone?: string
  addressLine?: string
  city?: string
  postalCode?: string
  countryCode?: string
  language?: 'FR' | 'EN'
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}

function authHeader(): Record<string, string> {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function getProfile(): Promise<ProfileData> {
  const res = await fetch('/api/me', { headers: authHeader() })
  if (!res.ok) throw new Error('Failed to load profile')
  return res.json()
}

export async function updateProfile(payload: UpdateProfilePayload): Promise<ProfileData> {
  const res = await fetch('/api/me', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(payload),
  })
  if (res.status === 422) throw Object.assign(new Error('Wrong password'), { code: 'WRONG_PASSWORD' })
  if (!res.ok) throw new Error('Failed to update profile')
  return res.json()
}
