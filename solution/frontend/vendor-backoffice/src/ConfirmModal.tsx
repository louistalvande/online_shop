import { useEffect } from 'react'

interface Props {
  title: string
  message: string
  confirmLabel: string
  cancelLabel: string
  onConfirm: () => void
  onCancel: () => void
  variant?: 'primary' | 'danger'
}

/** Themed confirmation dialog — replaces window.confirm(). */
export default function ConfirmModal({ title, message, confirmLabel, cancelLabel, onConfirm, onCancel, variant = 'primary' }: Props) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onCancel() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onCancel])

  return (
    <div className="confirm-overlay" role="dialog" aria-modal="true" aria-labelledby="confirm-title" onClick={onCancel}>
      <div className="confirm-modal" onClick={e => e.stopPropagation()}>
        <div className={`confirm-icon confirm-icon--${variant}`}>
          {variant === 'danger' ? '!' : '✓'}
        </div>
        <h2 id="confirm-title" className="confirm-title">{title}</h2>
        <p className="confirm-message">{message}</p>
        <div className="confirm-actions">
          <button className="confirm-btn confirm-btn--cancel" onClick={onCancel} autoFocus>
            {cancelLabel}
          </button>
          <button className={`confirm-btn confirm-btn--${variant}`} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
