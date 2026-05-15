import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { login } from './api/authApi'

interface Props {
  onClose: () => void
  onLogin: () => void
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 360, maxWidth: 420, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
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

export default function LoginModal({ onClose, onLogin }: Props) {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(email, password)
      onLogin()
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      setError(code === 'INVALID_CREDENTIALS' ? t('login.error.invalid') : t('login.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
          {t('login.title')}
        </h2>

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={fieldStyle}>
            {t('login.email')}
            <input style={inputStyle} type="email" required autoFocus value={email} onChange={e => setEmail(e.target.value)} />
          </label>
          <label style={fieldStyle}>
            {t('login.password')}
            <input style={inputStyle} type="password" required value={password} onChange={e => setPassword(e.target.value)} />
          </label>

          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 14, marginBottom: 20 }}>
            <input type="checkbox" />
            {t('login.remember')}
          </label>

          <Button type="submit" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
            {loading ? '…' : t('login.submit')}
          </Button>
        </form>

        <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', gap: 10, alignItems: 'center' }}>
          <a href="/forgot-password" style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            {t('login.forgotPassword')}
          </a>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', margin: 0 }}>
            {t('login.registerLink')}{' '}
            <a href="/register" style={{ color: 'var(--text)', fontWeight: 600 }}>{t('login.registerLinkLabel')}</a>
          </p>
        </div>
      </div>
    </div>
  )
}
