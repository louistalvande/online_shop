import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { AppShell, Button, LangToggle } from '@workspace/theme'
import { getSession, logout } from './api/authApi'
import {
  getProfile,
  updateProfile,
  listAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  type ProfileData,
  type DeliveryAddressData,
  type CreateDeliveryAddressPayload,
} from './api/profileApi'
import { listCountries, type CountryData } from './api/orderApi'

type Tab = 'profile' | 'addresses' | 'security'

interface AddressFormState {
  label: string
  addressLine: string
  city: string
  postalCode: string
  countryCode: string
  makeDefault: boolean
}

const EMPTY_FORM: AddressFormState = {
  label: '', addressLine: '', city: '', postalCode: '', countryCode: '', makeDefault: false,
}

export default function ProfilePage() {
  const { t, i18n } = useTranslation()
  const session = getSession()
  const locale = i18n.language

  const [activeTab, setActiveTab] = useState<Tab>('profile')
  const [profile, setProfile] = useState<ProfileData | null>(null)
  const [loadError, setLoadError] = useState(false)

  // Profile tab
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phone, setPhone] = useState('')
  const [language, setLanguage] = useState<'FR' | 'EN'>('FR')

  // Security tab
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  // Addresses tab
  const [addresses, setAddresses] = useState<DeliveryAddressData[]>([])
  const [countries, setCountries] = useState<CountryData[]>([])
  const [addrLoading, setAddrLoading] = useState(false)
  const [showAddrForm, setShowAddrForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [addrForm, setAddrForm] = useState<AddressFormState>(EMPTY_FORM)
  const [addrSaving, setAddrSaving] = useState(false)
  const [addrFormError, setAddrFormError] = useState('')

  // Shared UI state
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
        setLanguage(p.language)
      })
      .catch(() => setLoadError(true))
  }, [])

  useEffect(() => {
    if (activeTab !== 'addresses') return
    if (countries.length === 0) listCountries().then(setCountries).catch(() => {})
    loadAddresses()
  }, [activeTab])

  function loadAddresses() {
    setAddrLoading(true)
    listAddresses()
      .then(setAddresses)
      .catch(() => setErrorMsg(t('profile.addresses.error.load')))
      .finally(() => setAddrLoading(false))
  }

  function clearMessages() { setSuccessMsg(''); setErrorMsg('') }

  async function handleSaveProfile(e: React.FormEvent) {
    e.preventDefault()
    clearMessages()
    setSaving(true)
    try {
      const updated = await updateProfile({ firstName, lastName, phone: phone || undefined, language })
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
    if (newPassword !== confirmPassword) { setErrorMsg(t('profile.error.passwordMismatch')); return }
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

  function openAddForm() {
    setEditingId(null)
    setAddrForm(EMPTY_FORM)
    setAddrFormError('')
    setShowAddrForm(true)
  }

  function openEditForm(addr: DeliveryAddressData) {
    setEditingId(addr.id)
    setAddrForm({
      label: addr.label,
      addressLine: addr.addressLine,
      city: addr.city,
      postalCode: addr.postalCode,
      countryCode: addr.countryCode,
      makeDefault: addr.default,
    })
    setAddrFormError('')
    setShowAddrForm(true)
  }

  async function handleSaveAddress(e: React.FormEvent) {
    e.preventDefault()
    setAddrFormError('')
    setAddrSaving(true)
    try {
      const payload: CreateDeliveryAddressPayload = { ...addrForm }
      if (editingId) {
        const updated = await updateAddress(editingId, payload)
        setAddresses(prev => prev.map(a => a.id === editingId ? updated : (payload.makeDefault ? { ...a, default: false } : a)))
      } else {
        const created = await createAddress(payload)
        setAddresses(prev => {
          const cleared = payload.makeDefault ? prev.map(a => ({ ...a, default: false })) : prev
          return [...cleared, created]
        })
      }
      setShowAddrForm(false)
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      setAddrFormError(code === 'INVALID_COUNTRY' ? t('profile.addresses.error.invalidCountry') : t('profile.addresses.error.save'))
    } finally {
      setAddrSaving(false)
    }
  }

  async function handleDeleteAddress(id: string) {
    if (!window.confirm(t('profile.addresses.deleteConfirm'))) return
    try {
      await deleteAddress(id)
      setAddresses(prev => prev.filter(a => a.id !== id))
    } catch (err: unknown) {
      const code = (err as { code?: string }).code
      setErrorMsg(code === 'LAST_ACTIVE_ADDRESS' ? t('profile.addresses.error.lastActive') : t('profile.addresses.error.delete'))
    }
  }

  async function handleSetDefault(id: string) {
    try {
      await setDefaultAddress(id)
      setAddresses(prev => prev.map(a => ({ ...a, default: a.id === id })))
    } catch {
      setErrorMsg(t('profile.addresses.error.save'))
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
            className={`profile-tab${activeTab === 'addresses' ? ' profile-tab--active' : ''}`}
            onClick={() => { setActiveTab('addresses'); clearMessages() }}
          >
            {t('profile.tab.addresses')}
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

        {/* ─── PROFILE TAB ─── */}
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

            <div className="profile-actions">
              <Button type="button" variant="ghost" onClick={() => window.location.href = '/'}>{t('profile.cancel')}</Button>
              <Button type="submit" disabled={saving}>{saving ? t('profile.saving') : t('profile.save')}</Button>
            </div>
          </form>
        )}

        {/* ─── ADDRESSES TAB ─── */}
        {activeTab === 'addresses' && (
          <div className="profile-addresses">
            <h2 className="profile-section-title">{t('profile.addresses.title')}</h2>

            {addrLoading ? (
              <p className="profile-loading">{t('profile.loading')}</p>
            ) : addresses.length === 0 ? (
              <p className="profile-addresses-empty">{t('profile.addresses.empty')}</p>
            ) : (
              <table className="profile-addresses-table">
                <thead>
                  <tr>
                    <th>{t('profile.addresses.col.label')}</th>
                    <th>{t('profile.addresses.col.address')}</th>
                    <th>{t('profile.addresses.col.city')}</th>
                    <th>{t('profile.addresses.col.country')}</th>
                    <th>{t('profile.addresses.col.default')}</th>
                    <th>{t('profile.addresses.col.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {addresses.map(addr => (
                    <tr key={addr.id}>
                      <td>{addr.label}</td>
                      <td>{addr.addressLine}</td>
                      <td>{addr.city}</td>
                      <td>{addr.countryCode}</td>
                      <td className="profile-addresses-default">
                        {addr.default ? <span title={t('profile.addresses.default')}>★</span> : '☆'}
                      </td>
                      <td className="profile-addresses-actions">
                        <Button size="sm" variant="ghost" onClick={() => openEditForm(addr)}>
                          {t('profile.addresses.edit')}
                        </Button>
                        {!addr.default && (
                          <Button size="sm" variant="ghost" onClick={() => handleSetDefault(addr.id)}>
                            {t('profile.addresses.setDefault')}
                          </Button>
                        )}
                        <Button size="sm" variant="ghost" onClick={() => handleDeleteAddress(addr.id)}>
                          {t('profile.addresses.delete')}
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            <div className="profile-actions">
              <Button onClick={openAddForm}>{t('profile.addresses.add')}</Button>
            </div>

            {/* ─── ADDRESS FORM MODAL ─── */}
            {showAddrForm && (
              <div className="profile-modal-overlay" role="dialog" aria-modal="true">
                <div className="profile-modal">
                  <h2 className="profile-modal-title">
                    {editingId ? t('profile.addresses.form.title.edit') : t('profile.addresses.form.title.add')}
                  </h2>
                  <form onSubmit={handleSaveAddress}>
                    <div className="profile-field">
                      <label htmlFor="addrLabel" className="profile-label">{t('profile.addresses.form.label')}</label>
                      <input id="addrLabel" className="profile-input" value={addrForm.label} maxLength={100} required
                        onChange={e => setAddrForm(f => ({ ...f, label: e.target.value }))} />
                    </div>
                    <div className="profile-field">
                      <label htmlFor="addrLine" className="profile-label">{t('profile.addresses.form.line')}</label>
                      <input id="addrLine" className="profile-input" value={addrForm.addressLine} maxLength={255} required
                        onChange={e => setAddrForm(f => ({ ...f, addressLine: e.target.value }))} />
                    </div>
                    <div className="profile-row">
                      <div className="profile-field">
                        <label htmlFor="addrPostal" className="profile-label">{t('profile.addresses.form.postalCode')}</label>
                        <input id="addrPostal" className="profile-input" value={addrForm.postalCode} maxLength={20} required
                          onChange={e => setAddrForm(f => ({ ...f, postalCode: e.target.value }))} />
                      </div>
                      <div className="profile-field">
                        <label htmlFor="addrCity" className="profile-label">{t('profile.addresses.form.city')}</label>
                        <input id="addrCity" className="profile-input" value={addrForm.city} maxLength={100} required
                          onChange={e => setAddrForm(f => ({ ...f, city: e.target.value }))} />
                      </div>
                    </div>
                    <div className="profile-field">
                      <label htmlFor="addrCountry" className="profile-label">{t('profile.addresses.form.country')}</label>
                      <select id="addrCountry" className="profile-input" value={addrForm.countryCode} required
                        onChange={e => setAddrForm(f => ({ ...f, countryCode: e.target.value }))}>
                        <option value="">{t('checkout.address.countryPlaceholder')}</option>
                        {countries.map(c => (
                          <option key={c.code} value={c.code}>
                            {locale === 'en' ? c.nameEn : c.nameFr} ({c.code})
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="profile-field">
                      <label className="profile-checkbox-label">
                        <input type="checkbox" checked={addrForm.makeDefault}
                          onChange={e => setAddrForm(f => ({ ...f, makeDefault: e.target.checked }))} />
                        {' '}{t('profile.addresses.form.makeDefault')}
                      </label>
                    </div>

                    {addrFormError && <p className="profile-alert-error">{addrFormError}</p>}

                    <div className="profile-actions">
                      <Button type="button" variant="ghost" onClick={() => setShowAddrForm(false)}>
                        {t('profile.addresses.form.cancel')}
                      </Button>
                      <Button type="submit" disabled={addrSaving}>
                        {addrSaving ? t('profile.saving') : t('profile.addresses.form.save')}
                      </Button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ─── SECURITY TAB ─── */}
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
