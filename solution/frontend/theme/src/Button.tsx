import type { ButtonHTMLAttributes } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: 'sm' | 'md'
}

const styles: Record<Variant, React.CSSProperties> = {
  primary: {
    background: 'var(--accent)',
    color: '#fff',
    border: 'none',
  },
  secondary: {
    background: 'var(--surface)',
    color: 'var(--text)',
    border: '1px solid var(--border)',
  },
  ghost: {
    background: 'none',
    color: 'var(--text)',
    border: 'none',
  },
}

export function Button({ variant = 'primary', size = 'md', style, ...props }: ButtonProps) {
  return (
    <button
      style={{
        ...styles[variant],
        borderRadius: 'var(--radius)',
        padding: size === 'sm' ? '6px 12px' : '10px 20px',
        fontSize: size === 'sm' ? 13 : 15,
        fontWeight: 600,
        cursor: 'pointer',
        fontFamily: 'inherit',
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
        ...style,
      }}
      {...props}
    />
  )
}
