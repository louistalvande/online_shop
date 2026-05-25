import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'

export type Page = 'dashboard' | 'catalog' | 'reports' | 'announcements'

interface Props {
  onLogout: () => void
  onNavigate: (page: Page) => void
  currentPage: Page
  alertCount: number
  children: React.ReactNode
}

export default function Header({ onLogout, onNavigate, currentPage, alertCount, children }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.dashboard'), href: '#', onClick: () => onNavigate('dashboard') },
        {
          label: t('nav.catalog'), href: '#',
          onClick: () => onNavigate('catalog'),
          badge: currentPage !== 'catalog' ? alertCount : 0,
        },
        { label: t('nav.reports'), href: '#', onClick: () => onNavigate('reports') },
        { label: t('nav.announcements'), href: '#', onClick: () => onNavigate('announcements') },
      ]}
      actions={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <LangToggle
            lang={i18n.language}
            onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')}
          />
          <UserMenu
            label={t('nav.account')}
            settingsLabel={t('nav.profile')}
            logoutLabel={t('nav.logout')}
            onSettings={() => { window.location.href = `${import.meta.env.BASE_URL}profile` }}
            onLogout={onLogout}
          />
        </div>
      }
    >
      {children}
    </AppShell>
  )
}
