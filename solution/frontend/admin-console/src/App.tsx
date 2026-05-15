import './index.css'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, Card, UserIcon, PackageIcon, LangToggle, UserMenu, Snackbar, IconButton, TrashIcon, DotsHorizontalIcon } from '@workspace/theme'
import AccountModal from './components/AccountModal'
import LoginPage from './LoginPage'
import { getSession, logout } from './api/authApi'
import { listAccounts, type AccountResponse } from './api/accountApi'

export default function App() {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState(getSession)
  const [accounts, setAccounts] = useState<AccountResponse[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editAccount, setEditAccount] = useState<AccountResponse | null>(null)
  const [deleteAccount, setDeleteAccount] = useState<AccountResponse | null>(null)
  const [suspendAccount, setSuspendAccount] = useState<AccountResponse | null>(null)
  const [reactivateAccount, setReactivateAccount] = useState<AccountResponse | null>(null)
  const [snackbar, setSnackbar] = useState<string | null>(null)

  function showSnackbar(message: string) {
    setSnackbar(message)
    setTimeout(() => setSnackbar(null), 3000)
  }

  async function fetchAccounts() {
    try {
      setAccounts(await listAccounts())
    } catch (err: unknown) {
      const code = err instanceof Error ? (err as { code?: string }).code : undefined
      if (code === 'UNAUTHORIZED' || code === 'FORBIDDEN') {
        logout()
        setSession(null)
      }
      // other errors: backend may be unreachable in dev — keep current list
    }
  }

  useEffect(() => {
    if (session) fetchAccounts()
  }, [session])

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  const activeAdminCount = accounts.filter(a => a.role === 'ADMIN' && a.status === 'ACTIVE').length

  const stats = [
    { label: t('stats.users'), value: String(accounts.length), icon: <UserIcon size={18} /> },
    {
      label: t('stats.activeVendors'),
      value: String(accounts.filter(a => a.role === 'VENDOR' && a.status === 'ACTIVE').length),
      icon: <PackageIcon size={18} />,
    },
    { label: t('stats.totalOrders'), value: '—', icon: null },
    { label: t('stats.platformRevenue'), value: '—', icon: null },
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

      <AppShell
        appName={t('app.name')}
        navLinks={[
          { label: t('nav.overview'), href: '#' },
          { label: t('nav.users'), href: '#users' },
        ]}
        actions={
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <LangToggle
              lang={i18n.language}
              onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')}
            />
            <UserMenu
              label={t('nav.account')}
              email={session.email}
              settingsLabel={t('nav.configuration')}
              logoutLabel={t('nav.logout')}
              onLogout={() => { logout(); setSession(null) }}
            />
          </div>
        }
      >
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
                  <tr key={a.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 600 }}>{a.firstName} {a.lastName}</td>
                    <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{a.email}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ fontWeight: 600, fontSize: 13 }}>{t(`users.role.${a.role}`)}</span>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        {(a.status === 'ACTIVE' || a.status === 'SUSPENDED') && (() => {
                          const isLastAdmin = a.role === 'ADMIN' && activeAdminCount === 1
                          const isSuspended = a.status === 'SUSPENDED'
                          return (
                            <Button variant="ghost" size="sm"
                              onClick={() => !isLastAdmin && (isSuspended ? setReactivateAccount(a) : setSuspendAccount(a))}
                              disabled={isLastAdmin}
                              title={isLastAdmin ? t('users.suspend.lastAdmin') : undefined}
                              style={{
                                color: isLastAdmin ? 'var(--text-muted)' : isSuspended ? '#16a34a' : '#d97706',
                                cursor: isLastAdmin ? 'not-allowed' : undefined,
                              }}>
                              {isSuspended ? t('users.reactivate') : t('users.suspend')}
                            </Button>
                          )
                        })()}
                      </div>
                    </td>
                    <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                      {a.createdAt.slice(0, 7)}
                    </td>
                    <td style={{ padding: '14px 16px', display: 'flex', gap: 8 }}>
                      <IconButton onClick={() => setEditAccount(a)} title={t('users.edit')} style={{ color: 'var(--text-muted)' }}><DotsHorizontalIcon size={16} /></IconButton>
                      <IconButton onClick={() => setDeleteAccount(a)} title={t('users.delete')} style={{ color: 'var(--text-muted)' }}><TrashIcon size={16} /></IconButton>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </div>
      </AppShell>
    </>
  )
}
