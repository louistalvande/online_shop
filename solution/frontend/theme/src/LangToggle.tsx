import { GlobeIcon } from './icons'

interface LangToggleProps {
  lang: string
  onToggle: () => void
}

export function LangToggle({ lang, onToggle }: LangToggleProps) {
  return (
    <button className="lang-toggle" onClick={onToggle}>
      <GlobeIcon size={14} />
      {lang.toUpperCase()}
    </button>
  )
}
