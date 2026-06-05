const SESSION_KEY = 'buyer_session'

export interface BuyerSession {
  email: string
}

export interface RegisterPayload {
  firstName: string
  lastName: string
  email: string
  password: string
  marketingConsent: boolean
}

export interface LoginResult {
  session?: BuyerSession
  requiresMfa?: boolean
  mfaToken?: string
  email?: string
  requiresPasswordSetup?: boolean
}

export async function register(payload: RegisterPayload): Promise<void> {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (res.status === 409) throw Object.assign(new Error('Email already used'), { code: 'EMAIL_ALREADY_USED' })
  if (res.status === 422) throw Object.assign(new Error('Password compromised'), { code: 'PASSWORD_COMPROMISED' })
  if (!res.ok) throw new Error('Registration failed')
}

export async function activate(token: string, password?: string, confirmPassword?: string): Promise<void> {
  const body: Record<string, string> = { token }
  if (password != null) body.password = password
  if (confirmPassword != null) body.confirmPassword = confirmPassword

  const res = await fetch('/api/auth/activate', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (res.status === 404) throw Object.assign(new Error('Token not found'), { code: 'TOKEN_NOT_FOUND' })
  if (res.status === 410) throw Object.assign(new Error('Token expired'), { code: 'TOKEN_EXPIRED' })
  if (res.status === 422) throw Object.assign(new Error('Password compromised'), { code: 'PASSWORD_COMPROMISED' })
  if (res.status === 401) throw Object.assign(new Error('Password required'), { code: 'PASSWORD_REQUIRED' })
  if (res.status === 400) {
    const data = await res.json().catch(() => ({}))
    const code = data.error === 'PASSWORDS_MISMATCH' ? 'PASSWORDS_MISMATCH' : 'PASSWORD_REQUIRED'
    throw Object.assign(new Error(code), { code })
  }
  if (!res.ok) throw new Error('Activation failed')
}

export async function login(email: string, password: string): Promise<LoginResult> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid credentials'), { code: 'INVALID_CREDENTIALS' })
  if (res.status === 429) throw Object.assign(new Error('Too many attempts'), { code: 'TOO_MANY_ATTEMPTS' })
  if (!res.ok) throw new Error('Login failed')

  const data = await res.json()
  if (data.requiresMfa) {
    return { requiresMfa: true, mfaToken: data.mfaToken, email: data.email }
  }
  if (data.role !== 'BUYER') {
    throw Object.assign(new Error('Access restricted to buyers'), { code: 'UNAUTHORIZED' })
  }

  const session: BuyerSession = { email: data.email }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return { session, requiresPasswordSetup: data.requiresPasswordSetup }
}

export async function verifyMfa(mfaToken: string, code: string): Promise<BuyerSession> {
  const res = await fetch('/api/auth/mfa/verify', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mfaToken, code }),
  })
  if (res.status === 401) throw Object.assign(new Error('Invalid MFA code'), { code: 'INVALID_MFA_CODE' })
  if (!res.ok) throw new Error('MFA verification failed')

  const data = await res.json()
  const session: BuyerSession = { email: data.email }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }).catch(() => {})
  localStorage.removeItem(SESSION_KEY)
}

export function getSession(): BuyerSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  return raw ? JSON.parse(raw) : null
}

/** fetch wrapper that uses the HttpOnly cookie; redirects to /login on 401. */
export async function authedFetch(url: string, init: RequestInit = {}): Promise<Response> {
  if (!getSession()) {
    window.location.href = '/login'
    throw new Error('NOT_AUTHENTICATED')
  }
  const res = await fetch(url, { ...init, credentials: 'include' })
  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem(SESSION_KEY)
    window.location.href = '/login'
    throw new Error('SESSION_EXPIRED')
  }
  return res
}

export async function resendActivation(email: string): Promise<void> {
  const res = await fetch('/api/auth/resend-activation', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  if (!res.ok) throw new Error('Resend failed')
}

export async function forgotPassword(email: string): Promise<void> {
  const res = await fetch('/api/auth/forgot-password', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  if (!res.ok) throw new Error('Request failed')
}

export async function resetPassword(token: string, newPassword: string): Promise<void> {
  const res = await fetch('/api/auth/reset-password', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, newPassword }),
  })
  if (res.status === 410) throw Object.assign(new Error('Token invalid'), { code: 'RESET_TOKEN_INVALID' })
  if (res.status === 422) throw Object.assign(new Error('Password compromised'), { code: 'PASSWORD_COMPROMISED' })
  if (!res.ok) throw new Error('Reset failed')
}
