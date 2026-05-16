import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import type { AccountResponse } from '../api/accountApi'

interface Props {
  account: AccountResponse
  onClose: () => void
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 400, maxWidth: 460, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
}

const row: React.CSSProperties = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  padding: '10px 0', borderBottom: '1px solid var(--border)',
}

const rowLabel: React.CSSProperties = {
  fontSize: 12, fontWeight: 600, color: 'var(--text-muted)',
}

const rowValue: React.CSSProperties = {
  fontSize: 14, fontWeight: 500, textAlign: 'right',
}

export default function UserDetailModal({ account, onClose }: Props) {
  const { t } = useTranslation()
  const initials = `${account.firstName[0]}${account.lastName[0]}`.toUpperCase()
  const name = `${account.firstName} ${account.lastName}`

  const statusColor =
    account.status === 'ACTIVE' ? '#16a34a' :
    account.status === 'SUSPENDED' ? '#d97706' :
    account.status === 'PENDING' ? '#2563eb' : 'var(--text-muted)'

  const statusBg =
    account.status === 'ACTIVE' ? '#dcfce7' :
    account.status === 'SUSPENDED' ? '#fef9c3' :
    account.status === 'PENDING' ? '#dbeafe' : '#f1f5f9'

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 28 }}>
          <div style={{
            width: 52, height: 52, borderRadius: '50%',
            background: 'var(--surface-hover, #f1f5f9)',
            border: '2px solid var(--border)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 17, fontWeight: 700, color: 'var(--text)', flexShrink: 0,
          }}>
            {initials}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 17 }}>{name}</div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 2 }}>{account.email}</div>
          </div>
        </div>

        <div>
          <div style={row}>
            <span style={rowLabel}>{t('accountModal.role')}</span>
            <span style={rowValue}>{t(`users.role.${account.role}`)}</span>
          </div>
          <div style={row}>
            <span style={rowLabel}>{t('users.status')}</span>
            <span style={{
              ...rowValue,
              fontSize: 12, fontWeight: 600, padding: '3px 8px', borderRadius: 4,
              background: statusBg, color: statusColor,
            }}>
              {t(`users.status.${account.status}`)}
            </span>
          </div>
          <div style={row}>
            <span style={rowLabel}>{t('accountModal.language')}</span>
            <span style={rowValue}>{t(`accountModal.language.${account.language}`)}</span>
          </div>
          <div style={{ ...row, borderBottom: 'none' }}>
            <span style={rowLabel}>{t('users.since')}</span>
            <span style={{ ...rowValue, color: 'var(--text-muted)' }}>{account.createdAt.slice(0, 10)}</span>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 24 }}>
          <Button size="sm" variant="ghost" onClick={onClose}>{t('userDetail.close')}</Button>
        </div>
      </div>
    </div>
  )
}
