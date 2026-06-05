import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, Button, UserMenu } from '@workspace/theme'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { getCart, updateCartItem, removeCartItem, type CartData } from './api/cartApi'
import LoginModal from './LoginModal'
import { useShopName } from './hooks/useShopName'
import { useLogoUrl } from './hooks/useLogoUrl'

export default function CartPage() {
  const { t, i18n } = useTranslation()
  const brandName = useShopName()
  const logoUrl = useLogoUrl()
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [showLogin, setShowLogin] = useState(false)
  const [cart, setCart] = useState<CartData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [updating, setUpdating] = useState<string | null>(null)

  useEffect(() => {
    if (!session) {
      setLoading(false)
      return
    }
    loadCart()
  }, [session])

  async function loadCart() {
    setLoading(true)
    setError(null)
    try {
      setCart(await getCart())
    } catch {
      setError(t('cart.error.load'))
    } finally {
      setLoading(false)
    }
  }

  async function handleQuantityChange(itemId: string, newQty: number) {
    setUpdating(itemId)
    setError(null)
    try {
      setCart(await updateCartItem(itemId, newQty))
    } catch (e: unknown) {
      const code = e instanceof Error ? e.message : ''
      setError(code === 'OUT_OF_STOCK' ? t('cart.error.outOfStock') : t('cart.error.update'))
    } finally {
      setUpdating(null)
    }
  }

  async function handleRemove(itemId: string) {
    setUpdating(itemId)
    setError(null)
    try {
      setCart(await removeCartItem(itemId))
    } catch {
      setError(t('cart.error.update'))
    } finally {
      setUpdating(null)
    }
  }

  const headerActions = (
    <div className="header-actions">
      <LangToggle
        lang={i18n.language}
        onChange={lang => i18n.changeLanguage(lang)}
      />
      {session ? (
        <UserMenu
          label={t('nav.account')}
          email={session.email}
          settingsLabel={t('nav.profile')}
          logoutLabel={t('nav.logout')}
          onSettings={() => { window.location.href = '/profile' }}
          onLogout={() => { logout(); setSession(null); setCart(null); window.dispatchEvent(new Event('session-changed')) }}
        />
      ) : (
        <Button variant="ghost" size="sm" onClick={() => setShowLogin(true)}>
          {t('nav.login')}
        </Button>
      )}
    </div>
  )

  return (
    <>
      {showLogin && (
        <LoginModal
          onClose={() => setShowLogin(false)}
          onLogin={(s) => { setSession(s); setShowLogin(false) }}
        />
      )}
      <AppShell
        appName={t('app.name')}
        brandName={brandName}
        logoUrl={logoUrl}
        navLinks={[
          { label: t('nav.home'), href: '/' },
          { label: t('nav.catalog'), href: '/catalog' },
        ]}
        actions={headerActions}
      >
        <div className="cart-container">
          <h1>{t('cart.title')}</h1>

          {!session && (
            <p>
              {t('cart.loginRequired')}{' '}
              <button onClick={() => setShowLogin(true)}>{t('nav.login')}</button>
            </p>
          )}

          {session && loading && <p>{t('cart.loading')}</p>}

          {session && !loading && error && (
            <p className="cart-error">{error}</p>
          )}

          {session && !loading && cart && cart.items.length === 0 && (
            <p>{t('cart.empty')}</p>
          )}

          {session && !loading && cart && cart.items.length > 0 && (
            <>
              <table className="cart-table">
                <thead>
                  <tr>
                    <th className="cart-th--left">{t('cart.col.product')}</th>
                    <th className="cart-th--right">{t('cart.col.unitPrice')}</th>
                    <th className="cart-th--center">{t('cart.col.quantity')}</th>
                    <th className="cart-th--right">{t('cart.col.total')}</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {cart.items.map(item => (
                    <tr key={item.id} className="cart-row">
                      <td className="cart-td--product">
                        {item.photoUrl && (
                          <img
                            src={item.photoUrl}
                            alt={item.productName}
                            className="cart-product-img"
                          />
                        )}
                        {item.productName}
                        {!item.inStock && (
                          <span className="cart-out-of-stock">{t('cart.outOfStock')}</span>
                        )}
                      </td>
                      <td className="cart-td--right">{item.priceTTC.toFixed(2)} €</td>
                      <td className="cart-td--center">
                        <input
                          type="number"
                          min={1}
                          value={item.quantity}
                          disabled={updating === item.id}
                          onChange={e => {
                            const qty = parseInt(e.target.value, 10)
                            if (qty >= 1) handleQuantityChange(item.id, qty)
                          }}
                          className="cart-qty-input"
                        />
                      </td>
                      <td className="cart-td--right">{item.lineTotal.toFixed(2)} €</td>
                      <td className="cart-td--action">
                        <Button
                          variant="ghost"
                          size="sm"
                          disabled={updating === item.id}
                          onClick={() => handleRemove(item.id)}
                        >
                          {t('cart.remove')}
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="cart-total">
                {t('cart.grandTotal')} : {cart.total.toFixed(2)} €
              </div>

              <div className="cart-checkout-row">
                <Button
                  variant="primary"
                  onClick={() => { window.location.href = '/checkout' }}
                >
                  {t('cart.checkout')}
                </Button>
              </div>
            </>
          )}
        </div>
      </AppShell>
    </>
  )
}
