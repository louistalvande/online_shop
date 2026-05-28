import type { HTMLAttributes } from 'react'

export function Card({ className, style, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={['card', className].filter(Boolean).join(' ')}
      style={style}
      {...props}
    />
  )
}
