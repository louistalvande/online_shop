import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { IconButton, DotsHorizontalIcon } from '@workspace/theme'

export interface ActionItem {
  label: string
  icon: React.ReactNode
  onClick: () => void
  disabled?: boolean
  danger?: boolean
}

interface Props {
  actions: ActionItem[]
  title?: string
}

export default function ActionMenu({ actions, title }: Props) {
  const [open, setOpen] = useState(false)
  const [coords, setCoords] = useState({ top: 0, left: 0 })
  const triggerRef = useRef<HTMLSpanElement>(null)

  useEffect(() => {
    if (!open) return
    function handleClose(e: MouseEvent | KeyboardEvent) {
      if (e instanceof KeyboardEvent && e.key !== 'Escape') return
      if (e instanceof MouseEvent && triggerRef.current?.contains(e.target as Node)) return
      setOpen(false)
    }
    document.addEventListener('mousedown', handleClose)
    document.addEventListener('keydown', handleClose)
    return () => {
      document.removeEventListener('mousedown', handleClose)
      document.removeEventListener('keydown', handleClose)
    }
  }, [open])

  function handleTrigger() {
    if (!open && triggerRef.current) {
      const rect = triggerRef.current.getBoundingClientRect()
      setCoords({
        top: rect.bottom + window.scrollY + 4,
        left: rect.right + window.scrollX,
      })
    }
    setOpen(v => !v)
  }

  return (
    <>
      <span ref={triggerRef} style={{ display: 'inline-flex' }}>
        <IconButton
          onClick={handleTrigger}
          title={title}
          style={{ color: 'var(--text-muted)' }}
        >
          <DotsHorizontalIcon size={16} />
        </IconButton>
      </span>

      {open && createPortal(
        <div
          style={{
            position: 'absolute',
            top: coords.top,
            left: coords.left,
            transform: 'translateX(-100%)',
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 8,
            boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
            minWidth: 170,
            zIndex: 9999,
            overflow: 'hidden',
          }}
        >
          {actions.map((action, i) => (
            <button
              key={i}
              disabled={action.disabled}
              onMouseDown={e => {
                e.preventDefault()
                if (!action.disabled) { setOpen(false); action.onClick() }
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                width: '100%',
                padding: '10px 16px',
                background: 'none',
                border: 'none',
                cursor: action.disabled ? 'not-allowed' : 'pointer',
                fontSize: 14,
                color: action.disabled
                  ? 'var(--text-muted)'
                  : action.danger
                  ? '#dc2626'
                  : 'var(--text)',
                textAlign: 'left',
                opacity: action.disabled ? 0.5 : 1,
              }}
              onMouseEnter={e => {
                if (!action.disabled)
                  (e.currentTarget as HTMLButtonElement).style.background = 'rgba(0,0,0,0.05)'
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLButtonElement).style.background = 'none'
              }}
            >
              {action.icon}
              {action.label}
            </button>
          ))}
        </div>,
        document.body
      )}
    </>
  )
}
