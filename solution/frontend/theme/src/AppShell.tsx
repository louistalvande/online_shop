import type { ReactNode } from 'react'
import { GalleryIcon } from './icons'

interface AppShellProps {
  appName: string
  brandName?: string
  logoUrl?: string
  onLogoClick?: () => void
  navLinks?: { label: string; href: string; onClick?: () => void; badge?: number }[]
  actions?: ReactNode
  children: ReactNode
}

export function AppShell({ appName, brandName, logoUrl, onLogoClick, navLinks = [], actions, children }: AppShellProps) {
  return (
    <>
      <header className="shell-header">
        <div className="shell-inner">
          <div
            className={['shell-brand', onLogoClick ? 'shell-brand--clickable' : ''].filter(Boolean).join(' ')}
            onClick={onLogoClick}
            role={onLogoClick ? 'button' : undefined}
            tabIndex={onLogoClick ? 0 : undefined}
            onKeyDown={onLogoClick ? (e) => { if (e.key === 'Enter' || e.key === ' ') onLogoClick() } : undefined}
          >
            {logoUrl
              ? <img src={logoUrl} alt={appName} className="shell-brand-logo" />
              : <GalleryIcon size={32} />
            }
            <div>
              <div className="shell-brand-name">{brandName ?? 'Catalogue de dessins'}</div>
              <div className="shell-brand-sub">{appName}</div>
            </div>
          </div>
          <nav className="shell-nav">
            {navLinks.map(l => (
              <a
                key={l.label}
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
        <p>© 2026 {brandName ?? 'Catalogue de dessins'}</p>
      </footer>
    </>
  )
}
