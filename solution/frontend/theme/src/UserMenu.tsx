import { useState, useRef, useEffect } from 'react'
import type { CSSProperties } from 'react'
import { UserIcon } from './icons'

interface UserMenuProps {
  label: string
  email?: string
  settingsLabel: string
  logoutLabel: string
  onSettings?: () => void
  onLogout?: () => void
}

export function UserMenu({ label, email, settingsLabel, logoutLabel, onSettings, onLogout }: UserMenuProps) {
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
    <div ref={ref} style={{ position: 'relative' }}>
      <button onClick={() => setOpen(o => !o)} style={triggerStyle}>
        <UserIcon size={16} />
        <span>{label}</span>
        <span style={{ fontSize: 10, opacity: 0.6 }}>{open ? '▲' : '▼'}</span>
      </button>
      {open && (
        <div style={dropdownStyle}>
          {email && (
            <div style={emailStyle}>{email}</div>
          )}
          <button
            style={itemStyle}
            onMouseEnter={e => (e.currentTarget.style.background = 'var(--bg)')}
            onMouseLeave={e => (e.currentTarget.style.background = 'none')}
            onClick={() => { onSettings?.(); setOpen(false) }}
          >
            {settingsLabel}
          </button>
          <button
            style={itemStyle}
            onMouseEnter={e => (e.currentTarget.style.background = 'var(--bg)')}
            onMouseLeave={e => (e.currentTarget.style.background = 'none')}
            onClick={() => { onLogout?.(); setOpen(false) }}
          >
            {logoutLabel}
          </button>
        </div>
      )}
    </div>
  )
}

const triggerStyle: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 6,
  background: 'none',
  border: '1px solid var(--border)',
  borderRadius: 6,
  padding: '6px 10px',
  fontSize: 14,
  fontWeight: 500,
  color: 'var(--text)',
  cursor: 'pointer',
}

const dropdownStyle: CSSProperties = {
  position: 'absolute',
  right: 0,
  top: 'calc(100% + 8px)',
  background: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: 8,
  boxShadow: '0 4px 16px rgba(0,0,0,0.10)',
  minWidth: 180,
  zIndex: 100,
  overflow: 'hidden',
}

const emailStyle: CSSProperties = {
  padding: '10px 16px',
  fontSize: 13,
  color: 'var(--text-muted)',
  borderBottom: '1px solid var(--border)',
}

const itemStyle: CSSProperties = {
  display: 'block',
  width: '100%',
  padding: '10px 16px',
  textAlign: 'left',
  background: 'none',
  border: 'none',
  fontSize: 14,
  color: 'var(--text)',
  cursor: 'pointer',
}
