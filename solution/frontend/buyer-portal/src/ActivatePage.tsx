import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { activate, resendActivation } from './api/authApi'

type State = 'loading' | 'needs_password' | 'success' | 'expired' | 'already_active' | 'error'
type ResendState = 'idle' | 'loading' | 'sent' | 'error'

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
  const [resendState, setResendState] = useState<ResendState>('idle')
  const [resendEmail, setResendEmail] = useState('')

  useEffect(() => {
    if (!token) { setState('error'); return }

    activate(token).then(() => {
      setState('success')
      setTimeout(() => { window.location.href = '/login' }, 2000)
    }).catch((err: unknown) => {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'PASSWORD_REQUIRED') {
        setState('needs_password')
      } else if (code === 'TOKEN_EXPIRED') {
        setState('expired')
      } else if (code === 'TOKEN_NOT_FOUND') {
        setState('already_active')
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
      } else if (code === 'TOKEN_NOT_FOUND') {
        setState('already_active')
      } else {
        setError(t('activate.error.generic'))
      }
    } finally {
      setLoading(false)
    }
  }

  async function handleResend(e: React.FormEvent) {
    e.preventDefault()
    setResendState('loading')
    try {
      await resendActivation(resendEmail)
      setResendState('sent')
    } catch {
      setResendState('error')
    }
  }

  if (state === 'loading') {
    return (
      <div className="page-overlay">
        <p className="activate-loading-text">{t('activate.loading')}</p>
      </div>
    )
  }

  if (state === 'success') {
    return (
      <div className="page-overlay">
        <Card className="auth-card">
          <p className="activate-success-text">{t('activate.success')}</p>
        </Card>
      </div>
    )
  }

  if (state === 'already_active') {
    return (
      <div className="page-overlay">
        <Card className="auth-card">
          <p className="activate-already-text">{t('activate.error.alreadyActive')}</p>
          <a href="/login" className="activate-already-link">
            {t('activate.alreadyActiveAction')}
          </a>
        </Card>
      </div>
    )
  }

  if (state === 'expired') {
    return (
      <div className="page-overlay">
        <Card className="auth-card">
          <p className="activate-expired-text">{t('activate.error.expired')}</p>

          {resendState === 'sent' && (
            <div className="alert-success">{t('activate.resend.success')}</div>
          )}
          {resendState === 'error' && (
            <div className="alert-error">{t('activate.resend.error')}</div>
          )}

          {resendState !== 'sent' && (
            <form onSubmit={handleResend}>
              <label className="form-field">
                {t('activate.resend.label')}
                <input
                  className="form-input"
                  type="email"
                  required
                  value={resendEmail}
                  onChange={e => setResendEmail(e.target.value)}
                />
              </label>
              <Button type="submit" className="btn-full" disabled={resendState === 'loading'}>
                {resendState === 'loading' ? '…' : t('activate.resend.submit')}
              </Button>
            </form>
          )}
        </Card>
      </div>
    )
  }

  if (state === 'error') {
    return (
      <div className="page-overlay">
        <Card className="auth-card">
          <p className="activate-generic-text">{t('activate.error.generic')}</p>
        </Card>
      </div>
    )
  }

  // needs_password: admin-created account must define a password
  return (
    <div className="page-overlay">
      <Card className="auth-card">
        <h1 className="activate-title">{t('activate.title')}</h1>
        <p className="activate-subtitle">{t('activate.subtitle')}</p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label className="form-field">
            {t('activate.password')}
            <input className="form-input" type="password" required minLength={8} value={password} onChange={e => setPassword(e.target.value)} />
          </label>
          <label className="form-field">
            {t('activate.confirmPassword')}
            <input className="form-input" type="password" required value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
          </label>
          <p className="activate-hint">{t('activate.passwordHint')}</p>
          <Button type="submit" className="btn-full" disabled={loading}>
            {loading ? '…' : t('activate.submit')}
          </Button>
        </form>
      </Card>
    </div>
  )
}
