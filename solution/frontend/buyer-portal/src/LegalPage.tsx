import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell } from '@workspace/theme'
import { getLegalPage } from './api/legalApi'
import { useShopName } from './hooks/useShopName'
import { useLogoUrl } from './hooks/useLogoUrl'
import { useFooterLinks } from './hooks/useFooterLinks'
import { useFooterNotice } from './hooks/useFooterNotice'

interface Props {
  pageKey: string
}

export default function LegalPage({ pageKey }: Props) {
  const { t } = useTranslation()
  const brandName = useShopName()
  const logoUrl = useLogoUrl()
  const footerLinks = useFooterLinks()
  const footerNotice = useFooterNotice()
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getLegalPage(pageKey)
      .then(data => setContent(data.content))
      .catch(() => setContent(t('legal.loadError')))
      .finally(() => setLoading(false))
  }, [pageKey])

  return (
    <AppShell
      appName={t('app.name')}
      brandName={brandName}
      logoUrl={logoUrl}
      navLinks={[
        { label: t('nav.home'), href: '/' },
        { label: t('nav.catalog'), href: '/catalog' },
      ]}
      footerLinks={footerLinks}
      footerNotice={footerNotice}
    >
      <div className="legal-page">
        {loading ? (
          <p className="legal-loading">{t('legal.loading')}</p>
        ) : (
          <pre className="legal-content">{content}</pre>
        )}
        <a href="/" className="legal-back">{t('legal.back')}</a>
      </div>
    </AppShell>
  )
}
