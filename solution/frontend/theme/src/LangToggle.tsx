import { useState, useRef, useEffect } from 'react'

const LANGS = [
  { value: 'fr', abbr: 'FR', label: 'Français' },
  { value: 'en', abbr: 'EN', label: 'English' },
  { value: 'es', abbr: 'ES', label: 'Español' },
]

interface LangToggleProps {
  lang: string
  onChange: (lang: string) => void
}

export function LangToggle({ lang, onChange }: LangToggleProps) {
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
    <div ref={ref} className="lang-menu">
      <button
        className="lang-toggle"
        onClick={() => setOpen(o => !o)}
        aria-label="Language"
        aria-expanded={open}
      >
        {LANGS.find(l => l.value === lang)?.abbr ?? lang.toUpperCase()}
        <span className="user-menu-caret">{open ? '▲' : '▼'}</span>
      </button>
      {open && (
        <div className="user-menu-dropdown">
          {LANGS.map(l => (
            <button
              key={l.value}
              className="user-menu-item"
              style={{ fontWeight: l.value === lang ? 700 : 400 }}
              onClick={() => { onChange(l.value); setOpen(false) }}
            >
              {l.label}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
