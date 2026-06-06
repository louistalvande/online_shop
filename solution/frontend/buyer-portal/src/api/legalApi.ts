export interface LegalPageData {
  key: string
  content: string
}

export async function getLegalPage(pageKey: string): Promise<LegalPageData> {
  const res = await fetch(`/api/public/legal/${pageKey}`)
  if (!res.ok) throw new Error('legal_load_failed')
  return res.json()
}
