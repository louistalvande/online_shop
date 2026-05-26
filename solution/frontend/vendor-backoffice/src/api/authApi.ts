const SESSION_KEY = 'vendor_session'

export interface VendorSession {
  email: string
  token: string
}

export async function login(email: string, password: string): Promise<VendorSession> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (!res.ok) throw new Error('Login failed')
  const session: VendorSession = await res.json()
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export function logout() {
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): VendorSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

/** fetch wrapper that injects the Bearer token and redirects to /login on 401. */
export async function authedFetch(url: string, init: RequestInit = {}): Promise<Response> {
  const session = getSession()
  if (!session) {
    window.location.href = '/login'
    throw new Error('NOT_AUTHENTICATED')
  }
  const res = await fetch(url, {
    ...init,
    headers: { Authorization: `Bearer ${session.token}`, ...init.headers },
  })
  if (res.status === 401) {
    logout()
    window.location.href = '/login'
    throw new Error('SESSION_EXPIRED')
  }
  return res
}
