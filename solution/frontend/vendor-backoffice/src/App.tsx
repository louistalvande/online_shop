import { useState, useEffect } from 'react'
import './index.css'
import { getSession, logout } from './api/authApi'
import { listPendingAlerts } from './api/productApi'
import LoginPage from './LoginPage'
import Header, { type Page } from './Header'
import DashboardPage from './DashboardPage'
import CatalogPage from './CatalogPage'

export default function App() {
  const [session, setSession] = useState(getSession)
  const [page, setPage] = useState<Page>('dashboard')
  const [alertCount, setAlertCount] = useState(0)

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
    </Header>
  )
}
