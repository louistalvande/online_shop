import { GlobeIcon } from './icons'

interface LangToggleProps {
  lang: string
  onToggle: () => void
}

export function LangToggle({ lang, onToggle }: LangToggleProps) {
  return (
    <button
      onClick={onToggle}
      style={{
        background: 'none',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius)',
        padding: '4px 10px',
        fontSize: 13,
        fontWeight: 600,
        cursor: 'pointer',
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
        color: 'var(--text)',
        fontFamily: 'inherit',
      }}
    >
      <GlobeIcon size={14} />
      {lang.toUpperCase()}
    </button>
  )
}
