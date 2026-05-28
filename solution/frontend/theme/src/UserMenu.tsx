import { useState, useRef, useEffect } from 'react'
import { UserIcon } from './icons'

interface UserMenuProps {
  label: string
  email?: string
  settingsLabel: string
  ordersLabel?: string
  logoutLabel: string
  onSettings?: () => void
  onOrders?: () => void
  onLogout?: () => void
}

export function UserMenu({ label, email, settingsLabel, ordersLabel, logoutLabel, onSettings, onOrders, onLogout }: UserMenuProps) {
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
      <button className="user-menu-trigger" onClick={() => setOpen(o => !o)}>
        <UserIcon size={16} />
        <span>{label}</span>
        <span className="user-menu-caret">{open ? '▲' : '▼'}</span>
      </button>
      {open && (
        <div className="user-menu-dropdown">
          {email && <div className="user-menu-email">{email}</div>}
          <button className="user-menu-item" onClick={() => { onSettings?.(); setOpen(false) }}>
            {settingsLabel}
          </button>
          {ordersLabel && (
            <button className="user-menu-item" onClick={() => { onOrders?.(); setOpen(false) }}>
              {ordersLabel}
            </button>
          )}
          <button className="user-menu-item" onClick={() => { onLogout?.(); setOpen(false) }}>
            {logoutLabel}
          </button>
        </div>
      )}
    </div>
  )
}
