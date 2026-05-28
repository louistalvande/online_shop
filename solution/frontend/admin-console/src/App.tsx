import './index.css'
import { useState } from 'react'
import { getSession, logout, type AdminSession } from './api/authApi'
import LoginPage from './LoginPage'
import Header from './Header'
import DashboardPage from './DashboardPage'
import ProfilePage from './ProfilePage'

export default function App() {
  const [session, setSession] = useState<AdminSession | null>(getSession)
  const [showProfile, setShowProfile] = useState(false)

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
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
