import { useState } from 'react'
import './index.css'
import { getSession, logout } from './api/authApi'
import LoginPage from './LoginPage'
import Header from './Header'
import DashboardPage from './DashboardPage'

export default function App() {
  const [session, setSession] = useState(getSession)

  if (!session) {
    return <LoginPage onLogin={() => setSession(getSession())} />
  }

  return (
    <Header onLogout={() => { logout(); setSession(null) }}>
      <DashboardPage />
    </Header>
  )
}
