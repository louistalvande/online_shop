import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { activate } from './api/authApi'

type State = 'loading' | 'needs_password' | 'success' | 'expired' | 'error'

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

interface Props {
  token: string
}

export default function ActivatePage({ token }: Props) {
  const { t } = useTranslation()
  const [state, setState] = useState<State>('loading')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!token) { setState('error'); return }

    // Attempt activation without password — succeeds for self-registered buyers
    activate(token).then(() => {
      setState('success')
      setTimeout(() => { window.location.href = '/login' }, 2000)
    }).catch((err: unknown) => {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'PASSWORD_REQUIRED') {
        setState('needs_password')
      } else if (code === 'TOKEN_EXPIRED') {
        setState('expired')
      } else {
        setState('error')
      }
    })
  }, [token])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (password !== confirmPassword) {
      setError(t('activate.error.mismatch'))
      return
    }
    setError(null)
    setLoading(true)
    try {
      await activate(token, password, confirmPassword)
      setState('success')
      setTimeout(() => { window.location.href = '/login' }, 2000)
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'PASSWORDS_MISMATCH') {
        setError(t('activate.error.mismatch'))
      } else if (code === 'TOKEN_EXPIRED') {
        setState('expired')
      } else {
        setError(t('activate.error.generic'))
      }
    } finally {
      setLoading(false)
    }
  }

  if (state === 'loading') {
    return (
      <div style={overlay}>
        <p style={{ color: 'var(--text-muted)' }}>{t('activate.loading')}</p>
      </div>
    )
  }

  if (state === 'success') {
    return (
      <div style={overlay}>
        <Card style={cardStyle}>
          <p style={{ fontSize: 15, color: '#166534' }}>{t('activate.success')}</p>
        </Card>
      </div>
    )
  }

  if (state === 'expired') {
    return (
      <div style={overlay}>
        <Card style={cardStyle}>
          <p style={{ fontSize: 15, marginBottom: 16 }}>{t('activate.error.expired')}</p>
          <a href="/register" style={{ fontSize: 14, color: 'var(--text)', fontWeight: 600 }}>
            {t('activate.resendLink')}
          </a>
        </Card>
      </div>
    )
  }

  if (state === 'error') {
    return (
      <div style={overlay}>
        <Card style={cardStyle}>
          <p style={{ fontSize: 15 }}>{t('activate.error.generic')}</p>
        </Card>
      </div>
    )
  }

  // needs_password: admin-created account must define a password
  return (
    <div style={overlay}>
      <Card style={cardStyle}>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 24, fontWeight: 700, marginBottom: 8 }}>
          {t('activate.title')}
        </h1>
        <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 24 }}>{t('activate.subtitle')}</p>

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={fieldStyle}>
            {t('activate.password')}
            <input style={inputStyle} type="password" required minLength={8} value={password} onChange={e => setPassword(e.target.value)} />
          </label>
          <label style={fieldStyle}>
            {t('activate.confirmPassword')}
            <input style={inputStyle} type="password" required value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
          </label>
          <p style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 20 }}>{t('activate.passwordHint')}</p>
          <Button type="submit" style={{ width: '100%', justifyContent: 'center' }} disabled={loading}>
            {loading ? '…' : t('activate.submit')}
          </Button>
        </form>
      </Card>
    </div>
  )
}
