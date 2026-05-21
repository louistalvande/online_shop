import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App'
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
