import { authedFetch } from './authApi'

export type AuditEventType =
  | 'LOGIN_SUCCESS' | 'LOGIN_FAILURE' | 'ACCOUNT_LOCKED' | 'REGISTRATION'
  | 'ACCOUNT_ACTIVATED' | 'PASSWORD_CHANGED' | 'ACCOUNT_SUSPENDED' | 'ACCOUNT_REACTIVATED'
  | 'ACCOUNT_DELETED' | 'ACCOUNT_CREATED' | 'RESEND_ACTIVATION' | 'PASSWORD_RESET_REQUESTED'
  | 'PASSWORD_RESET' | 'MFA_ENABLED' | 'MFA_LOGIN_SUCCESS' | 'MFA_LOGIN_FAILURE'
  | 'PASSWORD_REVOKED' | 'MARKETING_CONSENT_EXPORT'

export interface AuditLogEntry {
  id: number
  eventType: AuditEventType
  email: string | null
  ipAddress: string | null
  details: string | null
  occurredAt: string
}

export interface AuditLogPage {
  content: AuditLogEntry[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface AuditLogFilter {
  eventType?: AuditEventType
  email?: string
  ipAddress?: string
  from?: string
  to?: string
  page?: number
  size?: number
}

const BASE = '/api/admin/audit-logs'

function buildParams(filter: AuditLogFilter): string {
  const p = new URLSearchParams()
  if (filter.eventType) p.set('eventType', filter.eventType)
  if (filter.email)     p.set('email',     filter.email)
  if (filter.ipAddress) p.set('ipAddress', filter.ipAddress)
  if (filter.from)      p.set('from',      filter.from)
  if (filter.to)        p.set('to',        filter.to)
  p.set('page', String(filter.page ?? 0))
  p.set('size', String(filter.size ?? 20))
  return p.toString()
}

/** Queries audit log entries with optional filters and pagination. */
export async function queryAuditLogs(filter: AuditLogFilter = {}): Promise<AuditLogPage> {
  const res = await authedFetch(`${BASE}?${buildParams(filter)}`)
  if (!res.ok) throw new Error('Failed to fetch audit logs')
  return res.json()
}

/** Downloads filtered audit log entries as a CSV file. */
export async function exportAuditLogCsv(filter: Omit<AuditLogFilter, 'page' | 'size'> = {}): Promise<void> {
  const p = new URLSearchParams()
  if (filter.eventType) p.set('eventType', filter.eventType)
  if (filter.email)     p.set('email',     filter.email)
  if (filter.ipAddress) p.set('ipAddress', filter.ipAddress)
  if (filter.from)      p.set('from',      filter.from)
  if (filter.to)        p.set('to',        filter.to)
  const res = await authedFetch(`${BASE}/export?${p}`)
  if (!res.ok) throw new Error('Failed to export audit logs')
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'audit-log.csv'
  a.click()
  URL.revokeObjectURL(url)
}
