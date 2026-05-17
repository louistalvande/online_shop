import './index.css'
import { useState } from 'react'
import { getSession, logout, type BuyerSession } from './api/authApi'
import LoginModal from './LoginModal'
import Header from './Header'
import HomePage from './HomePage'

interface Props {
  openLogin?: boolean
}

export default function App({ openLogin = false }: Props) {
  const [session, setSession] = useState<BuyerSession | null>(getSession)
  const [showLogin, setShowLogin] = useState(openLogin)

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
