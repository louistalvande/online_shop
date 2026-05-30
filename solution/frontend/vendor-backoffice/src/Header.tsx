import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'

export type Page = 'dashboard' | 'catalog' | 'reports' | 'visual-identity'

interface Props {
  onLogout: () => void
  onNavigate: (page: Page) => void
  currentPage: Page
  alertCount: number
  logoUrl?: string | null
  children: React.ReactNode
}

export default function Header({ onLogout, onNavigate, currentPage, alertCount, logoUrl, children }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <AppShell
      appName={t('app.name')}
      logoUrl={logoUrl ?? undefined}
      navLinks={[
        { label: t('nav.dashboard'), href: '#', onClick: () => onNavigate('dashboard') },
        {
          label: t('nav.catalog'), href: '#',
          onClick: () => onNavigate('catalog'),
          badge: currentPage !== 'catalog' ? alertCount : 0,
        },
        { label: t('nav.orders'), href: `${import.meta.env.BASE_URL}orders` },
        { label: t('nav.reports'), href: '#', onClick: () => onNavigate('reports') },
        { label: t('nav.visualIdentity'), href: '#', onClick: () => onNavigate('visual-identity') },
      ]}
      actions={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <LangToggle
            lang={i18n.language}
            onToggle={() => i18n.changeLanguage(({ fr: 'en', en: 'es', es: 'fr' } as Record<string, string>)[i18n.language] ?? 'fr')}
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
