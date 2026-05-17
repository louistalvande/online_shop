import './index.css'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, CartIcon, Button, Card, LangToggle, UserMenu } from '@workspace/theme'
import LoginModal from './LoginModal'
import { getSession, logout, type BuyerSession } from './api/authApi'

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
        <section style={{ background: 'var(--bg)', textAlign: 'center', padding: '80px 24px 72px' }}>
          <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 'clamp(28px,4vw,48px)', fontWeight: 700, letterSpacing: '-0.02em', maxWidth: 700, margin: '0 auto 20px' }}>
            {t('home.title')}
          </h1>
          <p style={{ fontSize: 16, color: 'var(--text-muted)', maxWidth: 560, margin: '0 auto', lineHeight: 1.7 }}>
            {t('home.subtitle')}
          </p>
        </section>

        <section id="catalogue" style={{ maxWidth: 1100, margin: '0 auto', padding: '48px 24px 64px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 32 }}>
            <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 26, fontWeight: 700 }}>{t('home.featured')}</h2>
            <Button>{t('home.viewAll')}</Button>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(230px, 1fr))', gap: 24 }}>
            {WORKS.map(w => (
              <Card key={w.id}>
                <div style={{ background: '#f0ece4', height: 200 }} />
                <div style={{ padding: '16px 18px 18px' }}>
                  <span style={{ fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--accent)' }}>
                    {w.category}
                  </span>
                  <h3 style={{ fontSize: 16, fontWeight: 600, marginTop: 6, marginBottom: 14 }}>{w.title}</h3>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ fontWeight: 700 }}>{w.price}</span>
                    <Button variant="secondary" size="sm">{t('product.addToCart')}</Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </section>
      </AppShell>
    </>
  )
}
