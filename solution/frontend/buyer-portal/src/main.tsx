import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App'
import { getShopTheme } from './api/themeApi'

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

const path = window.location.pathname
const params = new URLSearchParams(window.location.search)
const token = params.get('token') ?? ''

function Root() {
  if (path === '/register') return <RegisterPage />
  if (path === '/activate') return <ActivatePage token={token} />
  if (path === '/forgot-password') return <ForgotPasswordPage />
  if (path === '/reset-password') return <ResetPasswordPage token={token} />
  if (path === '/profile') return <ProfilePage />
  if (path === '/catalog') return <CatalogPage />
  const catalogDetailMatch = path.match(/^\/catalog\/([^/]+)$/)
  if (catalogDetailMatch) return <ProductDetailPage productId={catalogDetailMatch[1]} />
  if (path === '/cart') return <CartPage />
  if (path === '/checkout') return <CheckoutPage />
  if (path === '/my-orders') return <OrderListPage />
  const orderDetailMatch = path.match(/^\/my-orders\/([^/]+)$/)
  if (orderDetailMatch) return <OrderDetailPage orderId={orderDetailMatch[1]} />
  return <App openLogin={path === '/login'} />
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
)
