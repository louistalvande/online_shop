import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, UserIcon, PackageIcon, Snackbar, PencilIcon, BanIcon, CheckCircleIcon, TrashIcon } from '@workspace/theme'
import AccountModal from './components/AccountModal'
import ActionMenu from './components/ActionMenu'
import CarrierFormModal from './components/CarrierFormModal'
import CarrierDeleteModal from './components/CarrierDeleteModal'
import UserDetailModal from './components/UserDetailModal'
import MaintenanceModeCard from './components/MaintenanceModeCard'
import { logout } from './api/authApi'
import {
  listAccounts, revokePasswords, listRevokedAccounts,
  type AccountResponse, type RevokedAccountResponse, type AccountRole,
} from './api/accountApi'
import { listCarriers, deactivateCarrier, activateCarrier, type CarrierResponse } from './api/carrierApi'

interface Props {
  onUnauthorized: () => void
}

export default function DashboardPage({ onUnauthorized }: Props) {
  const { t } = useTranslation()
  const [accounts, setAccounts] = useState<AccountResponse[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editAccount, setEditAccount] = useState<AccountResponse | null>(null)
  const [deleteAccount, setDeleteAccount] = useState<AccountResponse | null>(null)
  const [suspendAccount, setSuspendAccount] = useState<AccountResponse | null>(null)
  const [reactivateAccount, setReactivateAccount] = useState<AccountResponse | null>(null)
  const [viewAccount, setViewAccount] = useState<AccountResponse | null>(null)
  const [carriers, setCarriers] = useState<CarrierResponse[]>([])
  const [showCarrierModal, setShowCarrierModal] = useState(false)
  const [editCarrier, setEditCarrier] = useState<CarrierResponse | null>(null)
  const [deleteCarrierTarget, setDeleteCarrierTarget] = useState<CarrierResponse | null>(null)
  const [snackbar, setSnackbar] = useState<string | null>(null)

  // Password revocation state (US-SEC-04)
  const [revokedAccounts, setRevokedAccounts] = useState<RevokedAccountResponse[]>([])
  const [showRevokeModal, setShowRevokeModal] = useState(false)
  const [revokeRole, setRevokeRole] = useState<AccountRole | 'ADMIN' | ''>('')
  const [revokeEmails, setRevokeEmails] = useState('')
  const [revoking, setRevoking] = useState(false)
  const [revokeError, setRevokeError] = useState<string | null>(null)

  function showSnackbar(message: string) {
    setSnackbar(message)
    setTimeout(() => setSnackbar(null), 3000)
  }

  async function fetchCarriers() {
    try { setCarriers(await listCarriers()) } catch { /* keep current list */ }
  }

  async function fetchAccounts() {
    try {
      setAccounts(await listAccounts())
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'UNAUTHORIZED' || code === 'FORBIDDEN') {
        logout()
        onUnauthorized()
      }
    }
  }

  async function fetchRevokedAccounts() {
    try { setRevokedAccounts(await listRevokedAccounts()) } catch { /* keep current list */ }
  }

  async function handleRevoke() {
    setRevokeError(null)
    const emails = revokeEmails.split(/[\s,]+/).map(e => e.trim()).filter(Boolean)
    if (!revokeRole && emails.length === 0) {
      setRevokeError(t('revoke.error.noTarget'))
      return
    }
    setRevoking(true)
    try {
      await revokePasswords({
        role: revokeRole ? (revokeRole as AccountRole) : undefined,
        emails: emails.length ? emails : undefined,
      })
      setShowRevokeModal(false)
      setRevokeRole('')
      setRevokeEmails('')
      fetchRevokedAccounts()
      showSnackbar(t('revoke.success'))
    } catch {
      setRevokeError(t('revoke.error.generic'))
    } finally {
      setRevoking(false)
    }
  }

  useEffect(() => {
    fetchAccounts()
    fetchCarriers()
    fetchRevokedAccounts()
  }, [])

  const activeAdminCount = accounts.filter(a => a.role === 'ADMIN' && a.status === 'ACTIVE').length

  const stats = [
    { label: t('stats.users'), value: String(accounts.length), icon: <UserIcon size={18} /> },
    {
      label: t('stats.activeVendors'),
      value: String(accounts.filter(a => a.role === 'VENDOR' && a.status === 'ACTIVE').length),
      icon: <PackageIcon size={18} />,
    },
    { label: t('stats.totalOrders'), value: '', icon: null },
    { label: t('stats.platformRevenue'), value: '', icon: null },
  ]

  const tableHeaders = [
    t('users.name'), t('users.email'), t('users.role'), t('users.status'), t('users.since'), '',
  ]

  return (
    <>
      {snackbar && <Snackbar message={snackbar} onDismiss={() => setSnackbar(null)} />}

      {showModal && (
        <AccountModal
          mode="create"
          onClose={() => setShowModal(false)}
          onSuccess={() => { setShowModal(false); fetchAccounts(); showSnackbar(t('snackbar.accountCreated')) }}
        />
      )}

      {editAccount && (
        <AccountModal
          mode="edit"
          account={editAccount}
          onClose={() => setEditAccount(null)}
          onSuccess={() => { setEditAccount(null); fetchAccounts(); showSnackbar(t('snackbar.accountUpdated')) }}
        />
      )}

      {deleteAccount && (
        <AccountModal
          mode="delete"
          account={deleteAccount}
          onClose={() => setDeleteAccount(null)}
          onSuccess={() => { setDeleteAccount(null); fetchAccounts(); showSnackbar(t('snackbar.accountDeleted')) }}
        />
      )}

      {suspendAccount && (
        <AccountModal
          mode="suspend"
          account={suspendAccount}
          onClose={() => setSuspendAccount(null)}
          onSuccess={() => { setSuspendAccount(null); fetchAccounts(); showSnackbar(t('snackbar.accountSuspended')) }}
        />
      )}

      {reactivateAccount && (
        <AccountModal
          mode="reactivate"
          account={reactivateAccount}
          onClose={() => setReactivateAccount(null)}
          onSuccess={() => { setReactivateAccount(null); fetchAccounts(); showSnackbar(t('snackbar.accountReactivated')) }}
        />
      )}

      {viewAccount && (
        <UserDetailModal account={viewAccount} onClose={() => setViewAccount(null)} />
      )}

      {showCarrierModal && (
        <CarrierFormModal
          onClose={() => setShowCarrierModal(false)}
          onSuccess={() => { setShowCarrierModal(false); fetchCarriers(); showSnackbar(t('snackbar.carrierCreated')) }}
        />
      )}

      {editCarrier && (
        <CarrierFormModal
          carrier={editCarrier}
          onClose={() => setEditCarrier(null)}
          onSuccess={() => { setEditCarrier(null); fetchCarriers(); showSnackbar(t('snackbar.carrierUpdated')) }}
        />
      )}

      {deleteCarrierTarget && (
        <CarrierDeleteModal
          carrier={deleteCarrierTarget}
          onClose={() => setDeleteCarrierTarget(null)}
          onSuccess={() => { setDeleteCarrierTarget(null); fetchCarriers(); showSnackbar(t('snackbar.carrierDeleted')) }}
        />
      )}

      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px 64px' }}>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, marginBottom: 32 }}>
          {t('overview.title')}
        </h1>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 20, marginBottom: 48 }}>
          {stats.map(stat => (
            <Card key={stat.label} style={{ padding: '20px 24px' }}>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
                {stat.icon}{stat.label}
              </div>
              <div style={{ fontSize: 28, fontWeight: 700 }}>{stat.value}</div>
            </Card>
          ))}
        </div>

        <div style={{ marginBottom: 48 }} id="settings">
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 22, fontWeight: 700, marginBottom: 20 }}>
            {t('maintenance.section.title')}
          </h2>
          <MaintenanceModeCard />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }} id="users">
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 22, fontWeight: 700 }}>{t('users.title')}</h2>
          <Button size="sm" onClick={() => setShowModal(true)}>{t('users.add')}</Button>
        </div>

        <Card>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)' }}>
                {tableHeaders.map((h, i) => (
                  <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {accounts.map(a => (
                <tr key={a.id} onClick={() => setViewAccount(a)} style={{ borderBottom: '1px solid var(--border)', cursor: 'pointer' }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{a.firstName} {a.lastName}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{a.email}</td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{ fontWeight: 600, fontSize: 13 }}>{t(`users.role.${a.role}`)}</span>
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      fontSize: 12,
                      fontWeight: 600,
                      padding: '3px 8px',
                      borderRadius: 4,
                      background: a.status === 'ACTIVE' ? '#dcfce7' : a.status === 'SUSPENDED' ? '#fef9c3' : a.status === 'PENDING' ? '#dbeafe' : '#f1f5f9',
                      color: a.status === 'ACTIVE' ? '#16a34a' : a.status === 'SUSPENDED' ? '#d97706' : a.status === 'PENDING' ? '#2563eb' : 'var(--text-muted)',
                    }}>
                      {t(`users.status.${a.status}`)}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                    {a.createdAt.slice(0, 7)}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right' }} onClick={e => e.stopPropagation()}>
                    {(() => {
                      const isLastAdmin = a.role === 'ADMIN' && activeAdminCount === 1
                      const isSuspended = a.status === 'SUSPENDED'
                      const canToggle = a.status === 'ACTIVE' || a.status === 'SUSPENDED'
                      return (
                        <ActionMenu actions={[
                          {
                            label: t('users.edit'),
                            icon: <PencilIcon size={15} />,
                            onClick: () => setEditAccount(a),
                          },
                          ...(canToggle ? [{
                            label: isSuspended ? t('users.reactivate') : t('users.suspend'),
                            icon: isSuspended ? <CheckCircleIcon size={15} /> : <BanIcon size={15} />,
                            onClick: () => isSuspended ? setReactivateAccount(a) : setSuspendAccount(a),
                            disabled: isLastAdmin,
                          }] : []),
                          {
                            label: t('users.delete'),
                            icon: <TrashIcon size={15} />,
                            onClick: () => setDeleteAccount(a),
                            danger: true,
                          },
                        ]} />
                      )
                    })()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20, marginTop: 48 }} id="carriers">
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 22, fontWeight: 700 }}>{t('carriers.title')}</h2>
          <Button size="sm" onClick={() => setShowCarrierModal(true)}>{t('carriers.add')}</Button>
        </div>

        <Card>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)' }}>
                {[t('carriers.col.name'), t('carriers.col.countries'), t('carriers.col.trackingUrl'), t('carriers.col.status'), t('carriers.col.since'), ''].map((h, i) => (
                  <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {carriers.map(c => (
                <tr key={c.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{c.name}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{c.supportedCountries.join(', ')}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)', fontSize: 13 }}>
                    <a href={c.trackingUrl} target="_blank" rel="noreferrer" style={{ color: 'var(--text-muted)' }}>{c.trackingUrl}</a>
                  </td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{
                      fontSize: 12, fontWeight: 600, padding: '3px 8px', borderRadius: 4,
                      background: c.active ? '#dcfce7' : '#f1f5f9',
                      color: c.active ? '#16a34a' : 'var(--text-muted)',
                    }}>
                      {c.active ? t('carriers.status.active') : t('carriers.status.inactive')}
                    </span>
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{c.createdAt.slice(0, 7)}</td>
                  <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                    <ActionMenu actions={[
                      {
                        label: t('carriers.edit'),
                        icon: <PencilIcon size={15} />,
                        onClick: () => setEditCarrier(c),
                      },
                      {
                        label: c.active ? t('carriers.deactivate') : t('carriers.activate'),
                        icon: c.active ? <BanIcon size={15} /> : <CheckCircleIcon size={15} />,
                        onClick: async () => {
                          try {
                            if (c.active) await deactivateCarrier(c.id)
                            else await activateCarrier(c.id)
                            fetchCarriers()
                            showSnackbar(c.active ? t('snackbar.carrierDeactivated') : t('snackbar.carrierActivated'))
                          } catch { showSnackbar(t('snackbar.error')) }
                        },
                      },
                      {
                        label: t('carriers.delete'),
                        icon: <TrashIcon size={15} />,
                        onClick: () => setDeleteCarrierTarget(c),
                        danger: true,
                      },
                    ]} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>

        {/* ── Password revocation (US-SEC-04) ─────────────────────────────── */}
        <div style={{ marginTop: 48 }} id="security">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 22, fontWeight: 700 }}>
              {t('revoke.section.title')}
            </h2>
            <Button size="sm" onClick={() => setShowRevokeModal(true)}>{t('revoke.action')}</Button>
          </div>

          {revokedAccounts.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>{t('revoke.empty')}</p>
          ) : (
            <Card>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border)' }}>
                    {[t('users.name'), t('users.email'), t('users.role'), t('users.status'), t('revoke.col.revokedAt'), t('revoke.col.hours')].map((h, i) => (
                      <th key={i} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-muted)', fontSize: 12 }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {revokedAccounts.map(a => (
                    <tr key={a.id} style={{ borderBottom: '1px solid var(--border)' }}>
                      <td style={{ padding: '14px 16px', fontWeight: 600 }}>{a.firstName} {a.lastName}</td>
                      <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{a.email}</td>
                      <td style={{ padding: '14px 16px' }}>{t(`users.role.${a.role}`)}</td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{
                          fontSize: 12, fontWeight: 600, padding: '3px 8px', borderRadius: 4,
                          background: a.status === 'ACTIVE' ? '#dcfce7' : '#fef9c3',
                          color: a.status === 'ACTIVE' ? '#16a34a' : '#d97706',
                        }}>
                          {t(`users.status.${a.status}`)}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                        {a.revokedAt ? new Date(a.revokedAt).toLocaleString() : '—'}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: a.hoursSinceRevocation >= 20 ? 700 : 400, color: a.hoursSinceRevocation >= 20 ? '#c62828' : 'inherit' }}>
                        {a.hoursSinceRevocation}h
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>
          )}
        </div>
      </div>

      {/* ── Revoke passwords modal ─────────────────────────────────────────── */}
      {showRevokeModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: 8, padding: '28px 32px', minWidth: 420, maxWidth: 520 }}>
            <h2 style={{ margin: '0 0 16px', fontSize: 18, fontWeight: 700 }}>{t('revoke.modal.title')}</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: 14, margin: '0 0 20px' }}>{t('revoke.modal.description')}</p>

            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 6 }}>{t('revoke.modal.roleLabel')}</label>
            <select
              value={revokeRole}
              onChange={e => setRevokeRole(e.target.value as AccountRole | 'ADMIN' | '')}
              style={{ width: '100%', padding: '8px 10px', borderRadius: 4, border: '1px solid #ccc', marginBottom: 16, fontSize: 14 }}
            >
              <option value="">{t('revoke.modal.roleNone')}</option>
              <option value="BUYER">{t('users.role.BUYER')}</option>
              <option value="VENDOR">{t('users.role.VENDOR')}</option>
              <option value="ADMIN">{t('users.role.ADMIN')}</option>
            </select>

            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 6 }}>{t('revoke.modal.emailsLabel')}</label>
            <textarea
              value={revokeEmails}
              onChange={e => setRevokeEmails(e.target.value)}
              placeholder={t('revoke.modal.emailsPlaceholder')}
              rows={3}
              style={{ width: '100%', padding: '8px 10px', borderRadius: 4, border: '1px solid #ccc', fontSize: 14, resize: 'vertical', boxSizing: 'border-box' }}
            />

            {revokeError && (
              <p style={{ color: '#c62828', fontSize: 13, margin: '8px 0 0' }}>{revokeError}</p>
            )}

            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 24 }}>
              <Button variant="ghost" size="sm" onClick={() => { setShowRevokeModal(false); setRevokeError(null) }}>
                {t('revoke.modal.cancel')}
              </Button>
              <Button size="sm" onClick={handleRevoke} disabled={revoking}>
                {revoking ? t('revoke.modal.revoking') : t('revoke.modal.confirm')}
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

