import './index.css'
import { useState } from 'react'
import { getSession, logout, type AdminSession } from './api/authApi'
import LoginPage from './LoginPage'
import Header from './Header'
import DashboardPage from './DashboardPage'

export default function App() {
  const [session, setSession] = useState<AdminSession | null>(getSession)

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  return (
    <Header session={session} onLogout={() => { logout(); setSession(null) }}>
      <DashboardPage onUnauthorized={() => setSession(null)} />
    </Header>
  )
}
