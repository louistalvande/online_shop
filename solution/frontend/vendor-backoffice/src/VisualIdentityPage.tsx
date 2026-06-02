import { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { uploadVendorLogo, deleteLogo, uploadVendorBanner, deleteBanner } from './api/profileApi'
import { getShopTheme, updateShopTheme } from './api/shopConfigApi'
import AnnouncementsPage from './AnnouncementsPage'

interface Props {
  /** Called after a logo upload or deletion so the parent can refresh the header. */
  onLogoChange?: (url: string | null) => void
}

/** Vendor back-office page for managing the shop's visual identity (BES-VND-015). */
export default function VisualIdentityPage({ onLogoChange }: Props) {
  const { t } = useTranslation()

  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [bannerUrl, setBannerUrl] = useState<string | null>(null)
  const [accentColor, setAccentColor] = useState('#4e8b82')
  const [uploadingLogo, setUploadingLogo] = useState(false)
  const [uploadingBanner, setUploadingBanner] = useState(false)
  const logoInputRef = useRef<HTMLInputElement>(null)
  const bannerInputRef = useRef<HTMLInputElement>(null)
  const [savingColor, setSavingColor] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  useEffect(() => {
    getShopTheme()
      .then(theme => {
        setLogoUrl(theme.logoUrl ?? null)
        setBannerUrl(theme.bannerUrl ?? null)
        setAccentColor(theme.accentColor ?? '#4e8b82')
      })
      .catch(() => {})
  }, [])

  function flash(msg: string, kind: 'success' | 'error') {
    if (kind === 'success') { setSuccessMsg(msg); setErrorMsg('') }
    else { setErrorMsg(msg); setSuccessMsg('') }
    setTimeout(() => { setSuccessMsg(''); setErrorMsg('') }, 3500)
  }

  async function handleLogoUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadingLogo(true)
    try {
      const url = await uploadVendorLogo(file)
      setLogoUrl(url)
      onLogoChange?.(url)
      flash(t('profile.logo.success'), 'success')
    } catch (err: unknown) {
      const serverMsg = (err as { serverMessage?: string })?.serverMessage
      flash(serverMsg ?? t('profile.logo.error'), 'error')
    } finally {
      setUploadingLogo(false)
      e.target.value = ''
    }
  }

  async function handleRemoveLogo() {
    try {
      await deleteLogo()
      setLogoUrl(null)
      onLogoChange?.(null)
      flash(t('profile.logo.removed'), 'success')
    } catch {
      flash(t('profile.error.generic'), 'error')
    }
  }

  async function handleBannerUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadingBanner(true)
    try {
      const url = await uploadVendorBanner(file)
      setBannerUrl(url)
      flash(t('profile.banner.success'), 'success')
    } catch {
      flash(t('profile.banner.error'), 'error')
    } finally {
      setUploadingBanner(false)
      e.target.value = ''
    }
  }

  async function handleRemoveBanner() {
    try {
      await deleteBanner()
      setBannerUrl(null)
      flash(t('profile.banner.removed'), 'success')
    } catch {
      flash(t('profile.error.generic'), 'error')
    }
  }

  async function handleSaveColor() {
    setSavingColor(true)
    try {
      await updateShopTheme({ accentColor })
      flash(t('profile.theme.success'), 'success')
    } catch {
      flash(t('profile.error.generic'), 'error')
    } finally {
      setSavingColor(false)
    }
  }

  const sectionStyle: React.CSSProperties = { marginBottom: 48 }
  const sectionHeadStyle: React.CSSProperties = {
    fontSize: 17, fontWeight: 700, margin: '0 0 16px',
    paddingBottom: 12, borderBottom: '1px solid var(--border)',
  }
  const inputStyle: React.CSSProperties = {
    padding: '8px 12px', border: '1px solid var(--border)',
    borderRadius: 6, fontSize: 14, background: 'var(--surface)', color: 'var(--text)',
  }
  const placeholderBox = (label: string, w: number, h: number): React.CSSProperties => ({
    width: w, height: h, background: 'var(--bg)', borderRadius: 6,
    border: '2px dashed var(--border)', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    fontSize: 12, color: 'var(--text-muted)', flexShrink: 0,
  })

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: '48px 24px' }}>
      <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, marginBottom: 40 }}>
        {t('visual.title')}
      </h1>

      {successMsg && (
        <div role="status" style={{ padding: '12px 16px', borderRadius: 6, background: '#dcfce7', color: '#15803d', marginBottom: 24, fontSize: 14 }}>
          {successMsg}
        </div>
      )}
      {errorMsg && (
        <div role="alert" style={{ padding: '12px 16px', borderRadius: 6, background: '#fee2e2', color: '#dc2626', marginBottom: 24, fontSize: 14 }}>
          {errorMsg}
        </div>
      )}

      {/* ── Logo ── */}
      <section style={sectionStyle}>
        <h2 style={sectionHeadStyle}>{t('visual.section.logo')}</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
          {logoUrl
            ? <img src={logoUrl} alt={t('profile.logo.preview')} style={{ height: 80, width: 'auto', objectFit: 'contain', borderRadius: 6, border: '1px solid var(--border)', flexShrink: 0 }} />
            : <div style={placeholderBox('Logo', 120, 80)} />
          }
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <Button type="button" variant="secondary" size="sm" disabled={uploadingLogo}
              onClick={() => logoInputRef.current?.click()}>
              {uploadingLogo ? t('profile.logo.uploading') : logoUrl ? t('profile.logo.change') : t('profile.logo.add')}
            </Button>
            <input id="vi-logo-upload" ref={logoInputRef} type="file" accept="image/jpeg,image/png,image/webp"
              style={{ display: 'none' }} onChange={handleLogoUpload} />
            {logoUrl && (
              <Button type="button" variant="ghost" size="sm" onClick={handleRemoveLogo}>
                {t('profile.logo.remove')}
              </Button>
            )}
          </div>
        </div>
      </section>

      {/* ── Couleur d'accentuation ── */}
      <section style={sectionStyle}>
        <h2 style={sectionHeadStyle}>{t('visual.section.color')}</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
          <input
            type="color"
            value={accentColor}
            onChange={e => setAccentColor(e.target.value)}
            style={{ width: 44, height: 36, padding: 2, border: '1px solid var(--border)', borderRadius: 6, cursor: 'pointer', background: 'none' }}
          />
          <input
            style={{ ...inputStyle, width: 110 }}
            value={accentColor}
            onChange={e => { if (/^#[0-9a-fA-F]{0,6}$/.test(e.target.value)) setAccentColor(e.target.value) }}
            placeholder="#4e8b82"
            maxLength={7}
          />
          {accentColor !== '#4e8b82' && (
            <Button type="button" variant="ghost" size="sm" onClick={() => setAccentColor('#4e8b82')}>
              {t('profile.theme.reset')}
            </Button>
          )}
          <Button type="button" size="sm" disabled={savingColor} onClick={handleSaveColor}>
            {savingColor ? t('profile.saving') : t('profile.save')}
          </Button>
        </div>
        <p style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 10 }}>{t('profile.theme.hint')}</p>
      </section>

      {/* ── Bannière ── */}
      <section style={sectionStyle}>
        <h2 style={sectionHeadStyle}>{t('visual.section.banner')}</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
          {bannerUrl
            ? <img src={bannerUrl} alt={t('profile.banner.preview')} style={{ height: 80, maxWidth: 240, objectFit: 'cover', borderRadius: 6, border: '1px solid var(--border)', flexShrink: 0 }} />
            : <div style={placeholderBox('Bannière', 240, 80)} />
          }
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <Button type="button" variant="secondary" size="sm" disabled={uploadingBanner}
              onClick={() => bannerInputRef.current?.click()}>
              {uploadingBanner ? t('profile.banner.uploading') : bannerUrl ? t('profile.banner.change') : t('profile.banner.add')}
            </Button>
            <input id="vi-banner-upload" ref={bannerInputRef} type="file" accept="image/jpeg,image/png,image/webp"
              style={{ display: 'none' }} onChange={handleBannerUpload} />
            {bannerUrl && (
              <Button type="button" variant="ghost" size="sm" onClick={handleRemoveBanner}>
                {t('profile.banner.remove')}
              </Button>
            )}
          </div>
        </div>
      </section>

      {/* ── Carrousel ── */}
      <section style={sectionStyle}>
        <h2 style={sectionHeadStyle}>{t('visual.section.carousel')}</h2>
        <AnnouncementsPage embedded />
      </section>
    </div>
  )
}
