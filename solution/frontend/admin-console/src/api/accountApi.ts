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

import { getSession } from './authApi'

const BASE = '/api/admin/accounts'

function authHeader(): HeadersInit {
  const session = getSession()
  return session ? { Authorization: `Bearer ${session.token}` } : {}
}

export async function listAccounts(): Promise<AccountResponse[]> {
  const res = await fetch(BASE, { headers: authHeader() })
  if (!res.ok) throw new Error('Failed to fetch accounts')
  return res.json()
}

export async function createAccount(payload: CreateAccountRequest): Promise<AccountResponse> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
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
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('Failed to update account')
  return res.json()
}

export async function deleteAccount(id: string): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'DELETE',
    headers: authHeader(),
  })
  if (!res.ok) throw new Error('Failed to delete account')
}
