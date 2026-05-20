import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { register } from './api/authApi'

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
    <div className="page-overlay">
      <Card className="auth-card">
        <h1 className="register-title">{t('register.title')}</h1>

        {success ? (
          <div className="alert-success">{t('register.success')}</div>
        ) : (
          <>
            {error && <div className="alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <label className="form-field">
                {t('register.firstName')}
                <input className="form-input" required value={firstName} onChange={e => setFirstName(e.target.value)} />
              </label>
              <label className="form-field">
                {t('register.lastName')}
                <input className="form-input" required value={lastName} onChange={e => setLastName(e.target.value)} />
              </label>
              <label className="form-field">
                {t('register.email')}
                <input className="form-input" type="email" required value={email} onChange={e => setEmail(e.target.value)} />
              </label>
              <label className="form-field">
                {t('register.password')}
                <input className="form-input" type="password" required minLength={12} value={password} onChange={e => setPassword(e.target.value)} />
              </label>
              <label className="form-field">
                {t('register.confirmPassword')}
                <input className="form-input" type="password" required value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
              </label>
              <p className="register-activation-note">{t('register.activationNote')}</p>
              <Button type="submit" className="btn-full" disabled={loading}>
                {loading ? '…' : t('register.submit')}
              </Button>
            </form>
          </>
        )}

        <p className="register-footer">
          {t('register.loginLink')}{' '}
          <a href="/login" className="register-login-link">{t('register.loginLinkLabel')}</a>
        </p>
      </Card>
    </div>
  )
}
