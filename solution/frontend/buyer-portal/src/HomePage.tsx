import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'

const WORKS = [
  { id: 1, title: 'Forêt en automne', category: 'Paysage', price: '4,90 €' },
  { id: 2, title: 'Rue pavée sous la pluie', category: 'Lieux urbains', price: '4,90 €' },
  { id: 3, title: 'Portrait au fusain', category: 'Portrait', price: '4,90 €' },
  { id: 4, title: 'Bord de mer au crépuscule', category: 'Paysage', price: '4,90 €' },
  { id: 5, title: 'Vieille bicyclette', category: 'Objets', price: '4,90 €' },
  { id: 6, title: 'Chat endormi', category: 'Animaux', price: '4,90 €' },
  { id: 7, title: 'Marché provençal', category: 'Lieux urbains', price: '4,90 €' },
  { id: 8, title: 'Bouquet sauvage', category: 'Nature', price: '4,90 €' },
]

/** Hero banner + featured catalog for the buyer portal home page. */
export default function HomePage() {
  const { t } = useTranslation()

  return (
    <>
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
          {WORKS.map(w => (
            <Card key={w.id}>
              <div className="product-image-placeholder" />
              <div className="product-card-body">
                <span className="product-card-category">{w.category}</span>
                <h3 className="product-card-title">{w.title}</h3>
                <div className="product-card-footer">
                  <span className="product-card-price">{w.price}</span>
                  <Button variant="secondary" size="sm">{t('product.addToCart')}</Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </>
  )
}
