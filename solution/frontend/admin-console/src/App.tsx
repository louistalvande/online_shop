import './index.css'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'
import DashboardPage from './DashboardPage'
import LoginPage from './LoginPage'
import { getSession, logout, type AdminSession } from './api/authApi'

export default function App() {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState<AdminSession | null>(getSession)

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.overview'), href: '#' },
        { label: t('nav.users'), href: '#users' },
        { label: t('nav.carriers'), href: '#carriers' },
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
      <DashboardPage session={session} onUnauthorized={() => setSession(null)} />
    </AppShell>
  )
}
