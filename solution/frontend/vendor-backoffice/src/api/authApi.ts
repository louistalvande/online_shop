const SESSION_KEY = 'vendor_session'

export interface VendorSession {
  email: string
}

export async function login(email: string, password: string): Promise<VendorSession> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (!res.ok) throw new Error('Login failed')
  const data = await res.json()
  if (data.role !== 'VENDOR') {
    throw Object.assign(new Error('Access restricted to vendors'), { code: 'UNAUTHORIZED' })
  }
  const session: VendorSession = { email: data.email }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }).catch(() => {})
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): VendorSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

/** fetch wrapper that uses the HttpOnly cookie; redirects to app root on 401. */
export async function authedFetch(url: string, init: RequestInit = {}): Promise<Response> {
  if (!getSession()) {
    localStorage.removeItem(SESSION_KEY)
    window.location.href = import.meta.env.BASE_URL || '/'
    throw new Error('NOT_AUTHENTICATED')
  }
  const res = await fetch(url, { ...init, credentials: 'include' })
  if (res.status === 401) {
    localStorage.removeItem(SESSION_KEY)
    window.location.href = import.meta.env.BASE_URL || '/'
    throw new Error('SESSION_EXPIRED')
  }
  return res
}
