import { useTranslation } from 'react-i18next'
import { AppShell, Button, CartIcon, LangToggle, UserMenu } from '@workspace/theme'
import type { BuyerSession } from './api/authApi'
import { useCartCount } from './hooks/useCartCount'
import { useShopName } from './hooks/useShopName'
import { useFooterLinks } from './hooks/useFooterLinks'
import { useFooterNotice } from './hooks/useFooterNotice'

interface Props {
  session: BuyerSession | null
  onShowLogin: () => void
  onLogout: () => void
  children: React.ReactNode
  logoUrl?: string | null
}

export default function Header({ session, onShowLogin, onLogout, children, logoUrl }: Props) {
  const { t, i18n } = useTranslation()
  const cartCount = useCartCount()
  const brandName = useShopName()
  const footerLinks = useFooterLinks()
  const footerNotice = useFooterNotice()

  return (
    <AppShell
      appName={t('app.name')}
      brandName={brandName}
      logoUrl={logoUrl ?? undefined}
      footerLinks={footerLinks}
      footerNotice={footerNotice}
      navLinks={[
        { label: t('nav.home'), href: '/' },
        { label: t('nav.catalog'), href: '/catalog' },
      ]}
      actions={
        <div className="header-actions">
          <LangToggle lang={i18n.language} onChange={lang => i18n.changeLanguage(lang)} />
          {session ? (
            <UserMenu
              label={t('nav.account')}
              email={session.email}
              settingsLabel={t('nav.profile')}
              ordersLabel={t('nav.orders')}
              logoutLabel={t('nav.logout')}
              onSettings={() => { window.location.href = '/profile' }}
              onOrders={() => { window.location.href = '/my-orders' }}
              onLogout={onLogout}
            />
          ) : (
            <UserMenu
              label={t('nav.account')}
              loginLabel={t('nav.login')}
              onLogin={onShowLogin}
            />
          )}
          <Button variant="ghost" size="sm" className="cart-icon-btn" aria-label={t('nav.cart')} onClick={() => { window.location.href = '/cart' }}>
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
