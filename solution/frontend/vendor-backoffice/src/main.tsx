import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App.tsx'
import ProfilePage from './ProfilePage'
import OrderListPage from './OrderListPage'
import OrderDetailPage from './OrderDetailPage'

const base = import.meta.env.BASE_URL.replace(/\/$/, '') // '/vendor'
const rawPath = window.location.pathname
const path = rawPath.startsWith(base) ? rawPath.slice(base.length) || '/' : rawPath

function Root() {
  if (path === '/profile') return <ProfilePage />
  if (path === '/orders') return <OrderListPage />
  const orderDetailMatch = path.match(/^\/orders\/([^/]+)$/)
  if (orderDetailMatch) return <OrderDetailPage orderId={orderDetailMatch[1]} />
  return <App />
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
)
