import './index.css'
import { useEffect, useState } from 'react'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import LoginModal from './LoginModal'
import Header from './Header'
import HomePage from './HomePage'
import MaintenancePage from './MaintenancePage'

interface Props {
  openLogin?: boolean
}

export default function App({ openLogin = false }: Props) {
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [showLogin, setShowLogin] = useState(openLogin)
  const [maintenance, setMaintenance] = useState(false)

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setMaintenance(s.active))
      .catch(() => {})
  }, [])

  if (maintenance) {
    return <MaintenancePage />
  }

  return (
    <>
      {showLogin && (
        <LoginModal
          onClose={() => setShowLogin(false)}
          onLogin={() => { setSession(getSession()); setShowLogin(false) }}
        />
      )}
      <Header
        session={session}
        onShowLogin={() => setShowLogin(true)}
        onLogout={() => { logout(); setSession(null) }}
      >
        <HomePage />
      </Header>
    </>
  )
}
