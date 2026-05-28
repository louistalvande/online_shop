import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, LangToggle } from '@workspace/theme'
import { getSession, logout } from './api/authApi'
import { getProfile, updateProfile, uploadVendorLogo, deleteLogo, uploadVendorBanner, deleteBanner, type ProfileData } from './api/profileApi'
import { getShopTheme, updateShopTheme } from './api/shopConfigApi'

type Tab = 'profile' | 'security'

export default function ProfilePage() {
  const { t, i18n } = useTranslation()
  const session = getSession()

  const [activeTab, setActiveTab] = useState<Tab>('profile')
  const [profile, setProfile] = useState<ProfileData | null>(null)
  const [loadError, setLoadError] = useState(false)

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phone, setPhone] = useState('')
  const [addressLine, setAddressLine] = useState('')
  const [city, setCity] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [countryCode, setCountryCode] = useState('')
  const [language, setLanguage] = useState<'FR' | 'EN'>('FR')

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [saving, setSaving] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')
  const [errorMsg, setErrorMsg] = useState('')
  const [logoUrl, setLogoUrl] = useState<string | null>(null)
  const [uploadingLogo, setUploadingLogo] = useState(false)
  const [bannerUrl, setBannerUrl] = useState<string | null>(null)
  const [uploadingBanner, setUploadingBanner] = useState(false)
  const [accentColor, setAccentColor] = useState('#4e8b82')

  useEffect(() => {
    getShopTheme()
      .then(t => {
        setLogoUrl(t.logoUrl ?? null)
        setBannerUrl(t.bannerUrl ?? null)
        setAccentColor(t.accentColor ?? '#4e8b82')
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!session) { window.location.href = import.meta.env.BASE_URL; return }
    getProfile()
      .then(p => {
        setProfile(p)
        setFirstName(p.firstName)
        setLastName(p.lastName)
        setPhone(p.phone ?? '')
        setAddressLine(p.addressLine ?? '')
        setCity(p.city ?? '')
        setPostalCode(p.postalCode ?? '')
        setCountryCode(p.countryCode ?? '')
        setLanguage(p.language)
      })
      .catch(() => setLoadError(true))
  }, [])

  function clearMessages() { setSuccessMsg(''); setErrorMsg('') }

  async function handleLogoUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadingLogo(true)
    clearMessages()
    try {
      const url = await uploadVendorLogo(file)
      setLogoUrl(url)
      setSuccessMsg(t('profile.logo.success'))
    } catch {
      setErrorMsg(t('profile.logo.error'))
    } finally {
      setUploadingLogo(false)
      e.target.value = ''
    }
  }

  async function handleRemoveLogo() {
    clearMessages()
    try {
      await deleteLogo()
      setLogoUrl(null)
      setSuccessMsg(t('profile.logo.removed'))
    } catch {
      setErrorMsg(t('profile.error.generic'))
    }
  }

  async function handleBannerUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadingBanner(true)
    clearMessages()
    try {
      const url = await uploadVendorBanner(file)
      setBannerUrl(url)
      setSuccessMsg(t('profile.banner.success'))
    } catch {
      setErrorMsg(t('profile.banner.error'))
    } finally {
      setUploadingBanner(false)
      e.target.value = ''
    }
  }

  async function handleRemoveBanner() {
    clearMessages()
    try {
      await deleteBanner()
      setBannerUrl(null)
      setSuccessMsg(t('profile.banner.removed'))
    } catch {
      setErrorMsg(t('profile.error.generic'))
    }
  }

  async function handleSaveProfile(e: React.FormEvent) {
    e.preventDefault()
    clearMessages()
    setSaving(true)
    try {
      const updated = await updateProfile({ firstName, lastName, phone: phone || undefined, addressLine: addressLine || undefined, city: city || undefined, postalCode: postalCode || undefined, countryCode: countryCode || undefined, language })
      await updateShopTheme({ accentColor })
      setProfile(updated)
      setSuccessMsg(t('profile.success'))
    } catch {
      setErrorMsg(t('profile.error.generic'))
    } finally {
      setSaving(false)
    }
  }

  async function handleSavePassword(e: React.FormEvent) {
    e.preventDefault()
    clearMessages()
    if (newPassword !== confirmPassword) {
      setErrorMsg(t('profile.error.passwordMismatch'))
      return
    }
    setSaving(true)
    try {
      await updateProfile({ currentPassword, newPassword, confirmPassword })
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('')
      setSuccessMsg(t('profile.security.success'))
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      setErrorMsg(code === 'WRONG_PASSWORD' ? t('profile.error.wrongPassword') : t('profile.error.generic'))
    } finally {
      setSaving(false)
    }
  }

  const tabStyle = (active: boolean): React.CSSProperties => ({
    padding: '10px 24px',
    background: 'none',
    border: 'none',
    borderBottom: active ? '2px solid var(--accent)' : '2px solid transparent',
    fontWeight: active ? 600 : 400,
    color: active ? 'var(--accent)' : 'var(--text-muted)',
    cursor: 'pointer',
    fontSize: 15,
  })

  const fieldStyle: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: 6 }
  const labelStyle: React.CSSProperties = { fontSize: 14, fontWeight: 500, color: 'var(--text)' }
  const inputStyle: React.CSSProperties = { padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 14, background: 'var(--surface)', color: 'var(--text)', width: '100%', boxSizing: 'border-box' }
  const readonlyStyle: React.CSSProperties = { ...inputStyle, background: 'var(--bg)', color: 'var(--text-muted)' }
  const rowStyle: React.CSSProperties = { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }

  if (loadError) {
    return (
      <div style={{ maxWidth: 480, margin: '120px auto', textAlign: 'center' }}>
        <p style={{ color: 'var(--text-muted)' }}>{t('profile.error.load')}</p>
        <Button onClick={() => window.location.reload()}>{t('profile.retry')}</Button>
      </div>
    )
  }

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.dashboard'), href: '/' },
        { label: t('nav.orders'), href: '/#orders' },
      ]}
      actions={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <LangToggle lang={i18n.language} onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')} />
          <Button variant="ghost" size="sm" onClick={() => { logout(); window.location.href = import.meta.env.BASE_URL }}>
            {t('nav.logout')}
          </Button>
        </div>
      }
    >
      <div style={{ maxWidth: 680, margin: '0 auto', padding: '48px 24px' }}>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, marginBottom: 32 }}>
          {t('profile.title')}
        </h1>

        <div style={{ display: 'flex', borderBottom: '1px solid var(--border)', marginBottom: 36 }}>
          <button style={tabStyle(activeTab === 'profile')} onClick={() => { setActiveTab('profile'); clearMessages() }}>
            {t('profile.tab.profile')}
          </button>
          <button style={tabStyle(activeTab === 'security')} onClick={() => { setActiveTab('security'); clearMessages() }}>
            {t('profile.tab.security')}
          </button>
        </div>

        {successMsg && <div role="status" style={{ padding: '12px 16px', borderRadius: 6, background: '#dcfce7', color: '#15803d', marginBottom: 24, fontSize: 14 }}>{successMsg}</div>}
        {errorMsg   && <div role="alert"  style={{ padding: '12px 16px', borderRadius: 6, background: '#fee2e2', color: '#dc2626', marginBottom: 24, fontSize: 14 }}>{errorMsg}</div>}

        {activeTab === 'profile' && (
          <form onSubmit={handleSaveProfile} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <div style={rowStyle}>
              <div style={fieldStyle}>
                <label htmlFor="firstName" style={labelStyle}>{t('profile.firstName')}</label>
                <input id="firstName" style={inputStyle} value={firstName} onChange={e => setFirstName(e.target.value)} required />
              </div>
              <div style={fieldStyle}>
                <label htmlFor="lastName" style={labelStyle}>{t('profile.lastName')}</label>
                <input id="lastName" style={inputStyle} value={lastName} onChange={e => setLastName(e.target.value)} required />
              </div>
            </div>

            <div style={fieldStyle}>
              <label htmlFor="email" style={labelStyle}>{t('profile.email')}</label>
              <input id="email" style={readonlyStyle} value={profile?.email ?? ''} readOnly aria-readonly="true" />
            </div>

            <div style={fieldStyle}>
              <label htmlFor="phone" style={labelStyle}>{t('profile.phone')}</label>
              <input id="phone" style={inputStyle} value={phone} onChange={e => setPhone(e.target.value)} />
            </div>

            <div style={fieldStyle}>
              <label htmlFor="language" style={labelStyle}>{t('profile.language')}</label>
              <select id="language" style={inputStyle} value={language} onChange={e => setLanguage(e.target.value as 'FR' | 'EN')}>
                <option value="FR">{t('profile.language.fr')}</option>
                <option value="EN">{t('profile.language.en')}</option>
              </select>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '4px 0' }} />
            <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0 }}>{t('profile.logo.title')}</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              {logoUrl && (
                <img src={logoUrl} alt={t('profile.logo.preview')} style={{ height: 64, width: 'auto', objectFit: 'contain', borderRadius: 6, border: '1px solid var(--border)' }} />
              )}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <label style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                  <Button type="button" variant="secondary" size="sm" disabled={uploadingLogo} onClick={() => document.getElementById('logo-upload')?.click()}>
                    {uploadingLogo ? t('profile.logo.uploading') : logoUrl ? t('profile.logo.change') : t('profile.logo.add')}
                  </Button>
                  <input id="logo-upload" type="file" accept="image/jpeg,image/png,image/webp" style={{ display: 'none' }} onChange={handleLogoUpload} />
                </label>
                {logoUrl && (
                  <Button type="button" variant="ghost" size="sm" onClick={handleRemoveLogo}>
                    {t('profile.logo.remove')}
                  </Button>
                )}
              </div>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '4px 0' }} />
            <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0 }}>{t('profile.banner.title')}</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              {bannerUrl && (
                <img src={bannerUrl} alt={t('profile.banner.preview')} style={{ height: 64, width: 'auto', maxWidth: 200, objectFit: 'cover', borderRadius: 6, border: '1px solid var(--border)' }} />
              )}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <label style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                  <Button type="button" variant="secondary" size="sm" disabled={uploadingBanner} onClick={() => document.getElementById('banner-upload')?.click()}>
                    {uploadingBanner ? t('profile.banner.uploading') : bannerUrl ? t('profile.banner.change') : t('profile.banner.add')}
                  </Button>
                  <input id="banner-upload" type="file" accept="image/jpeg,image/png,image/webp" style={{ display: 'none' }} onChange={handleBannerUpload} />
                </label>
                {bannerUrl && (
                  <Button type="button" variant="ghost" size="sm" onClick={handleRemoveBanner}>
                    {t('profile.banner.remove')}
                  </Button>
                )}
              </div>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '4px 0' }} />
            <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0 }}>{t('profile.theme.title')}</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <input
                  id="accentColorPicker"
                  type="color"
                  value={accentColor}
                  onChange={e => setAccentColor(e.target.value)}
                  style={{ width: 44, height: 36, padding: 2, border: '1px solid var(--border)', borderRadius: 6, cursor: 'pointer', background: 'none' }}
                />
                <div style={fieldStyle}>
                  <label htmlFor="accentColorHex" style={labelStyle}>{t('profile.theme.accentColor')}</label>
                  <input
                    id="accentColorHex"
                    style={{ ...inputStyle, width: 110 }}
                    value={accentColor}
                    onChange={e => { if (/^#[0-9a-fA-F]{0,6}$/.test(e.target.value)) setAccentColor(e.target.value) }}
                    placeholder="#4e8b82"
                    maxLength={7}
                  />
                </div>
              </div>
              {accentColor !== '#4e8b82' && (
                <Button type="button" variant="ghost" size="sm" onClick={() => setAccentColor('#4e8b82')}>
                  {t('profile.theme.reset')}
                </Button>
              )}
            </div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', margin: 0 }}>{t('profile.theme.hint')}</p>

            <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '4px 0' }} />
            <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0 }}>{t('profile.address.title')}</h2>

            <div style={fieldStyle}>
              <label htmlFor="addressLine" style={labelStyle}>{t('profile.address.line')}</label>
              <input id="addressLine" style={inputStyle} value={addressLine} onChange={e => setAddressLine(e.target.value)} />
            </div>

            <div style={rowStyle}>
              <div style={fieldStyle}>
                <label htmlFor="city" style={labelStyle}>{t('profile.address.city')}</label>
                <input id="city" style={inputStyle} value={city} onChange={e => setCity(e.target.value)} />
              </div>
              <div style={fieldStyle}>
                <label htmlFor="postalCode" style={labelStyle}>{t('profile.address.postalCode')}</label>
                <input id="postalCode" style={inputStyle} value={postalCode} onChange={e => setPostalCode(e.target.value)} />
              </div>
            </div>

            <div style={fieldStyle}>
              <label htmlFor="countryCode" style={labelStyle}>{t('profile.address.country')}</label>
              <input id="countryCode" style={inputStyle} value={countryCode} onChange={e => setCountryCode(e.target.value.toUpperCase().slice(0, 2))} maxLength={2} placeholder="FR" />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 8 }}>
              <Button type="button" variant="ghost" onClick={() => { window.location.href = import.meta.env.BASE_URL }}>{t('profile.cancel')}</Button>
              <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.save')}</Button>
            </div>
          </form>
        )}

        {activeTab === 'security' && (
          <form onSubmit={handleSavePassword} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <div style={fieldStyle}>
              <label htmlFor="currentPassword" style={labelStyle}>{t('profile.security.currentPassword')}</label>
              <input id="currentPassword" type="password" style={inputStyle} value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required />
            </div>
            <div style={fieldStyle}>
              <label htmlFor="newPassword" style={labelStyle}>{t('profile.security.newPassword')}</label>
              <input id="newPassword" type="password" style={inputStyle} value={newPassword} onChange={e => setNewPassword(e.target.value)} required minLength={8} />
            </div>
            <div style={fieldStyle}>
              <label htmlFor="confirmPassword" style={labelStyle}>{t('profile.security.confirmPassword')}</label>
              <input id="confirmPassword" type="password" style={inputStyle} value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 8 }}>
              <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.security.submit')}</Button>
            </div>
          </form>
        )}
      </div>
    </AppShell>
  )
}
