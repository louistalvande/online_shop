import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@workspace/theme'
import { deleteCarrier, type CarrierResponse } from '../api/carrierApi'

interface Props {
  carrier: CarrierResponse
  onClose: () => void
  onSuccess: () => void
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
}

const modal: React.CSSProperties = {
  background: 'var(--surface)', borderRadius: 8, padding: 32,
  minWidth: 400, maxWidth: 480, width: '100%', boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
}

const errorBox: React.CSSProperties = {
  background: '#fef2f2', border: '1px solid #fca5a5',
  borderRadius: 4, padding: '8px 12px', fontSize: 13, color: '#b91c1c', marginBottom: 16,
}

export default function CarrierDeleteModal({ carrier, onClose, onSuccess }: Props) {
  const { t } = useTranslation()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleDelete() {
    setLoading(true)
    try {
      await deleteCarrier(carrier.id)
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
        <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: 20, fontWeight: 700, marginBottom: 16 }}>
          {t('carrierDeleteModal.title')}
        </h2>

        <p style={{ fontSize: 14, color: 'var(--text)', marginBottom: 24, lineHeight: 1.6 }}>
          {t('carrierDeleteModal.confirm', { name: carrier.name })}
        </p>

        {error && <div style={errorBox}>{error}</div>}

        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <Button type="button" variant="ghost" size="sm" onClick={onClose}>
            {t('accountModal.cancel')}
          </Button>
          <Button
            type="button"
            size="sm"
            disabled={loading}
            onClick={handleDelete}
            style={{ background: '#dc2626', color: '#fff', borderColor: '#dc2626' }}
          >
            {loading ? '…' : t('carrierDeleteModal.submit')}
          </Button>
        </div>
      </div>
    </div>
  )
}
