const SESSION_KEY = 'admin_session'

export interface AdminSession {
  email: string
  token: string
}

export interface LoginResponse extends AdminSession {
  requiresPasswordSetup: boolean
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (!res.ok) throw new Error('Login failed')
  const data: LoginResponse = await res.json()
  localStorage.setItem(SESSION_KEY, JSON.stringify({ email: data.email, token: data.token }))
  return data
}

export async function setupPassword(password: string, confirmPassword: string): Promise<void> {
  const session = getSession()
  const res = await fetch('/api/auth/setup-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${session?.token}` },
    body: JSON.stringify({ password, confirmPassword }),
  })
  if (!res.ok) throw new Error('Password setup failed')
}

export function logout() {
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): AdminSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}
