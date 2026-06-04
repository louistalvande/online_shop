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

function wcagLuminance(hex: string): number {
  const toLinear = (c: number) => c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
  const r = toLinear(parseInt(hex.slice(1, 3), 16) / 255)
  const g = toLinear(parseInt(hex.slice(3, 5), 16) / 255)
  const b = toLinear(parseInt(hex.slice(5, 7), 16) / 255)
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

function applyTheme(accentColor: string, bgColor: string) {
  document.documentElement.style.setProperty('--accent', accentColor)
  document.documentElement.style.setProperty('--accent-hover', darkenHex(accentColor, 20))
  document.documentElement.style.setProperty('--bg', bgColor)
  document.documentElement.style.setProperty('--btn-text', wcagLuminance(accentColor) > 0.179 ? '#1a2120' : '#ffffff')
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
        if (t.accentColor) applyTheme(t.accentColor, t.bgColor ?? '#f2f6f5')
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
