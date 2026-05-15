import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import {
  createAccount,
  updateAccount,
  type AccountResponse,
  type AccountRole,
  type AccountLanguage,
} from '../api/accountApi'

type Props =
  | { mode: 'create'; onClose: () => void; onSuccess: () => void }
  | { mode: 'edit'; account: AccountResponse; onClose: () => void; onSuccess: () => void }

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 400, maxWidth: 480, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
}

const fieldStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: 4, marginBottom: 16,
}

const labelStyle: React.CSSProperties = {
  fontSize: 12, fontWeight: 600, color: 'var(--text-muted)',
}

const inputStyle: React.CSSProperties = {
  padding: '8px 12px', borderRadius: 4, border: '1px solid var(--border)',
  fontSize: 14, background: 'var(--surface)', color: 'var(--text)', width: '100%',
  boxSizing: 'border-box',
}

const errorBox: React.CSSProperties = {
  background: '#fef2f2', border: '1px solid #fca5a5',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#b91c1c', marginBottom: 16,
}

export default function AccountModal(props: Props) {
  const { t } = useTranslation()
  const isEdit = props.mode === 'edit'

  const [firstName, setFirstName] = useState(isEdit ? props.account.firstName : '')
  const [lastName, setLastName] = useState(isEdit ? props.account.lastName : '')
  const [role, setRole] = useState<AccountRole>(isEdit ? props.account.role as AccountRole : 'BUYER')
  const [email, setEmail] = useState('')
  const [language, setLanguage] = useState<AccountLanguage>(isEdit ? props.account.language : 'FR')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      if (props.mode === 'create') {
        await createAccount({ firstName, lastName, email, role })
      } else {
        await updateAccount(props.account.id, { firstName, lastName, role, language })
      }
      props.onSuccess()
    } catch (err: unknown) {
      if (props.mode === 'create' && err instanceof Error && (err as { code?: string }).code === 'EMAIL_ALREADY_USED') {
        setError(t('accountModal.error.emailUsed'))
      } else {
        setError(t('accountModal.error.generic'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay} onClick={props.onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 4 }}>
          {t(`accountModal.title.${props.mode}`)}
        </h2>
        {isEdit && (
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 24 }}>{props.account.email}</p>
        )}
        {!isEdit && <div style={{ marginBottom: 24 }} />}

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={fieldStyle}>
            <span style={labelStyle}>{t('accountModal.firstName')}</span>
            <input style={inputStyle} required value={firstName} onChange={e => setFirstName(e.target.value)} />
          </div>

          <div style={fieldStyle}>
            <span style={labelStyle}>{t('accountModal.lastName')}</span>
            <input style={inputStyle} required value={lastName} onChange={e => setLastName(e.target.value)} />
          </div>

          {props.mode === 'create' && (
            <div style={fieldStyle}>
              <span style={labelStyle}>{t('accountModal.email')}</span>
              <input style={inputStyle} type="email" required value={email} onChange={e => setEmail(e.target.value)} />
            </div>
          )}

          <div style={fieldStyle}>
            <span style={labelStyle}>{t('accountModal.role')}</span>
            <select style={inputStyle} value={role} onChange={e => setRole(e.target.value as AccountRole)}>
              <option value="BUYER">{t('users.role.BUYER')}</option>
              <option value="VENDOR">{t('users.role.VENDOR')}</option>
            </select>
          </div>

          {props.mode === 'edit' && (
            <div style={fieldStyle}>
              <span style={labelStyle}>{t('accountModal.language')}</span>
              <select style={inputStyle} value={language} onChange={e => setLanguage(e.target.value as AccountLanguage)}>
                <option value="FR">{t('accountModal.language.FR')}</option>
                <option value="EN">{t('accountModal.language.EN')}</option>
              </select>
            </div>
          )}

          {props.mode === 'create' && (
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 16 }}>
              {t('accountModal.activationNote')}
            </p>
          )}

          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
            <Button type="button" variant="ghost" size="sm" onClick={props.onClose}>
              {t('accountModal.cancel')}
            </Button>
            <Button type="submit" size="sm" disabled={loading}>
              {loading ? '…' : t(`accountModal.submit.${props.mode}`)}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
