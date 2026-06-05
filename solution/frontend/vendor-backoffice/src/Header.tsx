import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'

export type Page = 'dashboard' | 'catalog' | 'reports' | 'visual-identity' | 'announcements' | 'campaigns'

interface Props {
  onLogout: () => void
  onNavigate?: (page: Page) => void
  currentPage?: Page
  alertCount?: number
  logoUrl?: string | null
  children: React.ReactNode
}

export default function Header({ onLogout, onNavigate, currentPage, alertCount = 0, logoUrl, children }: Props) {
  const { t, i18n } = useTranslation()
  const base = import.meta.env.BASE_URL.replace(/\/$/, '')
  function nav(page: Page) {
    if (onNavigate) { onNavigate(page) } else { window.location.href = `${base}/${page}` }
  }

  return (
    <AppShell
      appName={t('app.name')}
      logoUrl={logoUrl ?? undefined}
      navLinks={[
        { label: t('nav.dashboard'), href: '#', onClick: () => nav('dashboard') },
        {
          label: t('nav.catalog'), href: '#',
          onClick: () => nav('catalog'),
          badge: currentPage !== 'catalog' ? alertCount : 0,
        },
        { label: t('nav.orders'), href: `${import.meta.env.BASE_URL}orders` },
        { label: t('nav.reports'), href: '#', onClick: () => nav('reports') },
        { label: t('nav.announcements'), href: '#', onClick: () => nav('announcements') },
        { label: t('nav.campaigns'), href: '#', onClick: () => nav('campaigns') },
        { label: t('nav.visualIdentity'), href: '#', onClick: () => nav('visual-identity') },
      ]}
      actions={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <LangToggle lang={i18n.language} onChange={lang => i18n.changeLanguage(lang)} />
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
