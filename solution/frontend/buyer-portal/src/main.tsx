import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './i18n'
import App from './App'
import RegisterPage from './RegisterPage'
import ActivatePage from './ActivatePage'
import ProfilePage from './ProfilePage'
const path = window.location.pathname
const token = new URLSearchParams(window.location.search).get('token') ?? ''

function Root() {
  if (path === '/register') return <RegisterPage />
  if (path === '/activate') return <ActivatePage token={token} />
  if (path === '/profile') return <ProfilePage />
  return <App openLogin={path === '/login'} />
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
)
