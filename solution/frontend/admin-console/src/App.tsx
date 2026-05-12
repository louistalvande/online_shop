import './index.css'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, Card, SettingsIcon, UserIcon, PackageIcon, LangToggle, IconButton } from '@workspace/theme'
import CreateAccountModal from './components/CreateAccountModal'
import LoginPage from './LoginPage'
import { getSession, logout } from './api/authApi'
import { listAccounts, type AccountResponse } from './api/accountApi'

export default function App() {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState(getSession)
  const [accounts, setAccounts] = useState<AccountResponse[]>([])
  const [showModal, setShowModal] = useState(false)

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  async function fetchAccounts() {
    try {
      setAccounts(await listAccounts())
    } catch {
      // backend may be unreachable in dev — keep current list
    }
  }

  useEffect(() => { fetchAccounts() }, [])

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
      {showModal && (
        <CreateAccountModal
          onClose={() => setShowModal(false)}
          onCreated={() => { setShowModal(false); fetchAccounts() }}
        />
      )}

      <AppShell
        appName={t('app.name')}
        navLinks={[
          { label: t('nav.overview'), href: '#' },
          { label: t('nav.users'), href: '#users' },
          { label: t('nav.settings'), href: '#settings' },
        ]}
        actions={
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <LangToggle
              lang={i18n.language}
              onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')}
            />
            <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{session.email}</span>
            <IconButton aria-label={t('nav.settings')}><SettingsIcon size={20} /></IconButton>
            <IconButton aria-label={t('nav.logout')} onClick={() => { logout(); setSession(null) }}>
              <UserIcon size={22} />
            </IconButton>
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
                    <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                      {t(`users.status.${a.status}`)}
                    </td>
                    <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>
                      {a.createdAt.slice(0, 7)}
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <Button variant="ghost" size="sm">{t('users.edit')}</Button>
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
