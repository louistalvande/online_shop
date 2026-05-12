import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { createAccount, type CreateAccountRequest, type AccountRole } from '../api/accountApi'

interface Props {
  onClose: () => void
  onCreated: () => void
}

const EMPTY: CreateAccountRequest = {
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  role: 'BUYER',
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 400, maxWidth: 480, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
}

const field: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: 4, marginBottom: 16,
}

const label: React.CSSProperties = {
  fontSize: 12, fontWeight: 600, color: 'var(--text-muted)',
}

const input: React.CSSProperties = {
  padding: '8px 12px', borderRadius: 4, border: '1px solid var(--border)',
  fontSize: 14, background: 'var(--surface)', color: 'var(--text)', width: '100%',
  boxSizing: 'border-box',
}

const errorBox: React.CSSProperties = {
  background: '#fef2f2', border: '1px solid #fca5a5',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#b91c1c', marginBottom: 16,
}

export default function CreateAccountModal({ onClose, onCreated }: Props) {
  const { t } = useTranslation()
  const [form, setForm] = useState<CreateAccountRequest>(EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  function set(field: keyof CreateAccountRequest, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await createAccount(form)
      onCreated()
    } catch (err: unknown) {
      if (err instanceof Error && (err as {code?: string}).code === 'EMAIL_ALREADY_USED') {
        setError(t('createAccount.error.emailUsed'))
      } else {
        setError(t('createAccount.error.generic'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
          {t('createAccount.title')}
        </h2>

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={field}>
            <span style={label}>{t('createAccount.firstName')}</span>
            <input style={input} required value={form.firstName}
              onChange={e => set('firstName', e.target.value)} />
          </div>

          <div style={field}>
            <span style={label}>{t('createAccount.lastName')}</span>
            <input style={input} required value={form.lastName}
              onChange={e => set('lastName', e.target.value)} />
          </div>

          <div style={field}>
            <span style={label}>{t('createAccount.email')}</span>
            <input style={input} type="email" required value={form.email}
              onChange={e => set('email', e.target.value)} />
          </div>

          <div style={field}>
            <span style={label}>{t('createAccount.password')}</span>
            <input style={input} type="password" required minLength={8} value={form.password}
              onChange={e => set('password', e.target.value)} />
          </div>

          <div style={field}>
            <span style={label}>{t('createAccount.role')}</span>
            <select style={input} value={form.role}
              onChange={e => set('role', e.target.value as AccountRole)}>
              <option value="BUYER">{t('createAccount.role.BUYER')}</option>
              <option value="VENDOR">{t('createAccount.role.VENDOR')}</option>
            </select>
          </div>

          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
            <Button type="button" variant="ghost" size="sm" onClick={onClose}>
              {t('createAccount.cancel')}
            </Button>
            <Button type="submit" size="sm" disabled={loading}>
              {loading ? '…' : t('createAccount.submit')}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
