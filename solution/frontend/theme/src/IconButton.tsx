import type { ButtonHTMLAttributes, ReactNode } from 'react'

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode
}

export function IconButton({ style, ...props }: IconButtonProps) {
  return (
    <button
      style={{
        background: 'none',
        border: 'none',
        display: 'flex',
        alignItems: 'center',
        color: 'var(--text)',
        cursor: 'pointer',
        padding: 4,
        ...style,
      }}
      {...props}
    />
  )
}
