import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import {
  getCampaignRecipientsCount,
  sendCampaign,
  type CampaignSentResponse,
} from './api/campaignApi'

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '8px 10px',
  borderRadius: 6,
  border: '1px solid #d1d5db',
  fontSize: 14,
  boxSizing: 'border-box',
}
const labelStyle: React.CSSProperties = {
  display: 'block',
  marginTop: 14,
  marginBottom: 4,
  fontWeight: 600,
  fontSize: 14,
}

/** Vendor back-office page for composing and sending marketing campaigns (US-MKTG-01). */
export default function CampaignsPage() {
  const { t } = useTranslation()
  const [recipientCount, setRecipientCount] = useState<number | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [subject, setSubject] = useState('')
  const [body, setBody] = useState('')
  const [sending, setSending] = useState(false)
  const [formError, setFormError] = useState('')
  const [result, setResult] = useState<CampaignSentResponse | null>(null)

  useEffect(() => {
    loadCount()
  }, [])

  async function loadCount() {
    setLoadError(false)
    try {
      const data = await getCampaignRecipientsCount()
      setRecipientCount(data.count)
    } catch {
      setLoadError(true)
    }
  }

  async function handleSend() {
    setFormError('')
    setResult(null)
    if (!subject.trim()) { setFormError(t('campaigns.error.subjectRequired')); return }
    if (!body.trim())    { setFormError(t('campaigns.error.bodyRequired'));    return }

    setSending(true)
    try {
      const sent = await sendCampaign({ subject: subject.trim(), body: body.trim() })
      setResult(sent)
      setSubject('')
      setBody('')
      setRecipientCount(null)
      await loadCount()
    } catch (err: unknown) {
      const code = err instanceof Error ? err.message : ''
      if (code === 'NO_CONSENTING_BUYERS') {
        setFormError(t('campaigns.error.noConsentingBuyers'))
      } else {
        setFormError(t('campaigns.error.generic'))
      }
    } finally {
      setSending(false)
    }
  }

  const canSend = recipientCount !== null && recipientCount > 0

  return (
    <div style={{ padding: '24px 32px', maxWidth: 640 }}>
      <h1 style={{ margin: '0 0 24px', fontSize: 22, fontWeight: 700 }}>{t('campaigns.title')}</h1>

      {loadError && (
        <p style={{ color: 'red' }}>
          {t('campaigns.error.load')}{' '}
          <button onClick={loadCount}>{t('campaigns.retry')}</button>
        </p>
      )}

      {!loadError && (
        <div style={{ background: '#f0f8f4', border: '1px solid #c3e6d8', borderRadius: 8, padding: '12px 16px', marginBottom: 24 }}>
          <strong>{t('campaigns.recipients.label')} </strong>
          {recipientCount === null
            ? t('campaigns.recipients.loading')
            : t('campaigns.recipients.count', { count: recipientCount })}
        </div>
      )}

      {result && (
        <div style={{ background: '#f0fdf4', border: '1px solid #86efac', borderRadius: 8, padding: '12px 16px', marginBottom: 24 }}>
          {t('campaigns.success', { count: result.recipientCount, sentAt: new Date(result.sentAt).toLocaleString() })}
        </div>
      )}

      <label htmlFor="campaign-subject" style={labelStyle}>{t('campaigns.form.subject')}</label>
      <input
        id="campaign-subject"
        style={inputStyle}
        type="text"
        maxLength={200}
        value={subject}
        onChange={e => setSubject(e.target.value)}
        placeholder={t('campaigns.form.subjectPlaceholder')}
        disabled={sending}
      />

      <label htmlFor="campaign-body" style={labelStyle}>{t('campaigns.form.body')}</label>
      <textarea
        id="campaign-body"
        style={{ ...inputStyle, height: 160, resize: 'vertical' }}
        value={body}
        onChange={e => setBody(e.target.value)}
        placeholder={t('campaigns.form.bodyPlaceholder')}
        disabled={sending}
      />

      {!canSend && !loadError && recipientCount !== null && (
        <p style={{ color: '#9ca3af', fontSize: 13, marginTop: 8 }}>{t('campaigns.error.noConsentingBuyers')}</p>
      )}

      {formError && <p style={{ color: 'red', marginTop: 8 }}>{formError}</p>}

      <div style={{ marginTop: 20 }}>
        <button
          className="btn btn-primary"
          onClick={handleSend}
          disabled={sending || !canSend}
        >
          {sending ? t('campaigns.form.sending') : t('campaigns.form.send')}
        </button>
      </div>
    </div>
  )
}
