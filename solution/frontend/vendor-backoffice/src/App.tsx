import { useState, useEffect } from 'react'
import './index.css'
import { getSession, logout } from './api/authApi'
import { listPendingAlerts } from './api/productApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import { getShopTheme } from './api/shopConfigApi'
import LoginPage from './LoginPage'
import Header, { type Page } from './Header'
import DashboardPage from './DashboardPage'
import CatalogPage from './CatalogPage'
import ReportsPage from './ReportsPage'
import VisualIdentityPage from './VisualIdentityPage'
import AnnouncementsPage from './AnnouncementsPage'
import CampaignsPage from './CampaignsPage'
import MaintenancePage from './MaintenancePage'

export default function App() {
  const [session, setSession] = useState(getSession)
  const [page, setPage] = useState<Page>('dashboard')
  const [alertCount, setAlertCount] = useState(0)
  const [maintenance, setMaintenance] = useState(false)
  const [logoUrl, setLogoUrl] = useState<string | null>(null)

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setMaintenance(s.active))
      .catch(() => {})
  }, [])

  useEffect(() => {
    getShopTheme().then(t => setLogoUrl(t.logoUrl ?? null)).catch(() => {})
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

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  return (
    <Header
      onLogout={() => { logout(); setSession(null) }}
      onNavigate={setPage}
      currentPage={page}
      alertCount={alertCount}
      logoUrl={logoUrl}
    >
      {page === 'dashboard' && <DashboardPage />}
      {page === 'catalog' && <CatalogPage />}
      {page === 'reports' && <ReportsPage />}
      {page === 'announcements' && <AnnouncementsPage />}
      {page === 'campaigns' && <CampaignsPage />}
      {page === 'visual-identity' && <VisualIdentityPage onLogoChange={url => setLogoUrl(url)} />}
    </Header>
  )
}
