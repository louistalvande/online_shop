import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, LangToggle, CartIcon, UserMenu, Snackbar } from '@workspace/theme'
import { fetchProduct, type BuyerProduct } from './api/catalogApi'
import { getProductSeo, getShopSeo, type ProductSeoOverride, type ShopSeoConfig } from './api/seoApi'
import { addToCart } from './api/cartApi'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { subscribeToRestock, unsubscribeFromRestock, listSubscriptions } from './api/stockSubscriptionApi'
import LoginModal from './LoginModal'
import { useCartCount } from './hooks/useCartCount'
import { useShopName } from './hooks/useShopName'
import { useSeoMeta } from './hooks/useSeoMeta'
import { useLogoUrl } from './hooks/useLogoUrl'
import { useFooterLinks } from './hooks/useFooterLinks'
import { useFooterNotice } from './hooks/useFooterNotice'

function formatPrice(price: number): string {
  return price.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })
}

interface Props {
  slug: string
}

export default function ProductDetailPage({ slug }: Props) {
  const { t, i18n } = useTranslation()
  const brandName = useShopName()
  const logoUrl = useLogoUrl()
  const footerLinks = useFooterLinks()
  const footerNotice = useFooterNotice()
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [product, setProduct] = useState<BuyerProduct | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [adding, setAdding] = useState(false)
  const [cartFeedback, setCartFeedback] = useState<boolean | null>(null)
  const [showLogin, setShowLogin] = useState(false)
  const [pendingAdd, setPendingAdd] = useState(false)
  const [snackbar, setSnackbar] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)
  const [currentPhoto, setCurrentPhoto] = useState(0)
  const [quantity, setQuantity] = useState(1)
  const [subscribed, setSubscribed] = useState(false)
  const [subscribing, setSubscribing] = useState(false)
  const [productSeo, setProductSeo] = useState<ProductSeoOverride | null>(null)
  const [shopSeo, setShopSeo] = useState<ShopSeoConfig | null>(null)
  const cartCount = useCartCount()

  useEffect(() => {
    fetchProduct(slug)
      .then(p => {
        setProduct(p)
        getProductSeo(p.id).then(setProductSeo).catch(() => {})
        if (p.outOfStock && session) {
          listSubscriptions()
            .then(subs => setSubscribed(subs.some(s => s.productId === p.id)))
            .catch(() => {})
        }
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false))
    getShopSeo().then(setShopSeo).catch(() => {})
  }, [slug])

  useEffect(() => {
    if (session && pendingAdd) {
      setPendingAdd(false)
      doAddToCart()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session])

  async function doAddToCart() {
    setAdding(true)
    setCartFeedback(null)
    try {
      await addToCart(product!.id, quantity)
      window.dispatchEvent(new Event('cart-updated'))
      setCartFeedback(true)
      setSnackbar({ message: t('cart.added'), variant: 'success' })
    } catch {
      setCartFeedback(false)
      setSnackbar({ message: t('cart.error.add'), variant: 'error' })
    } finally {
      setAdding(false)
      setTimeout(() => setCartFeedback(null), 2500)
    }
  }

  async function handleAddToCart() {
    if (!session) { setPendingAdd(true); setShowLogin(true); return }
    await doAddToCart()
  }

  const seoTitle = productSeo?.seoTitle ?? product?.name ?? undefined
  const seoDescription = productSeo?.seoDescription ?? product?.description ?? undefined
  const ogImage = productSeo?.ogImageUrl ?? undefined

  useSeoMeta({
    title: seoTitle ?? brandName ?? undefined,
    description: seoDescription,
    keywords: productSeo?.seoKeywords,
    ogImage,
    canonical: window.location.origin + '/catalog/' + slug,
    noindex: shopSeo ? !shopSeo.indexProducts : false,
  })

  return (
    <>
      {snackbar && <Snackbar message={snackbar.message} variant={snackbar.variant} onDismiss={() => setSnackbar(null)} />}
      {showLogin && (
        <LoginModal
          onClose={() => { setShowLogin(false); setPendingAdd(false) }}
          onLogin={(s) => {
            setSession(s)
            setShowLogin(false)
            window.dispatchEvent(new Event('session-changed'))
          }}
        />
      )}
      <AppShell
        appName={t('app.name')}
        brandName={brandName}
        logoUrl={logoUrl}
        footerLinks={footerLinks}
        footerNotice={footerNotice}
        navLinks={[
          { label: t('nav.home'), href: '/' },
          { label: t('nav.catalog'), href: '/catalog' },
        ]}
        actions={
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
                onLogout={() => { logout(); setSession(null); window.dispatchEvent(new Event('session-changed')) }}
              />
            ) : (
              <Button variant="ghost" size="sm" onClick={() => { window.location.href = '/login' }}>
                {t('nav.login')}
              </Button>
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
        <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 24px 64px' }}>
          <a href="/catalog" style={{ fontSize: 14, color: 'var(--accent)', textDecoration: 'none' }}>
            ← {t('nav.catalog')}
          </a>

          {loading && <p style={{ marginTop: 24, color: 'var(--text-muted)' }}>{t('catalog.loading')}</p>}
          {error && <p style={{ marginTop: 24, color: 'var(--accent)' }}>{t('catalog.error.load')}</p>}

          {product && (
            <div style={{ marginTop: 32, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 48, alignItems: 'start' }}>
              <div>
                {product.photoUrls.length > 0 ? (
                  <>
                    <img
                      src={product.photoUrls[currentPhoto]}
                      alt={product.name}
                      style={{ width: '100%', aspectRatio: '1', objectFit: 'cover', borderRadius: 8, background: 'var(--surface)' }}
                    />
                    {product.photoUrls.length > 1 && (
                      <div style={{ display: 'flex', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
                        {product.photoUrls.map((url, i) => (
                          <img
                            key={i}
                            src={url}
                            alt=""
                            onClick={() => setCurrentPhoto(i)}
                            style={{
                              width: 64, height: 64, objectFit: 'cover', borderRadius: 4, cursor: 'pointer',
                              border: i === currentPhoto ? '2px solid var(--accent)' : '2px solid transparent',
                            }}
                          />
                        ))}
                      </div>
                    )}
                  </>
                ) : (
                  <div style={{ width: '100%', aspectRatio: '1', background: 'var(--surface)', borderRadius: 8 }} />
                )}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                {product.category && (
                  <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--accent)', textTransform: 'uppercase', letterSpacing: 1 }}>
                    {product.category}
                  </span>
                )}
                <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, margin: 0 }}>
                  {product.name}
                </h1>
                <div>
                  <div style={{ fontSize: 26, fontWeight: 700 }}>{formatPrice(product.priceTTC)}</div>
                  <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
                    {t('catalog.priceExclTax', { price: formatPrice(product.priceExclTax) })}
                  </div>
                </div>

                {product.outOfStock ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <Button variant="secondary" disabled>
                      {t('catalog.outOfStock')}
                    </Button>
                    {!session ? (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => { setPendingAdd(true); setShowLogin(true) }}
                      >
                        {t('stock.notify.subscribe')}
                      </Button>
                    ) : subscribed ? (
                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={subscribing}
                        onClick={async () => {
                          setSubscribing(true)
                          try {
                            await unsubscribeFromRestock(product!.id)
                            setSubscribed(false)
                            setSnackbar({ message: t('stock.notify.removed'), variant: 'success' })
                          } catch {
                            setSnackbar({ message: t('stock.notify.error'), variant: 'error' })
                          } finally {
                            setSubscribing(false)
                          }
                        }}
                      >
                        {subscribing ? '…' : t('stock.notify.subscribed')}
                      </Button>
                    ) : (
                      <Button
                        variant="ghost"
                        size="sm"
                        disabled={subscribing}
                        onClick={async () => {
                          setSubscribing(true)
                          try {
                            await subscribeToRestock(product!.id)
                            setSubscribed(true)
                            setSnackbar({ message: t('stock.notify.success'), variant: 'success' })
                          } catch (err: unknown) {
                            const code = (err as { code?: string }).code
                            const msg = code === 'ALREADY_SUBSCRIBED'
                              ? t('stock.notify.alreadySubscribed')
                              : code === 'PRODUCT_IN_STOCK'
                                ? t('stock.notify.productInStock')
                                : t('stock.notify.error')
                            setSnackbar({ message: msg, variant: 'error' })
                          } finally {
                            setSubscribing(false)
                          }
                        }}
                      >
                        {subscribing ? '…' : t('stock.notify.subscribe')}
                      </Button>
                    )}
                  </div>
                ) : (
                  <div className="add-to-cart-row">
                    <div className="qty-stepper">
                      <button className="qty-btn" onClick={() => setQuantity(q => Math.max(1, q - 1))} aria-label="-">−</button>
                      <span className="qty-value">{quantity}</span>
                      <button className="qty-btn" onClick={() => setQuantity(q => q + 1)} aria-label="+">+</button>
                    </div>
                    <Button variant="primary" disabled={adding} onClick={handleAddToCart}>
                      <CartIcon size={16} />
                      {cartFeedback === true
                        ? t('cart.added')
                        : cartFeedback === false
                          ? t('cart.error.add')
                          : adding ? '…' : t('product.addToCart')}
                    </Button>
                  </div>
                )}

                {product.description && (
                  <div style={{ borderTop: '1px solid var(--border)', paddingTop: 20 }}>
                    <h2 style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 10 }}>
                      {t('product.description')}
                    </h2>
                    <p style={{ fontSize: 15, lineHeight: 1.7, margin: 0, color: 'var(--text)' }}>
                      {product.description}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </AppShell>
    </>
  )
}
