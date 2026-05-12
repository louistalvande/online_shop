import type { HTMLAttributes } from 'react'

export function Card({ style, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      style={{
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: 'var(--shadow)',
        ...style,
      }}
      {...props}
    />
  )
}
