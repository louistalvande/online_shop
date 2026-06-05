import './index.css'
import { useState, useEffect } from 'react'
import { getSession, logout, type AdminSession } from './api/authApi'
import { getShopTheme } from './api/themeApi'
import LoginPage from './LoginPage'
import Header from './Header'
import DashboardPage from './DashboardPage'
import ProfilePage from './ProfilePage'

export default function App() {
  const [session, setSession] = useState<AdminSession | null>(getSession)
  const [shopName, setShopName] = useState('')
  const [logoUrl, setLogoUrl] = useState<string | null>(null)

  useEffect(() => {
    getShopTheme().then(t => {
      if (t.shopName) setShopName(t.shopName)
      setLogoUrl(t.logoUrl ?? null)
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
  const [showProfile, setShowProfile] = useState(false)

  if (!session) {
    return <LoginPage shopName={shopName} logoUrl={logoUrl} onLogin={() => setSession(getSession())} />
  }

  return (
    <Header session={session} onLogout={() => { logout(); setSession(null) }} onSettings={() => setShowProfile(true)}>
      {showProfile
        ? <ProfilePage onBack={() => setShowProfile(false)} />
        : <DashboardPage onUnauthorized={() => setSession(null)} />
      }
    </Header>
  )
}
