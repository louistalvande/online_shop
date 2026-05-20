import type { ButtonHTMLAttributes, ReactNode } from 'react'

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode
}

export function IconButton({ className, style: _style, ...props }: IconButtonProps) {
  return (
    <button
      className={['icon-btn', className].filter(Boolean).join(' ')}
      {...props}
    />
  )
}
