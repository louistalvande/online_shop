import type { ReactNode } from 'react'
import { GalleryIcon } from './icons'

type NavLink = { label: string; href: string; onClick?: () => void; badge?: number }

interface AppShellProps {
  appName: string
  brandName?: string
  logoUrl?: string
  onLogoClick?: () => void
  centeredBrand?: boolean
  leftNavLinks?: NavLink[]
  navLinks?: NavLink[]
  actions?: ReactNode
  footerLinks?: { label: string; href: string }[]
  footerNotice?: string
  children: ReactNode
}

function NavLinkItem({ l }: { l: NavLink }) {
  return (
    <a
      href={l.href}
      className="shell-nav-link"
      onClick={l.onClick ? (e) => { e.preventDefault(); l.onClick!() } : undefined}
    >
      {l.label}
      {l.badge != null && l.badge > 0 && (
        <span className="shell-nav-badge">{l.badge}</span>
      )}
    </a>
  )
}

export function AppShell({ appName, brandName, logoUrl, onLogoClick, centeredBrand, leftNavLinks = [], navLinks = [], actions, footerLinks, footerNotice, children }: AppShellProps) {
  const brand = (
    <div
      className={['shell-brand', onLogoClick ? 'shell-brand--clickable' : ''].filter(Boolean).join(' ')}
      onClick={onLogoClick}
      role={onLogoClick ? 'button' : undefined}
      tabIndex={onLogoClick ? 0 : undefined}
      onKeyDown={onLogoClick ? (e) => { if (e.key === 'Enter' || e.key === ' ') onLogoClick() } : undefined}
    >
      {logoUrl
        ? <img src={logoUrl} alt={appName} className="shell-brand-logo" />
        : (
          <>
            <GalleryIcon size={32} />
            <div>
              <div className="shell-brand-name">{brandName ?? ''}</div>
              <div className="shell-brand-sub">{appName}</div>
            </div>
          </>
        )
      }
    </div>
  )

  return (
    <>
      <header className="shell-header">
        <div className={['shell-inner', centeredBrand ? 'shell-inner--centered' : ''].filter(Boolean).join(' ')}>
          {centeredBrand ? (
            <>
              <nav className="shell-nav shell-nav--left">
                {leftNavLinks.map(l => <NavLinkItem key={l.label} l={l} />)}
              </nav>
              {brand}
              <nav className="shell-nav shell-nav--right">
                <div className="shell-nav-links">
                  {navLinks.map(l => <NavLinkItem key={l.label} l={l} />)}
                </div>
                {actions && <div className="shell-nav-actions">{actions}</div>}
              </nav>
            </>
          ) : (
            <>
              {brand}
              <nav className="shell-nav">
                {navLinks.map(l => <NavLinkItem key={l.label} l={l} />)}
                {actions}
              </nav>
            </>
          )}
        </div>
      </header>
      <main>{children}</main>
      <footer className="shell-footer">
        <p>{footerNotice ?? `© 2026 ${brandName ?? ''}`}</p>
        {footerLinks && footerLinks.length > 0 && (
          <nav className="shell-footer-nav">
            {footerLinks.map(l => (
              <a key={l.href} href={l.href} className="shell-footer-link">{l.label}</a>
            ))}
          </nav>
        )}
      </footer>
    </>
  )
}
