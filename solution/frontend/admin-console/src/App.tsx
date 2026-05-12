import './index.css'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, Card, SettingsIcon, UserIcon, PackageIcon, LangToggle } from '@workspace/theme'

const USERS = [
  { id: 1, name: 'Marie Dupont', email: 'marie@example.com', role: 'Acheteur', since: '2025-03' },
  { id: 2, name: 'Atelier Lumière', email: 'contact@atelier-lumiere.fr', role: 'Vendeur', since: '2025-01' },
  { id: 3, name: 'Jean Martin', email: 'jean@example.com', role: 'Acheteur', since: '2026-01' },
]

const roleColor: Record<string, string> = {
  Acheteur: '#2563eb',
  Vendeur: '#b8431a',
}

export default function App() {
  const { t, i18n } = useTranslation()

  const stats = [
    { label: t('stats.users'), value: '142', icon: <UserIcon size={18} /> },
    { label: t('stats.activeVendors'), value: '8', icon: <PackageIcon size={18} /> },
    { label: t('stats.totalOrders'), value: '1 204', icon: null },
    { label: t('stats.platformRevenue'), value: '5 892 €', icon: null },
  ]

  const tableHeaders = [
    t('users.name'), t('users.email'), t('users.role'), t('users.since'), '',
  ]

  return (
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
          <button
            style={{ background: 'none', border: 'none', display: 'flex', color: 'var(--text)' }}
            aria-label={t('nav.settings')}
          >
            <SettingsIcon size={20} />
          </button>
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
          <Button size="sm">{t('users.add')}</Button>
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
              {USERS.map(u => (
                <tr key={u.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '14px 16px', fontWeight: 600 }}>{u.name}</td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{u.email}</td>
                  <td style={{ padding: '14px 16px' }}>
                    <span style={{ color: roleColor[u.role], fontWeight: 600, fontSize: 13 }}>{u.role}</span>
                  </td>
                  <td style={{ padding: '14px 16px', color: 'var(--text-muted)' }}>{u.since}</td>
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
  )
}
