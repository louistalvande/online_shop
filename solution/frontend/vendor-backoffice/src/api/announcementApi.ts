import { getSession } from './authApi'

export type ContentType = 'TEXT' | 'IMAGE' | 'BOTH'
export type ImageOrientation = 'PORTRAIT' | 'LANDSCAPE'

export interface Announcement {
  id: string
  vendorId: string
  contentType: ContentType
  textContent: string | null
  imageUrl: string | null
  imageOrientation: ImageOrientation | null
  redirectUrl: string | null
  sortOrder: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ImageUploadResult {
  imageUrl: string
  imageOrientation: ImageOrientation
}

export interface AnnouncementPayload {
  contentType: ContentType
  textContent?: string | null
  imageUrl?: string | null
  imageOrientation?: ImageOrientation | null
  redirectUrl?: string | null
  sortOrder?: number
  active?: boolean
}

function authHeaders(): Record<string, string> {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

/** Uploads an image file and returns its public URL + auto-detected orientation. */
export async function uploadImage(file: File): Promise<ImageUploadResult> {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch('/api/vendor/announcements/images', {
    method: 'POST',
    headers: authHeaders(),
    body: form,
  })
  if (!res.ok) throw new Error('upload_failed')
  return res.json()
}

/** Lists all announcements for the authenticated vendor. */
export async function listAnnouncements(): Promise<Announcement[]> {
  const res = await fetch('/api/vendor/announcements', {
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
  })
  if (!res.ok) throw new Error('load_failed')
  return res.json()
}

/** Creates a new announcement. */
export async function createAnnouncement(payload: AnnouncementPayload): Promise<Announcement> {
  const res = await fetch('/api/vendor/announcements', {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('save_failed')
  return res.json()
}

/** Updates an existing announcement. */
export async function updateAnnouncement(id: string, payload: AnnouncementPayload): Promise<Announcement> {
  const res = await fetch(`/api/vendor/announcements/${id}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('save_failed')
  return res.json()
}

/** Deletes an announcement. */
export async function deleteAnnouncement(id: string): Promise<void> {
  const res = await fetch(`/api/vendor/announcements/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('delete_failed')
}

/** Moves an announcement one position up in the list. */
export async function moveAnnouncementUp(id: string): Promise<Announcement> {
  const res = await fetch(`/api/vendor/announcements/${id}/move-up`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('move_failed')
  return res.json()
}

/** Moves an announcement one position down in the list. */
export async function moveAnnouncementDown(id: string): Promise<Announcement> {
  const res = await fetch(`/api/vendor/announcements/${id}/move-down`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('move_failed')
  return res.json()
}
