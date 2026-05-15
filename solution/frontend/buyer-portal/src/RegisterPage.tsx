import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { register } from './api/authApi'

const overlay: React.CSSProperties = {
  minHeight: '100vh', background: 'var(--bg)',
  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 24,
}

const cardStyle: React.CSSProperties = {
  width: '100%', maxWidth: 420, padding: '36px 32px',
}

const fieldStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 16, fontSize: 14, fontWeight: 500,
}

const inputStyle: React.CSSProperties = {
  padding: '10px 12px', border: '1px solid var(--border)', borderRadius: 4,
  fontSize: 14, background: 'var(--surface)', color: 'var(--text)', outline: 'none',
}

const errorBox: React.CSSProperties = {
  background: '#fef2f2', border: '1px solid #fca5a5',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#b91c1c', marginBottom: 16,
}

const successBox: React.CSSProperties = {
  background: '#f0fdf4', border: '1px solid #86efac',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#166534', marginBottom: 16,
}

export default function RegisterPage() {
  const { t } = useTranslation()
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (password !== confirmPassword) {
      setError(t('register.error.passwordMismatch'))
      return
    }
    setError(null)
    setLoading(true)
    try {
      await register({ firstName, lastName, email, password })
      setSuccess(true)
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      setError(code === 'EMAIL_ALREADY_USED' ? t('register.error.emailUsed') : t('register.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay}>
      <Card style={cardStyle}>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 24, fontWeight: 700, marginBottom: 28 }}>
          {t('register.title')}
        </h1>

        {success ? (
          <div style={successBox}>{t('register.success')}</div>
        ) : (
          <>
            {error && <div style={errorBox}>{error}</div>}
            <form onSubmit={handleSubmit}>
              <label style={fieldStyle}>
                {t('register.firstName')}
                <input style={inputStyle} required value={firstName} onChange={e => setFirstName(e.target.value)} />
              </label>
              <label style={fieldStyle}>
                {t('register.lastName')}
                <input style={inputStyle} required value={lastName} onChange={e => setLastName(e.target.value)} />
              </label>
              <label style={fieldStyle}>
                {t('register.email')}
                <input style={inputStyle} type="email" required value={email} onChange={e => setEmail(e.target.value)} />
              </label>
              <label style={fieldStyle}>
                {t('register.password')}
                <input style={inputStyle} type="password" required minLength={8} value={password} onChange={e => setPassword(e.target.value)} />
              </label>
              <label style={fieldStyle}>
                {t('register.confirmPassword')}
                <input style={inputStyle} type="password" required value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
              </label>
              <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 20 }}>
                {t('register.activationNote')}
              </p>
              <Button type="submit" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
                {loading ? '…' : t('register.submit')}
              </Button>
            </form>
          </>
        )}

        <p style={{ fontSize: 13, textAlign: 'center', marginTop: 20, color: 'var(--text-muted)' }}>
          {t('register.loginLink')}{' '}
          <a href="/login" style={{ color: 'var(--text)', fontWeight: 600 }}>{t('register.loginLinkLabel')}</a>
        </p>
      </Card>
    </div>
  )
}
