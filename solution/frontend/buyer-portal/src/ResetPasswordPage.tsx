import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { resetPassword } from './api/authApi'

interface Props {
  token: string
}

export default function ResetPasswordPage({ token }: Props) {
  const { t } = useTranslation()
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (password !== confirmPassword) {
      setError(t('resetPassword.error.mismatch'))
      return
    }
    setError(null)
    setLoading(true)
    try {
      await resetPassword(token, password)
      setSuccess(true)
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'RESET_TOKEN_INVALID') setError(t('resetPassword.error.tokenInvalid'))
      else if (code === 'PASSWORD_COMPROMISED') setError(t('resetPassword.error.compromised'))
      else setError(t('resetPassword.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="page-overlay">
        <Card className="auth-card">
          <div className="alert-error">{t('resetPassword.error.tokenInvalid')}</div>
          <div className="modal-footer">
            <a href="/forgot-password" className="modal-footer-link">{t('resetPassword.requestNew')}</a>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="page-overlay">
      <Card className="auth-card">
        <h1 className="register-title">{t('resetPassword.title')}</h1>

        {success ? (
          <>
            <div className="alert-success">{t('resetPassword.success')}</div>
            <div className="modal-footer">
              <a href="/login" className="modal-footer-link">{t('resetPassword.backToLogin')}</a>
            </div>
          </>
        ) : (
          <>
            {error && <div className="alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <label className="form-field">
                {t('resetPassword.newPassword')}
                <input
                  className="form-input"
                  type="password"
                  required
                  minLength={12}
                  autoFocus
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                />
              </label>
              <p className="form-hint">{t('activate.passwordHint')}</p>
              <label className="form-field">
                {t('resetPassword.confirmPassword')}
                <input
                  className="form-input"
                  type="password"
                  required
                  minLength={12}
                  value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)}
                />
              </label>
              <Button type="submit" className="btn-full" disabled={loading}>
                {loading ? '…' : t('resetPassword.submit')}
              </Button>
            </form>
          </>
        )}
      </Card>
    </div>
  )
}
