import { useTranslation } from 'react-i18next'

export default function MaintenancePage() {
  const { t } = useTranslation()

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      background: '#fafafa',
      padding: '24px',
      textAlign: 'center',
    }}>
      <div style={{ fontSize: 48, marginBottom: 16 }}>🔧</div>
      <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: 28, fontWeight: 700, marginBottom: 12 }}>
        {t('maintenance.title')}
      </h1>
      <p style={{ fontSize: 15, color: 'var(--text-muted)', maxWidth: 480 }}>
        {t('maintenance.message')}
      </p>
    </div>
  )
}
