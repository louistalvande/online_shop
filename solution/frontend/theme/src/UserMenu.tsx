import { useState, useRef, useEffect } from 'react'
import { UserIcon } from './icons'

interface UserMenuProps {
  label: string
  email?: string
  /** Login item shown when the user is unauthenticated (replaces settings/logout). */
  loginLabel?: string
  onLogin?: () => void
  settingsLabel?: string
  ordersLabel?: string
  logoutLabel?: string
  onSettings?: () => void
  onOrders?: () => void
  onLogout?: () => void
}

export function UserMenu({ label, email, loginLabel, onLogin, settingsLabel, ordersLabel, logoutLabel, onSettings, onOrders, onLogout }: UserMenuProps) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function onMouseDown(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', onMouseDown)
    return () => document.removeEventListener('mousedown', onMouseDown)
  }, [])

  return (
    <div ref={ref} className="user-menu">
      <button className="user-menu-trigger" onClick={() => setOpen(o => !o)} aria-label={label}>
        <UserIcon size={18} />
        <span className="user-menu-caret">{open ? '▲' : '▼'}</span>
      </button>
      {open && (
        <div className="user-menu-dropdown">
          {loginLabel ? (
            <button className="user-menu-item" onClick={() => { onLogin?.(); setOpen(false) }}>
              {loginLabel}
            </button>
          ) : (
            <>
              {email && <div className="user-menu-email">{email}</div>}
              {settingsLabel && (
                <button className="user-menu-item" onClick={() => { onSettings?.(); setOpen(false) }}>
                  {settingsLabel}
                </button>
              )}
              {ordersLabel && (
                <button className="user-menu-item" onClick={() => { onOrders?.(); setOpen(false) }}>
                  {ordersLabel}
                </button>
              )}
              {logoutLabel && (
                <button className="user-menu-item" onClick={() => { onLogout?.(); setOpen(false) }}>
                  {logoutLabel}
                </button>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
