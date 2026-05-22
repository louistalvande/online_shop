import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Card, Snackbar } from '@workspace/theme'
import { getMaintenanceStatus, setMaintenanceMode } from '../api/settingsApi'

export default function MaintenanceModeCard() {
  const { t } = useTranslation()
  const [active, setActive] = useState(false)
  const [loading, setLoading] = useState(false)
  const [snackbar, setSnackbar] = useState<string | null>(null)

  function showSnackbar(msg: string) {
    setSnackbar(msg)
    setTimeout(() => setSnackbar(null), 3000)
  }

  useEffect(() => {
    getMaintenanceStatus()
      .then(s => setActive(s.active))
      .catch(() => {})
  }, [])

  async function handleToggle() {
    setLoading(true)
    try {
      const updated = await setMaintenanceMode(!active)
      setActive(updated.active)
      showSnackbar(updated.active ? t('maintenance.snackbar.enabled') : t('maintenance.snackbar.disabled'))
    } catch {
      showSnackbar(t('snackbar.error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {snackbar && <Snackbar message={snackbar} onDismiss={() => setSnackbar(null)} />}
      <Card style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
          <div>
            <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 4 }}>
              {t('maintenance.title')}
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
              {active ? t('maintenance.description.active') : t('maintenance.description.inactive')}
            </div>
          </div>
          <button
            data-testid="maintenance-toggle"
            onClick={handleToggle}
            disabled={loading}
            style={{
              cursor: loading ? 'not-allowed' : 'pointer',
              padding: '8px 20px',
              borderRadius: 6,
              border: 'none',
              fontWeight: 600,
              fontSize: 13,
              background: active ? '#fee2e2' : '#dcfce7',
              color: active ? '#b91c1c' : '#15803d',
              opacity: loading ? 0.6 : 1,
              transition: 'background 0.2s',
            }}
          >
            {active ? t('maintenance.action.disable') : t('maintenance.action.enable')}
          </button>
        </div>
        <div style={{ marginTop: 12, fontSize: 12, padding: '8px 12px', borderRadius: 4, background: active ? '#fef2f2' : '#f0fdf4', color: active ? '#b91c1c' : '#15803d' }}>
          {t('maintenance.status.label')}:{' '}
          <strong>{active ? t('maintenance.status.active') : t('maintenance.status.inactive')}</strong>
        </div>
      </Card>
    </>
  )
}
