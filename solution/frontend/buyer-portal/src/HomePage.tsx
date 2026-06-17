import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card, Snackbar, CartIcon } from '@workspace/theme'
import { fetchProducts, type BuyerProduct } from './api/catalogApi'
import { addToCart } from './api/cartApi'
import { getSession, type BuyerSession } from './api/authApi'
import { subscribeToRestock } from './api/stockSubscriptionApi'
import { getShopSeo, type ShopSeoConfig } from './api/seoApi'
import { useShopName } from './hooks/useShopName'
import { useSeoMeta } from './hooks/useSeoMeta'
import LoginModal from './LoginModal'
import AnnouncementCarousel from './AnnouncementCarousel'

interface Props {
  bannerUrl?: string | null
}

/** Hero banner + featured catalog for the buyer portal home page. */
export default function HomePage({ bannerUrl }: Props) {
  const { t } = useTranslation()
  const shopName = useShopName()
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [products, setProducts] = useState<BuyerProduct[]>([])
  const [showLogin, setShowLogin] = useState(false)
  const [pendingCartProductId, setPendingCartProductId] = useState<string | null>(null)
  const [addingId, setAddingId] = useState<string | null>(null)
  const [cartFeedback, setCartFeedback] = useState<{ id: string; ok: boolean } | null>(null)
  const [snackbar, setSnackbar] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)
  const [pendingAlertProductId, setPendingAlertProductId] = useState<string | null>(null)
  const [subscribingId, setSubscribingId] = useState<string | null>(null)
  const [subscribedIds, setSubscribedIds] = useState<Set<string>>(new Set())
  const [shopSeo, setShopSeo] = useState<ShopSeoConfig | null>(null)

  useEffect(() => {
    getShopSeo().then(setShopSeo).catch(() => {})
  }, [])

  useSeoMeta({
    title: (shopSeo?.seoTitle ?? shopName) || undefined,
    description: shopSeo?.seoDescription,
    keywords: shopSeo?.seoKeywords,
    ogImage: shopSeo?.ogImageUrl,
    canonical: shopSeo?.canonicalUrl,
  })

  useEffect(() => {
    fetchProducts({ page: 0, size: 8 })
      .then(data => setProducts(data.content))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (session && pendingCartProductId) {
      const id = pendingCartProductId
      setPendingCartProductId(null)
      doAddToCart(id)
    }
    if (session && pendingAlertProductId) {
      const id = pendingAlertProductId
      setPendingAlertProductId(null)
      doSubscribe(id)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session])

  async function doAddToCart(productId: string) {
    setAddingId(productId)
    setCartFeedback(null)
    try {
      await addToCart(productId, 1)
      window.dispatchEvent(new Event('cart-updated'))
      setCartFeedback({ id: productId, ok: true })
      setSnackbar({ message: t('cart.added'), variant: 'success' })
    } catch {
      setCartFeedback({ id: productId, ok: false })
      setSnackbar({ message: t('cart.error.add'), variant: 'error' })
    } finally {
      setAddingId(null)
      setTimeout(() => setCartFeedback(null), 2500)
    }
  }

  async function handleAddToCart(productId: string) {
    if (!session) { setPendingCartProductId(productId); setShowLogin(true); return }
    await doAddToCart(productId)
  }

  async function doSubscribe(productId: string) {
    setSubscribingId(productId)
    try {
      await subscribeToRestock(productId)
      setSubscribedIds(prev => new Set(prev).add(productId))
      setSnackbar({ message: t('stock.notify.success'), variant: 'success' })
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      if (code === 'ALREADY_SUBSCRIBED') {
        setSubscribedIds(prev => new Set(prev).add(productId))
      } else {
        setSnackbar({ message: t('stock.notify.error'), variant: 'error' })
      }
    } finally {
      setSubscribingId(null)
    }
  }

  async function handleNotifyAlert(productId: string) {
    if (!session) { setPendingAlertProductId(productId); setShowLogin(true); return }
    await doSubscribe(productId)
  }

  return (
    <>

      {snackbar && <Snackbar message={snackbar.message} variant={snackbar.variant} onDismiss={() => setSnackbar(null)} />}
      {showLogin && (
        <LoginModal
          onClose={() => { setShowLogin(false); setPendingCartProductId(null); setPendingAlertProductId(null) }}
          onLogin={(s) => {
            setSession(s)
            setShowLogin(false)
            window.dispatchEvent(new Event('session-changed'))
          }}
        />
      )}

      {bannerUrl && (
        <div className="home-banner">
          <img src={bannerUrl} alt="" className="home-banner-img" />
        </div>
      )}

      <AnnouncementCarousel />

      <section id="catalogue" className="home-catalog-section">
        <div className="home-catalog-grid">
          {products.map(p => (
            <Card key={p.id}>
              <a href={`/catalog/${p.slug}`} style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}>
                {p.photoUrls.length > 0 ? (
                  <img src={p.photoUrls[0]} alt={p.name} className="catalog-product-image" />
                ) : (
                  <div className="product-image-placeholder" />
                )}
                <div className="product-card-body" style={{ paddingBottom: 0 }}>
                  {p.category && <span className="product-card-category">{p.category}</span>}
                  <h3 className="product-card-title">{p.name}</h3>
                </div>
              </a>
              <div className="product-card-body" style={{ paddingTop: 0 }}>
                <div className="product-card-footer">
                  <span className="product-card-price">
                    {p.priceTTC.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })}
                  </span>
                  {p.outOfStock ? (
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={subscribingId === p.id || subscribedIds.has(p.id)}
                      onClick={() => handleNotifyAlert(p.id)}
                    >
                      {subscribedIds.has(p.id) ? t('stock.notify.subscribed') : t('stock.notify.subscribe')}
                    </Button>
                  ) : (
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={addingId === p.id}
                      onClick={() => handleAddToCart(p.id)}
                      aria-label={t('product.addToCart')}
                    >
                      {cartFeedback?.id === p.id && cartFeedback.ok
                        ? '✓'
                        : addingId === p.id
                          ? '…'
                          : <CartIcon size={16} />}
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
        <div className="home-catalog-footer">
          <Button onClick={() => { window.location.href = '/catalog' }}>{t('home.viewAll')}</Button>
        </div>
      </section>
    </>
  )
}
