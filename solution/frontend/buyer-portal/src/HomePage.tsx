import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { fetchProducts, type BuyerProduct } from './api/catalogApi'
import { addToCart } from './api/cartApi'
import { getSession, type BuyerSession } from './api/authApi'
import LoginModal from './LoginModal'

/** Hero banner + featured catalog for the buyer portal home page. */
export default function HomePage() {
  const { t } = useTranslation()
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [products, setProducts] = useState<BuyerProduct[]>([])
  const [showLogin, setShowLogin] = useState(false)
  const [addingId, setAddingId] = useState<string | null>(null)
  const [cartFeedback, setCartFeedback] = useState<{ id: string; ok: boolean } | null>(null)

  useEffect(() => {
    fetchProducts({ page: 0, size: 8 })
      .then(data => setProducts(data.content))
      .catch(() => {})
  }, [])

  async function handleAddToCart(productId: string) {
    if (!session) { setShowLogin(true); return }
    setAddingId(productId)
    setCartFeedback(null)
    try {
      await addToCart(productId, 1)
      window.dispatchEvent(new Event('cart-updated'))
      setCartFeedback({ id: productId, ok: true })
    } catch {
      setCartFeedback({ id: productId, ok: false })
    } finally {
      setAddingId(null)
      setTimeout(() => setCartFeedback(null), 2500)
    }
  }

  return (
    <>
      {showLogin && (
        <LoginModal
          onClose={() => setShowLogin(false)}
          onLogin={() => { setSession(getSession()); setShowLogin(false) }}
        />
      )}

      <section className="home-hero">
        <h1 className="home-hero-title">{t('home.title')}</h1>
        <p className="home-hero-subtitle">{t('home.subtitle')}</p>
      </section>

      <section id="catalogue" className="home-catalog-section">
        <div className="home-catalog-header">
          <h2 className="home-catalog-title">{t('home.featured')}</h2>
          <Button onClick={() => { window.location.href = '/catalog' }}>{t('home.viewAll')}</Button>
        </div>
        <div className="home-catalog-grid">
          {products.map(p => (
            <Card key={p.id}>
              {p.photoUrls.length > 0 ? (
                <img src={p.photoUrls[0]} alt={p.name} className="catalog-product-image" />
              ) : (
                <div className="product-image-placeholder" />
              )}
              <div className="product-card-body">
                {p.category && <span className="product-card-category">{p.category}</span>}
                <h3 className="product-card-title">{p.name}</h3>
                <div className="product-card-footer">
                  <span className="product-card-price">
                    {p.priceTTC.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' })}
                  </span>
                  {p.outOfStock ? (
                    <Button variant="secondary" size="sm" disabled>
                      {t('catalog.outOfStock')}
                    </Button>
                  ) : (
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={addingId === p.id}
                      onClick={() => handleAddToCart(p.id)}
                    >
                      {cartFeedback?.id === p.id
                        ? t(cartFeedback.ok ? 'cart.added' : 'cart.error.add')
                        : addingId === p.id
                          ? '…'
                          : t('product.addToCart')}
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </>
  )
}
