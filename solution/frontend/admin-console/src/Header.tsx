import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, UserMenu } from '@workspace/theme'
import type { AdminSession } from './api/authApi'

interface Props {
  session: AdminSession
  onLogout: () => void
  onSettings: () => void
  shopName?: string
  logoUrl?: string | null
  children: React.ReactNode
}

export default function Header({ session, onLogout, onSettings, shopName, logoUrl, children }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <AppShell
      appName={t('app.name')}
      brandName={shopName}
      logoUrl={logoUrl ?? undefined}
      navLinks={[
        { label: t('nav.overview'),  href: '#' },
        { label: t('nav.users'),     href: '#users' },
        { label: t('nav.carriers'),  href: '#carriers' },
        { label: t('nav.security'),  href: '#security' },
        { label: t('nav.auditLog'),  href: '#audit' },
      ]}
      actions={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <LangToggle lang={i18n.language} onChange={lang => i18n.changeLanguage(lang)} />
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
