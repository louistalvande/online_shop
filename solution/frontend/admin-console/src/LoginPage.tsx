import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, GalleryIcon } from '@workspace/theme'
import { login, setupPassword } from './api/authApi'

interface LoginPageProps {
  onLogin: () => void
}

export default function LoginPage({ onLogin }: LoginPageProps) {
  const { t } = useTranslation()
  const [step, setStep] = useState<'login' | 'setup'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login(email, password)
      if (res.requiresPasswordSetup) {
        setStep('setup')
      } else {
        onLogin()
      }
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      if (code === 'INVALID_CREDENTIALS') setError(t('login.error.invalid'))
      else if (code === 'UNAUTHORIZED') setError(t('login.error.unauthorized'))
      else setError(t('login.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  async function handleSetup(e: React.FormEvent) {
    e.preventDefault()
    if (newPassword !== confirmPassword) {
      setError(t('setup.error.mismatch'))
      return
    }
    setError('')
    setLoading(true)
    try {
      await setupPassword(newPassword, confirmPassword)
      onLogin()
    } catch {
      setError(t('setup.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={pageStyle}>
      <div style={brandStyle}>
        <GalleryIcon size={32} />
        <span style={brandNameStyle}>Catalogue de dessins</span>
      </div>

      {step === 'login' ? (
        <Card style={cardStyle}>
          <h1 style={titleStyle}>{t('login.title')}</h1>
          <p style={subtitleStyle}>{t('login.subtitle')}</p>

          <form onSubmit={handleLogin} style={formStyle}>
            <label style={labelStyle}>
              {t('login.email')}
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
                autoFocus
                style={inputStyle}
              />
            </label>

            <label style={labelStyle}>
              {t('login.password')}
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                style={inputStyle}
              />
            </label>

            {error && <p style={errorStyle}>{error}</p>}

            <Button type="submit" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
              {loading ? t('login.loading') : t('login.submit')}
            </Button>
          </form>
        </Card>
      ) : (
        <Card style={cardStyle}>
          <h1 style={titleStyle}>{t('setup.title')}</h1>
          <p style={subtitleStyle}>{t('setup.subtitle')}</p>

          <form onSubmit={handleSetup} style={formStyle}>
            <label style={labelStyle}>
              {t('setup.newPassword')}
              <input
                type="password"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
                minLength={8}
                autoFocus
                style={inputStyle}
              />
            </label>

            <label style={labelStyle}>
              {t('setup.confirmPassword')}
              <input
                type="password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
                minLength={8}
                style={inputStyle}
              />
            </label>

            {error && <p style={errorStyle}>{error}</p>}

            <Button type="submit" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
              {loading ? t('setup.loading') : t('setup.submit')}
            </Button>
          </form>
        </Card>
      )}
    </div>
  )
}

const pageStyle: React.CSSProperties = {
  minHeight: '100vh',
  background: 'var(--bg)',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: 24,
}
const brandStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 10,
  marginBottom: 32,
  color: 'var(--text)',
}
const brandNameStyle: React.CSSProperties = {
  fontWeight: 700,
  fontSize: 18,
}
const cardStyle: React.CSSProperties = {
  width: '100%',
  maxWidth: 480,
  padding: '36px 40px',
}
const titleStyle: React.CSSProperties = {
  fontFamily: 'var(--font-serif)',
  fontSize: 24,
  fontWeight: 700,
  marginBottom: 6,
}
const subtitleStyle: React.CSSProperties = {
  fontSize: 14,
  color: 'var(--text-muted)',
  marginBottom: 28,
}
const formStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: 18,
}
const labelStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: 6,
  fontSize: 14,
  fontWeight: 500,
}
const inputStyle: React.CSSProperties = {
  padding: '10px 12px',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius)',
  fontSize: 14,
  fontFamily: 'inherit',
  background: 'var(--surface)',
  color: 'var(--text)',
  outline: 'none',
}
const errorStyle: React.CSSProperties = {
  fontSize: 13,
  color: 'var(--accent)',
  margin: 0,
}
