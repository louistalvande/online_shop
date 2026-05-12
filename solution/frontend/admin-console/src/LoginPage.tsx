import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, GalleryIcon } from '@workspace/theme'
import { login } from './api/authApi'

interface LoginPageProps {
  onLogin: () => void
}

export default function LoginPage({ onLogin }: LoginPageProps) {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      onLogin()
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      setError(code === 'INVALID_CREDENTIALS' ? t('login.error.invalid') : t('login.error.generic'))
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

      <Card style={cardStyle}>
        <h1 style={titleStyle}>{t('login.title')}</h1>
        <p style={subtitleStyle}>{t('login.subtitle')}</p>

        <form onSubmit={handleSubmit} style={formStyle}>
          <label style={labelStyle}>
            {t('login.email')}
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              autoFocus
              style={inputStyle}
              placeholder="admin@onlineshop.com"
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
  maxWidth: 400,
  padding: '36px 32px',
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
