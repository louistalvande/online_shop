const SESSION_KEY = 'buyer_session'

export interface BuyerSession {
  email: string
  token: string
}

export interface RegisterPayload {
  firstName: string
  lastName: string
  email: string
  password: string
}

export async function register(payload: RegisterPayload): Promise<void> {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 409) throw Object.assign(new Error('Email already used'), { code: 'EMAIL_ALREADY_USED' })
  if (!res.ok) throw new Error('Registration failed')
}

export async function activate(token: string, password?: string, confirmPassword?: string): Promise<void> {
  const body: Record<string, string> = { token }
  if (password != null) body.password = password
  if (confirmPassword != null) body.confirmPassword = confirmPassword

  const res = await fetch('/api/auth/activate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (res.status === 404) throw Object.assign(new Error('Token not found'), { code: 'TOKEN_NOT_FOUND' })
  if (res.status === 410) throw Object.assign(new Error('Token expired'), { code: 'TOKEN_EXPIRED' })
  if (res.status === 401) throw Object.assign(new Error('Password required'), { code: 'PASSWORD_REQUIRED' })
  if (res.status === 400) {
    const data = await res.json().catch(() => ({}))
    const code = data.error === 'PASSWORDS_MISMATCH' ? 'PASSWORDS_MISMATCH' : 'PASSWORD_REQUIRED'
    throw Object.assign(new Error(code), { code })
  }
  if (!res.ok) throw new Error('Activation failed')
}

export async function login(email: string, password: string): Promise<BuyerSession> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (!res.ok) throw new Error('Login failed')
  const session: BuyerSession = await res.json()
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export function logout(): void {
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): BuyerSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

export async function resendActivation(email: string): Promise<void> {
  const res = await fetch('/api/auth/resend-activation', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  if (!res.ok) throw new Error('Resend failed')
}
