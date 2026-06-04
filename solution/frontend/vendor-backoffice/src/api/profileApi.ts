import { authedFetch } from './authApi'

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

/** Uploads a vendor logo image and returns its fixed public URL. */
export async function uploadVendorLogo(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const res = await authedFetch('/api/me/logo', { method: 'POST', body: form })
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw Object.assign(new Error('Failed to upload logo'), { serverMessage: body?.message ?? null })
  }
  const data: { logoUrl: string } = await res.json()
  return data.logoUrl
}

/** Deletes the vendor logo file. */
export async function deleteLogo(): Promise<void> {
  const res = await authedFetch('/api/me/logo', { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete logo')
}

/** Uploads a vendor hero banner image and returns its fixed public URL. */
export async function uploadVendorBanner(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const res = await authedFetch('/api/me/banner', { method: 'POST', body: form })
  if (!res.ok) throw new Error('Failed to upload banner')
  const data: { bannerUrl: string } = await res.json()
  return data.bannerUrl
}

/** Deletes the vendor hero banner file. */
export async function deleteBanner(): Promise<void> {
  const res = await authedFetch('/api/me/banner', { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete banner')
}
