import { useState, useEffect } from 'react'
import './index.css'
import { getSession, logout } from './api/authApi'
import { listPendingAlerts } from './api/productApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import LoginPage from './LoginPage'
import Header, { type Page } from './Header'
import DashboardPage from './DashboardPage'
import CatalogPage from './CatalogPage'
import ReportsPage from './ReportsPage'
import MaintenancePage from './MaintenancePage'

export default function App() {
  const [session, setSession] = useState(getSession)
  const [page, setPage] = useState<Page>('dashboard')
  const [alertCount, setAlertCount] = useState(0)
  const [maintenance, setMaintenance] = useState(false)

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setMaintenance(s.active))
      .catch(() => {})
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
    >
      {page === 'dashboard' && <DashboardPage />}
      {page === 'catalog' && <CatalogPage />}
      {page === 'reports' && <ReportsPage />}
    </Header>
  )
}
