import './index.css'
import { useEffect, useState } from 'react'
import { getSession, logout, type BuyerSession } from './api/authApi'
import { getMaintenanceStatus } from './api/maintenanceApi'
import { getShopTheme } from './api/themeApi'
import LoginModal from './LoginModal'
import Header from './Header'
import HomePage from './HomePage'
import MaintenancePage from './MaintenancePage'

function darkenHex(hex: string, amount: number): string {
  const n = parseInt(hex.slice(1), 16)
  const r = Math.max(0, (n >> 16) - amount)
  const g = Math.max(0, ((n >> 8) & 0xff) - amount)
  const b = Math.max(0, (n & 0xff) - amount)
  return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('')
}

function applyTheme(accentColor: string) {
  document.documentElement.style.setProperty('--accent', accentColor)
  document.documentElement.style.setProperty('--accent-hover', darkenHex(accentColor, 20))
}

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
        if (t.accentColor) applyTheme(t.accentColor)
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
          onLogin={() => { setSession(getSession()); setShowLogin(false) }}
        />
      )}
      <Header
        session={session}
        onShowLogin={() => setShowLogin(true)}
        onLogout={() => { logout(); setSession(null) }}
        logoUrl={logoUrl}
      >
        <HomePage bannerUrl={bannerUrl} />
      </Header>
    </>
  )
}
