import { useState, useEffect, useCallback } from 'react'

export interface CarouselItem {
  imageUrl?: string | null
  textContent?: string | null
  redirectUrl?: string | null
  orientation?: 'PORTRAIT' | 'LANDSCAPE' | null
}

interface Props {
  items: CarouselItem[]
  autoAdvanceMs?: number
  prevLabel?: string
  nextLabel?: string
  goToLabel?: string
  ariaLabel?: string
}

/**
 * Generic auto-advancing image carousel usable anywhere in the shell or page body.
 * Landscape images fill full width; portrait images are centred at reduced width.
 * Hidden when items array is empty.
 */
export function ImageCarousel({
  items,
  autoAdvanceMs = 5000,
  prevLabel = '‹',
  nextLabel = '›',
  goToLabel = 'Go to',
  ariaLabel = 'Image carousel',
}: Props) {
  const [index, setIndex] = useState(0)

  const next = useCallback(() => setIndex(i => (i + 1) % items.length), [items.length])
  const prev = useCallback(() => setIndex(i => (i - 1 + items.length) % items.length), [items.length])

  useEffect(() => {
    setIndex(0)
  }, [items])

  useEffect(() => {
    if (items.length < 2) return
    const id = setInterval(next, autoAdvanceMs)
    return () => clearInterval(id)
  }, [items.length, autoAdvanceMs, next])

  if (items.length === 0) return null

  const current = items[index]
  const isPortrait = current.orientation === 'PORTRAIT'

  function handleClick() {
    if (current.redirectUrl) window.open(current.redirectUrl, '_blank', 'noopener')
  }

  return (
    <section className="announcement-carousel" aria-label={ariaLabel}>
      <div
        className={`carousel-slide ${current.redirectUrl ? 'carousel-slide--clickable' : ''}`}
        onClick={current.redirectUrl ? handleClick : undefined}
        role={current.redirectUrl ? 'link' : undefined}
        tabIndex={current.redirectUrl ? 0 : undefined}
        onKeyDown={current.redirectUrl ? (e) => { if (e.key === 'Enter') handleClick() } : undefined}
      >
        {current.imageUrl && (
          <div className={`carousel-image-wrapper ${isPortrait ? 'carousel-image--portrait' : 'carousel-image--landscape'}`}>
            <img src={current.imageUrl} alt={current.textContent ?? ''} className="carousel-image" />
          </div>
        )}
        {current.textContent && (
          <p className="carousel-text">{current.textContent}</p>
        )}
      </div>

      {items.length > 1 && (
        <div className="carousel-controls">
          <button className="carousel-btn" onClick={prev} aria-label={prevLabel}>‹</button>
          <div className="carousel-dots">
            {items.map((_, i) => (
              <button
                key={i}
                className={`carousel-dot ${i === index ? 'carousel-dot--active' : ''}`}
                onClick={() => setIndex(i)}
                aria-label={`${goToLabel} ${i + 1}`}
              />
            ))}
          </div>
          <button className="carousel-btn" onClick={next} aria-label={nextLabel}>›</button>
        </div>
      )}
    </section>
  )
}
