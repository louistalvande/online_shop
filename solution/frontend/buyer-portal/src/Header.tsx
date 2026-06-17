import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, CartIcon, LangToggle, UserMenu, SearchIcon } from '@workspace/theme'
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
  const [quickSearch, setQuickSearch] = useState('')

  function handleQuickSearch(e: React.FormEvent) {
    e.preventDefault()
    const q = quickSearch.trim()
    window.location.href = q ? `/catalog?q=${encodeURIComponent(q)}` : '/catalog'
  }

  return (
    <AppShell
      appName={t('app.name')}
      brandName={brandName}
      logoUrl={logoUrl ?? undefined}
      footerLinks={footerLinks}
      footerNotice={footerNotice}
      centeredBrand
      leftNavLinks={[
        { label: t('nav.home'), href: '/' },
      ]}
      navLinks={[
        { label: t('nav.catalog'), href: '/catalog' },
      ]}
      actions={
        <div className="header-actions">
          <form className="quick-search quick-search--header" onSubmit={handleQuickSearch}>
            <button type="submit" className="quick-search-btn" aria-label={t('catalog.search.submit')}>
              <SearchIcon size={18} />
            </button>
            <input
              type="search"
              className="quick-search-input"
              value={quickSearch}
              onChange={e => setQuickSearch(e.target.value)}
              placeholder={t('catalog.search.placeholder')}
            />
          </form>
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
