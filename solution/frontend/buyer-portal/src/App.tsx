import './index.css'
import { useEffect, useState } from 'react'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import { getShopTheme } from './api/themeApi'
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
  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [bannerUrl, setBannerUrl] = useState<string | null>(null)

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setMaintenance(s.active))
      .catch(() => {})
  }, [])

  useEffect(() => {
    getShopTheme()
      .then(t => {
        // theme already applied globally by main.tsx at startup
        setLogoUrl(t.logoUrl ?? null)
        setBannerUrl(t.bannerUrl ?? null)
      })
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
          onLogin={(s) => { setSession(s); setShowLogin(false) }}
        />
      )}
      <Header
        session={session}
        logoUrl={logoUrl}
        onShowLogin={() => setShowLogin(true)}
        onLogout={() => { logout(); setSession(null); window.dispatchEvent(new Event('session-changed')) }}
      >
        <HomePage bannerUrl={bannerUrl} />
      </Header>
    </>
  )
}
