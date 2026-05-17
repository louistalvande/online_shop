import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'

interface Props {
  onLogout: () => void
  children: React.ReactNode
}

export default function Header({ onLogout, children }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.dashboard'), href: '#' },
        { label: t('nav.orders'), href: '#orders' },
        { label: t('nav.catalog'), href: '#catalogue' },
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
