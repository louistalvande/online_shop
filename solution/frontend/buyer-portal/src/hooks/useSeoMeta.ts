import { useEffect } from 'react'

interface SeoMeta {
  title?: string | null
  description?: string | null
  keywords?: string | null
  ogImage?: string | null
  canonical?: string | null
  noindex?: boolean
  googleVerification?: string | null
  bingVerification?: string | null
  ga4Id?: string | null
}

function upsertMeta(attr: string, attrValue: string, content: string) {
  let el = document.querySelector(`meta[${attr}="${attrValue}"]`) as HTMLMetaElement | null
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, attrValue)
    document.head.appendChild(el)
  }
  el.content = content
}

function upsertLink(rel: string, href: string) {
  let el = document.querySelector(`link[rel="${rel}"]`) as HTMLLinkElement | null
  if (!el) {
    el = document.createElement('link')
    el.rel = rel
    document.head.appendChild(el)
  }
  el.href = href
}

function injectGa4(id: string) {
  if (document.querySelector(`script[data-ga4="${id}"]`)) return
  const s1 = document.createElement('script')
  s1.async = true
  s1.src = `https://www.googletagmanager.com/gtag/js?id=${id}`
  s1.setAttribute('data-ga4', id)
  document.head.appendChild(s1)
  const s2 = document.createElement('script')
  s2.text = `window.dataLayer=window.dataLayer||[];function gtag(){dataLayer.push(arguments);}gtag('js',new Date());gtag('config','${id}');`
  document.head.appendChild(s2)
}

/** Injects SEO-related tags directly into the document head via DOM manipulation. */
export function useSeoMeta(meta: SeoMeta) {
  useEffect(() => {
    if (meta.title) document.title = meta.title
    if (meta.description) upsertMeta('name', 'description', meta.description)
    if (meta.keywords) upsertMeta('name', 'keywords', meta.keywords)
    if (meta.ogImage) upsertMeta('property', 'og:image', meta.ogImage)
    if (meta.canonical) upsertLink('canonical', meta.canonical)
    if (meta.noindex) upsertMeta('name', 'robots', 'noindex,nofollow')
    if (meta.googleVerification) upsertMeta('name', 'google-site-verification', meta.googleVerification)
    if (meta.bingVerification) upsertMeta('name', 'msvalidate.01', meta.bingVerification)
    if (meta.ga4Id) injectGa4(meta.ga4Id)
  }, [
    meta.title, meta.description, meta.keywords, meta.ogImage,
    meta.canonical, meta.noindex, meta.googleVerification,
    meta.bingVerification, meta.ga4Id,
  ])
}
