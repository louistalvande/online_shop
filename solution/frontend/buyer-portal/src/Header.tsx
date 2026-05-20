import { useTranslation } from 'react-i18next'
import { AppShell, CartIcon, Button, LangToggle, UserMenu } from '@workspace/theme'
import type { BuyerSession } from './api/authApi'
import { useCartCount } from './hooks/useCartCount'

interface Props {
  session: BuyerSession | null
  onShowLogin: () => void
  onLogout: () => void
  children: React.ReactNode
}

export default function Header({ session, onShowLogin, onLogout, children }: Props) {
  const { t, i18n } = useTranslation()
  const cartCount = useCartCount()

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.home'), href: '/' },
        { label: t('nav.catalog'), href: '/catalog' },
      ]}
      actions={
        <div className="header-actions">
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
              onLogout={onLogout}
            />
          ) : (
            <Button variant="ghost" size="sm" onClick={onShowLogin}>
              {t('nav.login')}
            </Button>
          )}
          <Button variant="ghost" size="sm" aria-label={t('nav.cart')} onClick={() => { window.location.href = '/cart' }}>
            <span className="cart-btn-wrapper">
              <CartIcon size={22} />
              {cartCount > 0 && <span className="cart-badge">{cartCount > 99 ? '99+' : cartCount}</span>}
            </span>
          </Button>
        </div>
      }
    >
      {children}
    </AppShell>
  )
}
