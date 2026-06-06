import { useTranslation } from 'react-i18next'

export function useFooterLinks() {
  const { t } = useTranslation()
  return [
    { label: t('footer.reproduction'), href: '/legal/reproduction' },
    { label: t('footer.returnPolicy'), href: '/legal/retour' },
    { label: t('footer.privacy'),      href: '/legal/confidentialite' },
    { label: t('footer.cgv'),          href: '/legal/cgv' },
    { label: t('footer.legal'),        href: '/legal/mentions-legales' },
    { label: t('footer.about'),        href: '/legal/apropos' },
  ]
}
