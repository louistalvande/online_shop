import type { ReactNode } from 'react'
import { GalleryIcon } from './icons'

interface AppShellProps {
  appName: string
  navLinks?: { label: string; href: string }[]
  actions?: ReactNode
  children: ReactNode
}

export function AppShell({ appName, navLinks = [], actions, children }: AppShellProps) {
  return (
    <>
      <header style={headerStyle}>
        <div style={innerStyle}>
          <div style={brandStyle}>
            <GalleryIcon size={32} />
            <div>
              <div style={brandNameStyle}>Catalogue de dessins</div>
              <div style={brandSubStyle}>{appName}</div>
            </div>
          </div>
          <nav style={navStyle}>
            {navLinks.map(l => (
              <a key={l.href} href={l.href} style={navLinkStyle}>{l.label}</a>
            ))}
            {actions}
          </nav>
        </div>
      </header>
      <main>{children}</main>
      <footer style={footerStyle}>
        <p>© 2026 Catalogue de dessins</p>
      </footer>
    </>
  )
}

const headerStyle: React.CSSProperties = {
  background: 'var(--surface)',
  borderBottom: '1px solid var(--border)',
  position: 'sticky',
  top: 0,
  zIndex: 10,
}
const innerStyle: React.CSSProperties = {
  maxWidth: 1100,
  margin: '0 auto',
  padding: '0 24px',
  height: 68,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
}
const brandStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 12,
  color: 'var(--text)',
}
const brandNameStyle: React.CSSProperties = {
  fontWeight: 700,
  fontSize: 16,
  letterSpacing: '-0.01em',
}
const brandSubStyle: React.CSSProperties = {
  fontSize: 11,
  color: 'var(--text-muted)',
  marginTop: 1,
}
const navStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 24,
}
const navLinkStyle: React.CSSProperties = {
  fontSize: 14,
  fontWeight: 500,
  color: 'var(--text)',
}
const footerStyle: React.CSSProperties = {
  borderTop: '1px solid var(--border)',
  padding: '20px 24px',
  textAlign: 'center',
  fontSize: 13,
  color: 'var(--text-muted)',
}
