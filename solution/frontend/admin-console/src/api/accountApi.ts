import { authedFetch } from './authApi'

export type AccountRole = 'BUYER' | 'VENDOR'
export type AccountStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'DELETED'
export type AccountLanguage = 'FR' | 'EN'

export interface AccountResponse {
  id: string
  email: string
  firstName: string
  lastName: string
  role: AccountRole | 'ADMIN'
  status: AccountStatus
  language: AccountLanguage
  createdAt: string
  passwordRevoked: boolean
  passwordRevokedAt: string | null
}

export interface CreateAccountRequest {
  email: string
  firstName: string
  lastName: string
  role: AccountRole
}

export interface UpdateAccountRequest {
  firstName?: string
  lastName?: string
  role?: AccountRole
  language?: AccountLanguage
}

export interface RevokePasswordsRequest {
  role?: AccountRole | 'ADMIN'
  emails?: string[]
}

export interface RevokedAccountResponse {
  id: string
  email: string
  firstName: string
  lastName: string
  role: AccountRole | 'ADMIN'
  status: AccountStatus
  revokedAt: string
  hoursSinceRevocation: number
}

const BASE = '/api/admin/accounts'

export async function listAccounts(): Promise<AccountResponse[]> {
  const res = await authedFetch(BASE)
  if (res.status === 401) throw Object.assign(new Error('Unauthorized'), { code: 'UNAUTHORIZED' })
  if (res.status === 403) throw Object.assign(new Error('Forbidden'), { code: 'FORBIDDEN' })
  if (!res.ok) throw new Error('Failed to fetch accounts')
  return res.json()
}

export async function createAccount(payload: CreateAccountRequest): Promise<AccountResponse> {
  const res = await authedFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 409) {
    const body = await res.json()
    throw Object.assign(new Error(body.message), { code: 'EMAIL_ALREADY_USED' })
  }
  if (!res.ok) throw new Error('Failed to create account')
  return res.json()
}

export async function updateAccount(id: string, payload: UpdateAccountRequest): Promise<AccountResponse> {
  const res = await authedFetch(`${BASE}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update account')
  return res.json()
}

export async function deleteAccount(id: string): Promise<void> {
  const res = await authedFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete account')
}

export async function suspendAccount(id: string): Promise<AccountResponse> {
  const res = await authedFetch(`${BASE}/${id}/suspend`, { method: 'PATCH' })
  if (res.status === 409) throw Object.assign(new Error('Invalid account state'), { code: 'INVALID_ACCOUNT_STATE' })
  if (!res.ok) throw new Error('Failed to suspend account')
  return res.json()
}

export async function reactivateAccount(id: string): Promise<AccountResponse> {
  const res = await authedFetch(`${BASE}/${id}/reactivate`, { method: 'PATCH' })
  if (res.status === 409) throw Object.assign(new Error('Invalid account state'), { code: 'INVALID_ACCOUNT_STATE' })
  if (!res.ok) throw new Error('Failed to reactivate account')
  return res.json()
}

/** Marks accounts (by role or email list) as password-revoked and sends notification emails (US-SEC-04). */
export async function revokePasswords(payload: RevokePasswordsRequest): Promise<void> {
  const res = await authedFetch(`${BASE}/revoke-passwords`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to revoke passwords')
}

/** Returns accounts whose password has been revoked and who have not yet renewed it (US-SEC-04). */
export async function listRevokedAccounts(): Promise<RevokedAccountResponse[]> {
  const res = await authedFetch(`${BASE}/revoked`)
  if (!res.ok) throw new Error('Failed to fetch revoked accounts')
  return res.json()
}
