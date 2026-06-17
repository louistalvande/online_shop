import { useState, useEffect, useCallback } from 'react'
import './index.css'
import { getSession, logout, validateSession } from './api/authApi'
import { listPendingAlerts } from './api/productApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import { getShopTheme } from './api/shopConfigApi'
import LoginPage from './LoginPage'
import Header, { type Page } from './Header'
import DashboardPage from './DashboardPage'
import CatalogPage from './CatalogPage'
import ReportsPage from './ReportsPage'
import VisualIdentityPage from './VisualIdentityPage'
import LegalPagesPage from './LegalPagesPage'
import CampaignsPage from './CampaignsPage'
import SeoPage from './SeoPage'
import MaintenancePage from './MaintenancePage'

const VALID_PAGES: Page[] = ['dashboard', 'catalog', 'reports', 'campaigns', 'visual-identity', 'legal-pages', 'seo']

function pageFromUrl(): Page {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '')
  const segment = window.location.pathname.replace(base, '').replace(/^\//, '').split('/')[0]
  return VALID_PAGES.includes(segment as Page) ? (segment as Page) : 'dashboard'
}

export default function App() {
  const [session, setSession] = useState(getSession)
  const [authChecked, setAuthChecked] = useState(() => !getSession())
  const [page, setPage] = useState<Page>(pageFromUrl)
  const [alertCount, setAlertCount] = useState(0)
  const [maintenance, setMaintenance] = useState(false)
  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [shopName, setShopName] = useState('')

  const navigate = useCallback((newPage: Page) => {
    const base = import.meta.env.BASE_URL.replace(/\/$/, '')
    window.history.pushState({}, '', `${base}/${newPage}`)
    setPage(newPage)
  }, [])

  useEffect(() => {
    const onPop = () => setPage(pageFromUrl())
    window.addEventListener('popstate', onPop)
    return () => window.removeEventListener('popstate', onPop)
  }, [])

  useEffect(() => {
    if (!getSession()) { setAuthChecked(true); return }
    validateSession()
      .then(() => setAuthChecked(true))
      .catch(() => { setSession(null); setAuthChecked(true) })
  }, [])

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setMaintenance(s.active))
      .catch(() => {})
  }, [])

  useEffect(() => {
    getShopTheme().then(t => {
      setLogoUrl(t.logoUrl ?? null)
      if (t.shopName) setShopName(t.shopName)
      if (t.accentColor) {
        const hex = t.accentColor
        const n = parseInt(hex.slice(1), 16)
        const darken = (v: number) => Math.max(0, v - 20).toString(16).padStart(2, '0')
        const hover = '#' + darken(n >> 16) + darken((n >> 8) & 0xff) + darken(n & 0xff)
        document.documentElement.style.setProperty('--accent', hex)
        document.documentElement.style.setProperty('--accent-hover', hover)
      }
      if (t.bgColor) {
        document.documentElement.style.setProperty('--bg', t.bgColor)
      }
    }).catch(() => {})
  }, [])

  useEffect(() => {
    if (!session) return
    listPendingAlerts()
      .then(alerts => setAlertCount(alerts.length))
      .catch(() => {})
    const interval = setInterval(() => {
      listPendingAlerts()
        .then(alerts => setAlertCount(alerts.length))
        .catch(() => {})
    }, 60_000)
    return () => clearInterval(interval)
  }, [session])

  if (maintenance) {
    return <MaintenancePage />
  }

  if (!authChecked) {
    return null
  }

  if (!session) {
    return <LoginPage shopName={shopName} logoUrl={logoUrl} onLogin={() => setSession(getSession())} />
  }

  return (
    <Header
      onLogout={() => { logout(); setSession(null) }}
      onNavigate={navigate}
      currentPage={page}
      alertCount={alertCount}
      logoUrl={logoUrl}
      shopName={shopName}
    >
      {page === 'dashboard' && <DashboardPage />}
      {page === 'catalog' && <CatalogPage />}
      {page === 'reports' && <ReportsPage />}
      {page === 'campaigns' && <CampaignsPage />}
      {page === 'visual-identity' && <VisualIdentityPage onLogoChange={url => setLogoUrl(url)} />}
      {page === 'legal-pages' && <LegalPagesPage />}
      {page === 'seo' && <SeoPage />}
    </Header>
  )
}
