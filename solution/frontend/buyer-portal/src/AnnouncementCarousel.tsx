import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { ImageCarousel } from '@workspace/theme'
import { fetchActiveAnnouncements } from './api/announcementApi'

/**
 * Fetches active announcements and renders them via the generic ImageCarousel.
 * Returns null when there are no active announcements.
 */
export default function AnnouncementCarousel() {
  const { t } = useTranslation()
  const [items, setItems] = useState<{ imageUrl?: string | null; textContent?: string | null; redirectUrl?: string | null; orientation?: 'PORTRAIT' | 'LANDSCAPE' | null }[]>([])
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    fetchActiveAnnouncements().then(data => {
      setItems(data.map(a => ({
        imageUrl: a.imageUrl,
        textContent: a.textContent,
        redirectUrl: a.redirectUrl,
        orientation: a.imageOrientation,
      })))
      setLoaded(true)
    })
  }, [])

  if (!loaded || items.length === 0) return null

  return (
    <ImageCarousel
      items={items}
      ariaLabel={t('carousel.label')}
      prevLabel={t('carousel.prev')}
      nextLabel={t('carousel.next')}
      goToLabel={t('carousel.goTo')}
    />
  )
}
