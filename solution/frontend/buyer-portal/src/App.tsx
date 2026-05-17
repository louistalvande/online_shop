import './index.css'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, CartIcon, Button, LangToggle, UserMenu } from '@workspace/theme'
import LoginModal from './LoginModal'
import HomePage from './HomePage'
import { getSession, logout, type BuyerSession } from './api/authApi'

interface Props {
  openLogin?: boolean
}

export default function App({ openLogin = false }: Props) {
  const { t, i18n } = useTranslation()
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [showLogin, setShowLogin] = useState(openLogin)

  return (
    <>
      {showLogin && (
        <LoginModal
          onClose={() => setShowLogin(false)}
          onLogin={() => { setSession(getSession()); setShowLogin(false) }}
        />
      )}

      <AppShell
        appName={t('app.name')}
        navLinks={[
          { label: t('nav.home'), href: '#' },
          { label: t('nav.catalog'), href: '#catalogue' },
        ]}
        actions={
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <LangToggle
              lang={i18n.language}
              onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')}
            />
            {session ? (
              <UserMenu
                label={t('nav.account')}
                email={session.email}
                settingsLabel={t('nav.profile')}
                logoutLabel={t('nav.logout')}
                onSettings={() => { window.location.href = '/profile' }}
                onLogout={() => { logout(); setSession(null) }}
              />
            ) : (
              <Button variant="ghost" size="sm" onClick={() => setShowLogin(true)}>
                {t('nav.login')}
              </Button>
            )}
            <Button variant="ghost" size="sm" aria-label={t('nav.cart')}>
              <CartIcon size={22} />
            </Button>
          </div>
        }
      >
        <HomePage />
      </AppShell>
    </>
  )
}
