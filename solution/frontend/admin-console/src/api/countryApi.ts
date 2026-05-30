import { authedFetch } from './authApi'

const BASE = '/api/admin/countries'

export interface CountryResponse {
  code: string
  nameFr: string
  nameEn: string
}

export async function listCountries(): Promise<CountryResponse[]> {
  const res = await authedFetch(BASE)
  if (!res.ok) throw new Error('Failed to fetch countries')
  return res.json()
}
