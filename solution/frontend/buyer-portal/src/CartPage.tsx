import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, LangToggle, Button, UserMenu } from '@workspace/theme'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { getCart, updateCartItem, removeCartItem, type CartData } from './api/cartApi'
import LoginModal from './LoginModal'

export default function CartPage() {
  const { t, i18n } = useTranslation()
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
          onLogout={() => { logout(); setSession(null); setCart(null) }}
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
          onLogin={() => { setSession(getSession()); setShowLogin(false) }}
        />
      )}
      <AppShell
        appName={t('app.name')}
        navLinks={[
          { label: t('nav.home'), href: '/' },
          { label: t('nav.catalog'), href: '/catalog' },
        ]}
        actions={headerActions}
      >
        <div style={{ maxWidth: 800, margin: '0 auto', padding: '2rem 1rem' }}>
          <h1>{t('cart.title')}</h1>

          {!session && (
            <p>
              {t('cart.loginRequired')}{' '}
              <button onClick={() => setShowLogin(true)}>{t('nav.login')}</button>
            </p>
          )}

          {session && loading && <p>{t('cart.loading')}</p>}

          {session && !loading && error && (
            <p style={{ color: 'red' }}>{error}</p>
          )}

          {session && !loading && cart && cart.items.length === 0 && (
            <p>{t('cart.empty')}</p>
          )}

          {session && !loading && cart && cart.items.length > 0 && (
            <>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: 'left', padding: '0.5rem' }}>{t('cart.col.product')}</th>
                    <th style={{ textAlign: 'right', padding: '0.5rem' }}>{t('cart.col.unitPrice')}</th>
                    <th style={{ textAlign: 'center', padding: '0.5rem' }}>{t('cart.col.quantity')}</th>
                    <th style={{ textAlign: 'right', padding: '0.5rem' }}>{t('cart.col.total')}</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {cart.items.map(item => (
                    <tr key={item.id} style={{ borderTop: '1px solid #eee' }}>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        {item.photoUrl && (
                          <img
                            src={item.photoUrl}
                            alt={item.productName}
                            style={{ width: 48, height: 48, objectFit: 'cover', marginRight: 8, verticalAlign: 'middle' }}
                          />
                        )}
                        {item.productName}
                        {!item.inStock && (
                          <span style={{ color: 'red', marginLeft: 8, fontSize: '0.85em' }}>
                            {t('cart.outOfStock')}
                          </span>
                        )}
                      </td>
                      <td style={{ textAlign: 'right', padding: '0.75rem 0.5rem' }}>
                        {item.priceTTC.toFixed(2)} €
                      </td>
                      <td style={{ textAlign: 'center', padding: '0.75rem 0.5rem' }}>
                        <input
                          type="number"
                          min={1}
                          value={item.quantity}
                          disabled={updating === item.id}
                          onChange={e => {
                            const qty = parseInt(e.target.value, 10)
                            if (qty >= 1) handleQuantityChange(item.id, qty)
                          }}
                          style={{ width: 60, textAlign: 'center' }}
                        />
                      </td>
                      <td style={{ textAlign: 'right', padding: '0.75rem 0.5rem' }}>
                        {item.lineTotal.toFixed(2)} €
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
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

              <div style={{ textAlign: 'right', marginTop: '1.5rem', fontSize: '1.2rem', fontWeight: 600 }}>
                {t('cart.grandTotal')} : {cart.total.toFixed(2)} €
              </div>

              <div style={{ textAlign: 'right', marginTop: '1rem' }}>
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
