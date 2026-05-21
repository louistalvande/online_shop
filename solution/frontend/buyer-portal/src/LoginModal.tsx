import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { login, verifyMfa } from './api/authApi'

interface Props {
  onClose: () => void
  onLogin: () => void
}

export default function LoginModal({ onClose, onLogin }: Props) {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [mfaStep, setMfaStep] = useState(false)
  const [mfaToken, setMfaToken] = useState('')
  const [mfaCode, setMfaCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const result = await login(email, password)
      if (result.requiresMfa && result.mfaToken) {
        setMfaToken(result.mfaToken)
        setMfaStep(true)
      } else {
        onLogin()
      }
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'INVALID_CREDENTIALS') setError(t('login.error.invalid'))
      else if (code === 'TOO_MANY_ATTEMPTS') setError(t('login.error.tooMany'))
      else setError(t('login.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  async function handleMfaSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await verifyMfa(mfaToken, mfaCode)
      onLogin()
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      setError(code === 'INVALID_MFA_CODE' ? t('login.mfa.error.invalid') : t('login.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  if (mfaStep) {
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal" onClick={e => e.stopPropagation()}>
          <h2 className="modal-title">{t('login.mfa.title')}</h2>
          <p className="modal-subtitle">{t('login.mfa.subtitle')}</p>

          {error && <div className="alert-error">{error}</div>}

          <form onSubmit={handleMfaSubmit}>
            <label className="form-field">
              {t('login.mfa.code')}
              <input
                className="form-input"
                type="text"
                inputMode="numeric"
                pattern="\d{6}"
                maxLength={6}
                required
                autoFocus
                value={mfaCode}
                onChange={e => setMfaCode(e.target.value)}
                placeholder="000000"
              />
            </label>

            <Button type="submit" className="btn-full" disabled={loading}>
              {loading ? '…' : t('login.mfa.submit')}
            </Button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h2 className="modal-title">{t('login.title')}</h2>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label className="form-field">
            {t('login.email')}
            <input className="form-input" type="email" required autoFocus value={email} onChange={e => setEmail(e.target.value)} />
          </label>
          <label className="form-field">
            {t('login.password')}
            <input className="form-input" type="password" required value={password} onChange={e => setPassword(e.target.value)} />
          </label>

          <label className="modal-checkbox-label">
            <input type="checkbox" />
            {t('login.remember')}
          </label>

          <Button type="submit" className="btn-full" disabled={loading}>
            {loading ? '…' : t('login.submit')}
          </Button>
        </form>

        <div className="modal-footer">
          <a href="/forgot-password" className="modal-footer-link">
            {t('login.forgotPassword')}
          </a>
          <p className="modal-footer-text">
            {t('login.registerLink')}{' '}
            <a href="/register" className="modal-footer-register-link">{t('login.registerLinkLabel')}</a>
          </p>
        </div>
      </div>
    </div>
  )
}
