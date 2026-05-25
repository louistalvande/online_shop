export type ContentType = 'TEXT' | 'IMAGE' | 'BOTH'
export type ImageOrientation = 'PORTRAIT' | 'LANDSCAPE'

export interface PublicAnnouncement {
  id: string
  contentType: ContentType
  textContent: string | null
  imageUrl: string | null
  imageOrientation: ImageOrientation | null
  redirectUrl: string | null
  sortOrder: number
}

/** Fetches all active announcements for the home page carousel. */
export async function fetchActiveAnnouncements(): Promise<PublicAnnouncement[]> {
  const res = await fetch('/api/announcements')
  if (!res.ok) return []
  return res.json()
}
