import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'
import type { AdminSession } from './api/authApi'

interface Props {
  session: AdminSession
  onLogout: () => void
  onSettings: () => void
  children: React.ReactNode
}

export default function Header({ session, onLogout, onSettings, children }: Props) {
  const { t, i18n } = useTranslation()

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
            onSettings={onSettings}
            onLogout={onLogout}
          />
        </div>
      }
    >
      {children}
    </AppShell>
  )
}
