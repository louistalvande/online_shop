import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { forgotPassword } from './api/authApi'

export default function ForgotPasswordPage() {
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await forgotPassword(email)
    } catch {
      // Silently ignore — always show the same confirmation message to prevent email enumeration
    } finally {
      setLoading(false)
      setSubmitted(true)
    }
  }

  return (
    <div className="page-overlay">
      <Card className="auth-card">
        <h1 className="register-title">{t('forgotPassword.title')}</h1>

        {submitted ? (
          <div className="alert-success">{t('forgotPassword.success')}</div>
        ) : (
          <>
            <p className="form-hint">{t('forgotPassword.subtitle')}</p>
            <form onSubmit={handleSubmit}>
              <label className="form-field">
                {t('forgotPassword.email')}
                <input
                  className="form-input"
                  type="email"
                  required
                  autoFocus
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                />
              </label>
              <Button type="submit" className="btn-full" disabled={loading}>
                {loading ? '…' : t('forgotPassword.submit')}
              </Button>
            </form>
          </>
        )}

        <div className="modal-footer">
          <a href="/login" className="modal-footer-link">{t('forgotPassword.backToLogin')}</a>
        </div>
      </Card>
    </div>
  )
}
