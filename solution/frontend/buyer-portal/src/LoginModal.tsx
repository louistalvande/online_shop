import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { login } from './api/authApi'

interface Props {
  onClose: () => void
  onLogin: () => void
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
