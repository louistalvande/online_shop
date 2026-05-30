const SESSION_KEY = 'admin_session'

export interface AdminSession {
  email: string
}

export interface LoginResponse extends AdminSession {
  requiresPasswordSetup: boolean
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (!res.ok) throw new Error('Login failed')
  const data = await res.json()
  if (data.role !== 'ADMIN') {
    throw Object.assign(new Error('Access restricted to administrators'), { code: 'UNAUTHORIZED' })
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify({ email: data.email }))
  return { email: data.email, requiresPasswordSetup: data.requiresPasswordSetup }
}

export async function setupPassword(password: string, confirmPassword: string): Promise<void> {
  const res = await fetch('/api/auth/setup-password', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ password, confirmPassword }),
  })
  if (!res.ok) throw new Error('Password setup failed')
}

export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }).catch(() => {})
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): AdminSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

export function authedFetch(url: string, options: RequestInit = {}): Promise<Response> {
  return fetch(url, { ...options, credentials: 'include' })
}
