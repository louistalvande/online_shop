import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { createCarrier } from '../api/carrierApi'
import { listCountries, type CountryResponse } from '../api/countryApi'

interface Props {
  onClose: () => void
  onSuccess: () => void
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 480, maxWidth: 560, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
  maxHeight: '90vh', overflowY: 'auto',
}

const fieldStyle: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: 4, marginBottom: 16,
}

const labelStyle: React.CSSProperties = {
  fontSize: 12, fontWeight: 600, color: 'var(--text-muted)',
}

const inputStyle: React.CSSProperties = {
  padding: '8px 12px', borderRadius: 4, border: '1px solid var(--border)',
  fontSize: 14, background: 'var(--surface)', color: 'var(--text)', width: '100%',
  boxSizing: 'border-box',
}

const errorBox: React.CSSProperties = {
  background: '#fef2f2', border: '1px solid #fca5a5',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#b91c1c', marginBottom: 16,
}

export default function CarrierFormModal({ onClose, onSuccess }: Props) {
  const { t, i18n } = useTranslation()
  const [name, setName] = useState('')
  const [trackingUrl, setTrackingUrl] = useState('')
  const [countries, setCountries] = useState<CountryResponse[]>([])
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingCountries, setLoadingCountries] = useState(true)

  useEffect(() => {
    listCountries()
      .then(list => {
        setCountries([...list].sort((a, b) => {
          const na = i18n.language === 'fr' ? a.nameFr : a.nameEn
          const nb = i18n.language === 'fr' ? b.nameFr : b.nameEn
          return na.localeCompare(nb)
        }))
      })
      .catch(() => setError(t('carrierModal.error.generic')))
      .finally(() => setLoadingCountries(false))
  }, [])

  function toggleCountry(code: string) {
    setSelected(prev => {
      const next = new Set(prev)
      next.has(code) ? next.delete(code) : next.add(code)
      return next
    })
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (selected.size === 0) { setError(t('carrierModal.error.noCountry')); return }
    setError(null)
    setLoading(true)
    try {
      await createCarrier({ name, trackingUrl, supportedCountries: [...selected] })
      onSuccess()
    } catch {
      setError(t('carrierModal.error.generic'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 24 }}>
          {t('carrierModal.title.create')}
        </h2>

        {error && <div style={errorBox}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={fieldStyle}>
            <span style={labelStyle}>{t('carrierModal.name')}</span>
            <input style={inputStyle} required value={name} onChange={e => setName(e.target.value)} />
          </div>

          <div style={fieldStyle}>
            <span style={labelStyle}>{t('carrierModal.trackingUrl')}</span>
            <input style={inputStyle} required type="url" value={trackingUrl} onChange={e => setTrackingUrl(e.target.value)} />
          </div>

          <div style={fieldStyle}>
            <span style={labelStyle}>{t('carrierModal.countries')}</span>
            {loadingCountries ? (
              <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>…</span>
            ) : (
              <div style={{
                display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '6px 16px',
                padding: 12, border: '1px solid var(--border)', borderRadius: 4,
                maxHeight: 220, overflowY: 'auto',
              }}>
                {countries.map(c => (
                  <label key={c.code} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, cursor: 'pointer' }}>
                    <input type="checkbox" checked={selected.has(c.code)} onChange={() => toggleCountry(c.code)} />
                    {i18n.language === 'fr' ? c.nameFr : c.nameEn}
                  </label>
                ))}
              </div>
            )}
          </div>

          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 8 }}>
            <Button type="button" variant="ghost" size="sm" onClick={onClose}>
              {t('accountModal.cancel')}
            </Button>
            <Button type="submit" size="sm" disabled={loading}>
              {loading ? '…' : t('carrierModal.submit')}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
