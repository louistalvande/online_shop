import { StrictMode, useState, useEffect, type ReactNode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App'
import { getShopTheme } from './api/themeApi'
import { getShopSeo } from './api/seoApi'
import { getSession, validateSession } from './api/authApi'

function darkenHex(hex: string, amount: number): string {
  const n = parseInt(hex.slice(1), 16)
  const r = Math.max(0, (n >> 16) - amount)
  const g = Math.max(0, ((n >> 8) & 0xff) - amount)
  const b = Math.max(0, (n & 0xff) - amount)
  return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('')
}

getShopTheme().then(t => {
  if (t.accentColor) {
    document.documentElement.style.setProperty('--accent', t.accentColor)
    document.documentElement.style.setProperty('--accent-hover', darkenHex(t.accentColor, 20))
  }
  if (t.bgColor) document.documentElement.style.setProperty('--bg', t.bgColor)
  if (t.shopName) document.title = t.shopName
}).catch(() => {})

getShopSeo().then(seo => {
  if (seo.googleVerification) {
    const el = document.createElement('meta')
    el.setAttribute('name', 'google-site-verification')
    el.content = seo.googleVerification
    document.head.appendChild(el)
  }
  if (seo.bingVerification) {
    const el = document.createElement('meta')
    el.setAttribute('name', 'msvalidate.01')
    el.content = seo.bingVerification
    document.head.appendChild(el)
  }
  if (seo.ga4Id) {
    const s1 = document.createElement('script')
    s1.async = true
    s1.src = `https://www.googletagmanager.com/gtag/js?id=${seo.ga4Id}`
    document.head.appendChild(s1)
    const s2 = document.createElement('script')
    s2.text = `window.dataLayer=window.dataLayer||[];function gtag(){dataLayer.push(arguments);}gtag('js',new Date());gtag('config','${seo.ga4Id}');`
    document.head.appendChild(s2)
  }
}).catch(() => {})
import RegisterPage from './RegisterPage'
import ActivatePage from './ActivatePage'
import ProfilePage from './ProfilePage'
import CatalogPage from './CatalogPage'
import CartPage from './CartPage'
import CheckoutPage from './CheckoutPage'
import OrderListPage from './OrderListPage'
import OrderDetailPage from './OrderDetailPage'
import ForgotPasswordPage from './ForgotPasswordPage'
import ResetPasswordPage from './ResetPasswordPage'
import ProductDetailPage from './ProductDetailPage'
import LegalPage from './LegalPage'

const path = window.location.pathname
const params = new URLSearchParams(window.location.search)
const token = params.get('token') ?? ''

/** Validates the JWT with the backend before rendering a protected page. Redirects to /login if expired. */
function AuthGuard({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<'checking' | 'ok' | 'denied'>(() =>
    getSession() ? 'checking' : 'denied'
  )

  useEffect(() => {
    if (status === 'denied') {
      window.location.replace('/login')
      return
    }
    validateSession()
      .then(() => setStatus('ok'))
      .catch(() => {
        setStatus('denied')
        window.location.replace('/login')
      })
  }, [])

  if (status !== 'ok') return null
  return <>{children}</>
}

function Root() {
  if (path === '/register') return <RegisterPage />
  if (path === '/activate') return <ActivatePage token={token} />
  if (path === '/forgot-password') return <ForgotPasswordPage />
  if (path === '/reset-password') return <ResetPasswordPage token={token} />
  if (path === '/profile') return <AuthGuard><ProfilePage /></AuthGuard>
  if (path === '/catalog') return <CatalogPage />
  const catalogDetailMatch = path.match(/^\/catalog\/([^/]+)$/)
  if (catalogDetailMatch) return <ProductDetailPage productId={catalogDetailMatch[1]} />
  if (path === '/cart') return <CartPage />
  if (path === '/checkout') return <AuthGuard><CheckoutPage /></AuthGuard>
  if (path === '/my-orders') return <AuthGuard><OrderListPage /></AuthGuard>
  const orderDetailMatch = path.match(/^\/my-orders\/([^/]+)$/)
  if (orderDetailMatch) return <AuthGuard><OrderDetailPage orderId={orderDetailMatch[1]} /></AuthGuard>
  if (path === '/legal/cgv')              return <LegalPage pageKey="legal_cgv" />
  if (path === '/legal/mentions-legales') return <LegalPage pageKey="legal_mentions_legales" />
  if (path === '/legal/confidentialite')  return <LegalPage pageKey="legal_confidentialite" />
  if (path === '/legal/retour')           return <LegalPage pageKey="legal_retour" />
  if (path === '/legal/apropos')          return <LegalPage pageKey="legal_apropos" />
  if (path === '/legal/reproduction')     return <LegalPage pageKey="legal_reproduction" />
  return <App openLogin={path === '/login'} />
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
)
