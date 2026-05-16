import { getSession } from './authApi'

const BASE = '/api/admin/countries'

export interface CountryResponse {
  code: string
  nameFr: string
  nameEn: string
}

function authHeader(): HeadersInit {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function listCountries(): Promise<CountryResponse[]> {
  const res = await fetch(BASE, { headers: authHeader() })
  if (!res.ok) throw new Error('Failed to fetch countries')
  return res.json()
}
