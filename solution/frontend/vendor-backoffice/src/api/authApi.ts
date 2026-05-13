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
