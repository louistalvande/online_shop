import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import {
  createAccount,
  updateAccount,
  deleteAccount,
  suspendAccount,
  reactivateAccount,
  type AccountResponse,
  type AccountRole,
  type AccountLanguage,
} from '../api/accountApi'

type Props =
  | { mode: 'create'; onClose: () => void; onSuccess: () => void }
  | { mode: 'edit'; account: AccountResponse; onClose: () => void; onSuccess: () => void }
  | { mode: 'delete'; account: AccountResponse; onClose: () => void; onSuccess: () => void }
  | { mode: 'suspend'; account: AccountResponse; onClose: () => void; onSuccess: () => void }
  | { mode: 'reactivate'; account: AccountResponse; onClose: () => void; onSuccess: () => void }

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
  const isConfirmAction = props.mode === 'delete' || props.mode === 'suspend' || props.mode === 'reactivate'

  const [firstName, setFirstName] = useState(isEdit ? props.account.firstName : '')
  const [lastName, setLastName] = useState(isEdit ? props.account.lastName : '')
  const [role, setRole] = useState<AccountRole>(isEdit ? props.account.role as AccountRole : 'BUYER')
  const [email, setEmail] = useState('')
  const [language, setLanguage] = useState<AccountLanguage>(isEdit ? props.account.language : 'FR')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleConfirmAction() {
    if (!isConfirmAction) return
    setError(null)
    setLoading(true)
    try {
      if (props.mode === 'delete') await deleteAccount(props.account.id)
      else if (props.mode === 'suspend') await suspendAccount(props.account.id)
      else await reactivateAccount(props.account.id)
      props.onSuccess()
    } catch {
      setError(t('accountModal.error.generic'))
    } finally {
      setLoading(false)
    }
  }

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

  if (isConfirmAction) {
    const accentColor =
      props.mode === 'delete' ? '#dc2626' :
      props.mode === 'suspend' ? '#d97706' : '#16a34a'
    const accentBg =
      props.mode === 'delete' ? '#fef2f2' :
      props.mode === 'suspend' ? '#fffbeb' : '#f0fdf4'
    const accentBorder =
      props.mode === 'delete' ? '#fca5a5' :
      props.mode === 'suspend' ? '#fcd34d' : '#86efac'
    const initials = `${props.account.firstName[0]}${props.account.lastName[0]}`.toUpperCase()
    const name = `${props.account.firstName} ${props.account.lastName}`
    return (
      <div style={overlay} onClick={props.onClose}>
        <div style={{ ...modal, maxWidth: 420 }} onClick={e => e.stopPropagation()}>
          <div style={{ height: 4, background: accentColor, borderRadius: '8px 8px 0 0', margin: '-32px -32px 28px' }} />

          <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 20 }}>
            <div style={{
              width: 44, height: 44, borderRadius: '50%', background: accentBg,
              border: `2px solid ${accentBorder}`, display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 15, fontWeight: 700, color: accentColor, flexShrink: 0,
            }}>
              {initials}
            </div>
            <div>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{name}</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{props.account.email}</div>
            </div>
          </div>

          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 18, fontWeight: 700, marginBottom: 8 }}>
            {t(`accountModal.title.${props.mode}`)}
          </h2>
          <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 24, lineHeight: 1.5 }}>
            {t(`accountModal.${props.mode}.confirm`, { name })}
          </p>

          {error && <div style={errorBox}>{error}</div>}

          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
            <Button type="button" variant="ghost" size="sm" onClick={props.onClose}>
              {t('accountModal.cancel')}
            </Button>
            <Button type="button" size="sm" disabled={loading} onClick={handleConfirmAction}
              style={{ background: accentColor, borderColor: accentColor, color: '#fff' }}>
              {loading ? '…' : t(`accountModal.${props.mode}.submit`)}
            </Button>
          </div>
        </div>
      </div>
    )
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
        {props.mode === 'create' && <div style={{ marginBottom: 24 }} />}

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={fieldStyle}>
            <label htmlFor="account-firstName" style={labelStyle}>{t('accountModal.firstName')}</label>
            <input id="account-firstName" style={inputStyle} required value={firstName} onChange={e => setFirstName(e.target.value)} />
          </div>

          <div style={fieldStyle}>
            <label htmlFor="account-lastName" style={labelStyle}>{t('accountModal.lastName')}</label>
            <input id="account-lastName" style={inputStyle} required value={lastName} onChange={e => setLastName(e.target.value)} />
          </div>

          {props.mode === 'create' && (
            <div style={fieldStyle}>
              <label htmlFor="account-email" style={labelStyle}>{t('accountModal.email')}</label>
              <input id="account-email" style={inputStyle} type="email" required value={email} onChange={e => setEmail(e.target.value)} />
            </div>
          )}

          <div style={fieldStyle}>
            <label htmlFor="account-role" style={labelStyle}>{t('accountModal.role')}</label>
            <select id="account-role" style={inputStyle} value={role} onChange={e => setRole(e.target.value as AccountRole)}>
              <option value="BUYER">{t('users.role.BUYER')}</option>
              <option value="VENDOR">{t('users.role.VENDOR')}</option>
            </select>
          </div>

          {props.mode === 'edit' && (
            <div style={fieldStyle}>
              <label htmlFor="account-language" style={labelStyle}>{t('accountModal.language')}</label>
              <select id="account-language" style={inputStyle} value={language} onChange={e => setLanguage(e.target.value as AccountLanguage)}>
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
