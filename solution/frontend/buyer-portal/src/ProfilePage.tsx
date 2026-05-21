import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, LangToggle } from '@workspace/theme'
import { getSession, logout } from './api/authApi'
import { getProfile, updateProfile, type ProfileData } from './api/profileApi'

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

  useEffect(() => {
    if (!session) { window.location.href = '/login'; return }
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

  async function handleSaveProfile(e: React.FormEvent) {
    e.preventDefault()
    clearMessages()
    setSaving(true)
    try {
      const updated = await updateProfile({ firstName, lastName, phone: phone || undefined, addressLine: addressLine || undefined, city: city || undefined, postalCode: postalCode || undefined, countryCode: countryCode || undefined, language })
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

  if (loadError) {
    return (
      <div className="profile-error-state">
        <p className="profile-error-text">{t('profile.error.load')}</p>
        <Button onClick={() => window.location.reload()}>{t('profile.retry')}</Button>
      </div>
    )
  }

  return (
    <AppShell
      appName={t('app.name')}
      navLinks={[
        { label: t('nav.home'), href: '/' },
        { label: t('nav.catalog'), href: '/#catalogue' },
      ]}
      actions={
        <div className="header-actions">
          <LangToggle lang={i18n.language} onToggle={() => i18n.changeLanguage(i18n.language === 'fr' ? 'en' : 'fr')} />
          <Button variant="ghost" size="sm" onClick={() => { logout(); window.location.href = '/' }}>
            {t('nav.logout')}
          </Button>
        </div>
      }
    >
      <div className="profile-page">
        <h1 className="profile-title">{t('profile.title')}</h1>

        <div className="profile-tabs">
          <button
            className={`profile-tab${activeTab === 'profile' ? ' profile-tab--active' : ''}`}
            onClick={() => { setActiveTab('profile'); clearMessages() }}
          >
            {t('profile.tab.profile')}
          </button>
          <button
            className={`profile-tab${activeTab === 'security' ? ' profile-tab--active' : ''}`}
            onClick={() => { setActiveTab('security'); clearMessages() }}
          >
            {t('profile.tab.security')}
          </button>
        </div>

        {successMsg && <div role="status" className="profile-alert-success">{successMsg}</div>}
        {errorMsg   && <div role="alert"  className="profile-alert-error">{errorMsg}</div>}

        {activeTab === 'profile' && (
          <form onSubmit={handleSaveProfile} className="profile-form">
            <div className="profile-row">
              <div className="profile-field">
                <label htmlFor="firstName" className="profile-label">{t('profile.firstName')}</label>
                <input id="firstName" className="profile-input" value={firstName} onChange={e => setFirstName(e.target.value)} required />
              </div>
              <div className="profile-field">
                <label htmlFor="lastName" className="profile-label">{t('profile.lastName')}</label>
                <input id="lastName" className="profile-input" value={lastName} onChange={e => setLastName(e.target.value)} required />
              </div>
            </div>

            <div className="profile-field">
              <label htmlFor="email" className="profile-label">{t('profile.email')}</label>
              <input id="email" className="profile-input profile-input--readonly" value={profile?.email ?? ''} readOnly aria-readonly="true" />
            </div>

            <div className="profile-field">
              <label htmlFor="phone" className="profile-label">{t('profile.phone')}</label>
              <input id="phone" className="profile-input" value={phone} onChange={e => setPhone(e.target.value)} />
            </div>

            <div className="profile-field">
              <label htmlFor="language" className="profile-label">{t('profile.language')}</label>
              <select id="language" className="profile-input" value={language} onChange={e => setLanguage(e.target.value as 'FR' | 'EN')}>
                <option value="FR">{t('profile.language.fr')}</option>
                <option value="EN">{t('profile.language.en')}</option>
              </select>
            </div>

            <hr className="profile-divider" />
            <h2 className="profile-section-title">{t('profile.address.title')}</h2>

            <div className="profile-field">
              <label htmlFor="addressLine" className="profile-label">{t('profile.address.line')}</label>
              <input id="addressLine" className="profile-input" value={addressLine} onChange={e => setAddressLine(e.target.value)} />
            </div>

            <div className="profile-row">
              <div className="profile-field">
                <label htmlFor="city" className="profile-label">{t('profile.address.city')}</label>
                <input id="city" className="profile-input" value={city} onChange={e => setCity(e.target.value)} />
              </div>
              <div className="profile-field">
                <label htmlFor="postalCode" className="profile-label">{t('profile.address.postalCode')}</label>
                <input id="postalCode" className="profile-input" value={postalCode} onChange={e => setPostalCode(e.target.value)} />
              </div>
            </div>

            <div className="profile-field">
              <label htmlFor="countryCode" className="profile-label">{t('profile.address.country')}</label>
              <input id="countryCode" className="profile-input" value={countryCode} onChange={e => setCountryCode(e.target.value.toUpperCase().slice(0, 2))} maxLength={2} placeholder="FR" />
            </div>

            <div className="profile-actions">
              <Button type="button" variant="ghost" onClick={() => window.location.href = '/'}>{t('profile.cancel')}</Button>
              <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.save')}</Button>
            </div>
          </form>
        )}

        {activeTab === 'security' && (
          <form onSubmit={handleSavePassword} className="profile-form">
            <div className="profile-field">
              <label htmlFor="currentPassword" className="profile-label">{t('profile.security.currentPassword')}</label>
              <input id="currentPassword" type="password" className="profile-input" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required />
            </div>
            <div className="profile-field">
              <label htmlFor="newPassword" className="profile-label">{t('profile.security.newPassword')}</label>
              <input id="newPassword" type="password" className="profile-input" value={newPassword} onChange={e => setNewPassword(e.target.value)} required minLength={8} />
            </div>
            <div className="profile-field">
              <label htmlFor="confirmPassword" className="profile-label">{t('profile.security.confirmPassword')}</label>
              <input id="confirmPassword" type="password" className="profile-input" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
            </div>

            <div className="profile-actions">
              <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.security.submit')}</Button>
            </div>
          </form>
        )}
      </div>
    </AppShell>
  )
}
