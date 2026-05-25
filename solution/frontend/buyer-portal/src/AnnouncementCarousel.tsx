import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { fetchActiveAnnouncements, type PublicAnnouncement } from './api/announcementApi'

/** Auto-advances every 5 seconds. */
const AUTO_ADVANCE_MS = 5000

/**
 * Scrolling announcement carousel for the buyer portal home page (US-ANN-01).
 * Landscape images fill the full carousel width; portrait images are centred at reduced width.
 * Returns null when there are no active announcements (carousel hidden per acceptance criteria).
 */
export default function AnnouncementCarousel() {
  const { t } = useTranslation()
  const [items, setItems]     = useState<PublicAnnouncement[]>([])
  const [index, setIndex]     = useState(0)
  const [loaded, setLoaded]   = useState(false)

  useEffect(() => {
    fetchActiveAnnouncements().then(data => {
      setItems(data)
      setLoaded(true)
    })
  }, [])

  const next = useCallback(() => setIndex(i => (i + 1) % items.length), [items.length])
  const prev = useCallback(() => setIndex(i => (i - 1 + items.length) % items.length), [items.length])

  useEffect(() => {
    if (items.length < 2) return
    const id = setInterval(next, AUTO_ADVANCE_MS)
    return () => clearInterval(id)
  }, [items.length, next])

  if (!loaded || items.length === 0) return null

  const current = items[index]
  const isPortrait = current.imageOrientation === 'PORTRAIT'

  function handleClick() {
    if (current.redirectUrl) window.open(current.redirectUrl, '_blank', 'noopener')
  }

  return (
    <section className="announcement-carousel" aria-label={t('carousel.label')}>
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
          <button className="carousel-btn" onClick={prev} aria-label={t('carousel.prev')}>‹</button>
          <div className="carousel-dots">
            {items.map((_, i) => (
              <button
                key={i}
                className={`carousel-dot ${i === index ? 'carousel-dot--active' : ''}`}
                onClick={() => setIndex(i)}
                aria-label={`${t('carousel.goTo')} ${i + 1}`}
              />
            ))}
          </div>
          <button className="carousel-btn" onClick={next} aria-label={t('carousel.next')}>›</button>
        </div>
      )}
    </section>
  )
}
