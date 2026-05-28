import React, { useEffect } from 'react'

interface SnackbarProps {
  message: string
  variant?: 'success' | 'error'
  onDismiss?: () => void
  duration?: number
}

const variantStyles: Record<string, React.CSSProperties> = {
  success: { background: '#166534', color: '#f0fdf4', borderLeft: '4px solid #4ade80' },
  error:   { background: '#7f1d1d', color: '#fef2f2', borderLeft: '4px solid #f87171' },
}

/** Fixed bottom-center notification bar. Auto-dismisses after `duration` ms (default 3000). */
export function Snackbar({ message, variant = 'success', onDismiss, duration = 3000 }: SnackbarProps) {
  useEffect(() => {
    if (!onDismiss) return
    const id = setTimeout(onDismiss, duration)
    return () => clearTimeout(id)
  }, [message, onDismiss, duration])

  const bar: React.CSSProperties = {
    position: 'fixed',
    bottom: 28,
    left: '50%',
    transform: 'translateX(-50%)',
    display: 'flex',
    alignItems: 'center',
    gap: 16,
    padding: '12px 20px',
    borderRadius: 6,
    fontSize: 14,
    fontWeight: 500,
    boxShadow: '0 4px 16px rgba(0,0,0,0.25)',
    zIndex: 200,
    minWidth: 260,
    maxWidth: 480,
    ...variantStyles[variant],
  }

  return (
    <div style={bar} role="status" aria-live="polite">
      <span style={{ flex: 1 }}>{message}</span>
      {onDismiss && (
        <button
          onClick={onDismiss}
          aria-label="Dismiss"
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', fontSize: 16, padding: 0, lineHeight: 1 }}
        >
          ✕
        </button>
      )}
    </div>
  )
}
