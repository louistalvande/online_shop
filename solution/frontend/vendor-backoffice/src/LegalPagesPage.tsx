import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Button, Card } from '@workspace/theme'
import { getLegalPage, updateLegalPage } from './api/shopConfigApi'

const LEGAL_KEYS = [
  { key: 'legal_cgv',              labelKey: 'legal.cgv' },
  { key: 'legal_mentions_legales', labelKey: 'legal.mentions' },
  { key: 'legal_confidentialite',  labelKey: 'legal.privacy' },
  { key: 'legal_retour',           labelKey: 'legal.return' },
  { key: 'legal_apropos',          labelKey: 'legal.about' },
  { key: 'legal_reproduction',     labelKey: 'legal.reproduction' },
]

export default function LegalPagesPage() {
  const { t } = useTranslation()
  const [activeKey, setActiveKey] = useState(LEGAL_KEYS[0].key)
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    setSuccess(false)
    setError('')
    getLegalPage(activeKey)
      .then(p => setContent(p.content))
      .catch(() => setError(t('legal.loadError')))
      .finally(() => setLoading(false))
  }, [activeKey])

  async function handleSave() {
    setSaving(true)
    setSuccess(false)
    setError('')
    try {
      await updateLegalPage(activeKey, content)
      setSuccess(true)
    } catch {
      setError(t('legal.saveError'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div style={{ padding: '32px 24px', maxWidth: 900, margin: '0 auto' }}>
      <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 24, fontWeight: 700, marginBottom: 24 }}>
        {t('legal.pageTitle')}
      </h1>

      <div style={{ display: 'flex', gap: 24, alignItems: 'flex-start' }}>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: 4, minWidth: 200 }}>
          {LEGAL_KEYS.map(item => (
            <button
              key={item.key}
              onClick={() => setActiveKey(item.key)}
              style={{
                textAlign: 'left',
                padding: '8px 12px',
                borderRadius: 6,
                border: 'none',
                background: activeKey === item.key ? 'var(--accent)' : 'transparent',
                color: activeKey === item.key ? '#fff' : 'var(--text)',
                fontFamily: 'inherit',
                fontSize: 14,
                fontWeight: activeKey === item.key ? 600 : 400,
                cursor: 'pointer',
              }}
            >
              {t(item.labelKey)}
            </button>
          ))}
        </nav>

        <Card style={{ flex: 1, padding: 24 }}>
          <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>
            {t(LEGAL_KEYS.find(k => k.key === activeKey)!.labelKey)}
          </h2>

          {loading ? (
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>{t('legal.loading')}</p>
          ) : (
            <textarea
              value={content}
              onChange={e => { setContent(e.target.value); setSuccess(false) }}
              rows={20}
              style={{
                width: '100%',
                boxSizing: 'border-box',
                padding: '10px 12px',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius)',
                fontSize: 13,
                fontFamily: 'inherit',
                lineHeight: 1.6,
                resize: 'vertical',
                background: 'var(--surface)',
                color: 'var(--text)',
              }}
            />
          )}

          {error && <p style={{ color: '#b91c1c', fontSize: 13, marginTop: 8 }}>{error}</p>}
          {success && <p style={{ color: '#2e7d32', fontSize: 13, marginTop: 8 }}>{t('legal.saved')}</p>}

          <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="primary" disabled={saving || loading} onClick={handleSave}>
              {saving ? t('legal.saving') : t('legal.save')}
            </Button>
          </div>
        </Card>
      </div>
    </div>
  )
}
