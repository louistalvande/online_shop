import type { ReactNode } from 'react'
import { GalleryIcon } from './icons'

interface AppShellProps {
  appName: string
  logoUrl?: string
  navLinks?: { label: string; href: string; onClick?: () => void; badge?: number }[]
  actions?: ReactNode
  children: ReactNode
}

export function AppShell({ appName, logoUrl, navLinks = [], actions, children }: AppShellProps) {
  return (
    <>
      <header className="shell-header">
        <div className="shell-inner">
          <div className="shell-brand">
            {logoUrl
              ? <img src={logoUrl} alt={appName} className="shell-brand-logo" />
              : <GalleryIcon size={32} />
            }
            <div>
              <div className="shell-brand-name">Catalogue de dessins</div>
              <div className="shell-brand-sub">{appName}</div>
            </div>
          </div>
          <nav className="shell-nav">
            {navLinks.map(l => (
              <a
                key={l.href}
                href={l.href}
                className="shell-nav-link"
                onClick={l.onClick ? (e) => { e.preventDefault(); l.onClick!() } : undefined}
              >
                {l.label}
                {l.badge != null && l.badge > 0 && (
                  <span className="shell-nav-badge">{l.badge}</span>
                )}
              </a>
            ))}
            {actions}
          </nav>
        </div>
      </header>
      <main>{children}</main>
      <footer className="shell-footer">
        <p>© 2026 Catalogue de dessins</p>
      </footer>
    </>
  )
}
