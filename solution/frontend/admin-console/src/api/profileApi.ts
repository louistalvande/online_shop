import { authedFetch } from './authApi'

export interface ProfileData {
  id: string
  email: string
  firstName: string
  lastName: string
  phone: string | null
  language: 'FR' | 'EN'
  role: string
}

export interface UpdateProfilePayload {
  firstName?: string
  lastName?: string
  phone?: string
  language?: 'FR' | 'EN'
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}

export async function getProfile(): Promise<ProfileData> {
  const res = await authedFetch('/api/me')
  if (!res.ok) throw new Error('Failed to load profile')
  return res.json()
}

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
