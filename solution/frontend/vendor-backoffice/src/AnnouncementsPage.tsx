import { useState, useEffect, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import {
  type Announcement,
  type ContentType,
  type ImageOrientation,
  listAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  moveAnnouncementUp,
  moveAnnouncementDown,
  uploadImage,
} from './api/announcementApi'

/** Orientation badge colours */
const ORIENTATION_STYLE: Record<string, { background: string; color: string }> = {
  PORTRAIT:  { background: '#e8f4ff', color: '#1565c0' },
  LANDSCAPE: { background: '#fff3e0', color: '#e65100' },
}

function Badge({ orientation }: { orientation: ImageOrientation | null }) {
  const { t } = useTranslation()
  if (!orientation) return null
  const s = ORIENTATION_STYLE[orientation]
  return (
    <span style={{ ...s, fontSize: 11, fontWeight: 600, borderRadius: 4, padding: '2px 6px' }}>
      {t(`announcements.orientation.${orientation.toLowerCase()}`)}
    </span>
  )
}

interface FormState {
  contentType: ContentType
  textContent: string
  imageUrl: string
  imageOrientation: ImageOrientation | null
  redirectUrl: string
  sortOrder: number
  active: boolean
}

const EMPTY_FORM: FormState = {
  contentType: 'TEXT',
  textContent: '',
  imageUrl: '',
  imageOrientation: null,
  redirectUrl: '',
  sortOrder: 0,
  active: true,
}

interface AnnouncementsPageProps {
  /** When true, suppresses outer padding and the page title (for embedding inside another page). */
  embedded?: boolean
}

/** Vendor back-office page for managing scrolling announcements (US-ANN-01). */
export default function AnnouncementsPage({ embedded = false }: AnnouncementsPageProps) {
  const { t } = useTranslation()
  const [announcements, setAnnouncements] = useState<Announcement[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    load()
  }, [])

  async function load() {
    setLoading(true)
    setLoadError(false)
    try {
      setAnnouncements(await listAnnouncements())
    } catch {
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setEditingId(null)
    setForm(EMPTY_FORM)
    setFormError('')
    setShowForm(true)
  }

  function openEdit(a: Announcement) {
    setEditingId(a.id)
    setForm({
      contentType: a.contentType,
      textContent: a.textContent ?? '',
      imageUrl: a.imageUrl ?? '',
      imageOrientation: a.imageOrientation,
      redirectUrl: a.redirectUrl ?? '',
      sortOrder: a.sortOrder,
      active: a.active,
    })
    setFormError('')
    setShowForm(true)
  }

  function cancelForm() {
    setShowForm(false)
    setEditingId(null)
  }

  async function handleImageFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    setFormError('')
    try {
      const result = await uploadImage(file)
      setForm(f => ({ ...f, imageUrl: result.imageUrl, imageOrientation: result.imageOrientation }))
    } catch {
      setFormError(t('announcements.error.upload'))
    } finally {
      setUploading(false)
    }
  }

  async function handleSave() {
    setFormError('')
    const needsText  = form.contentType === 'TEXT'  || form.contentType === 'BOTH'
    const needsImage = form.contentType === 'IMAGE' || form.contentType === 'BOTH'
    if (needsText  && !form.textContent.trim()) { setFormError(t('announcements.error.textRequired'));  return }
    if (needsImage && !form.imageUrl)           { setFormError(t('announcements.error.imageRequired')); return }

    setSaving(true)
    try {
      const payload = {
        contentType:      form.contentType,
        textContent:      form.textContent || null,
        imageUrl:         form.imageUrl    || null,
        imageOrientation: form.imageOrientation,
        redirectUrl:      form.redirectUrl || null,
        sortOrder:        form.sortOrder,
        active:           form.active,
      }
      if (editingId) {
        await updateAnnouncement(editingId, payload)
      } else {
        await createAnnouncement(payload)
      }
      setShowForm(false)
      await load()
    } catch {
      setFormError(t('announcements.error.save'))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm(t('announcements.deleteConfirm'))) return
    try {
      await deleteAnnouncement(id)
      setAnnouncements(prev => prev.filter(a => a.id !== id))
    } catch {
      alert(t('announcements.error.delete'))
    }
  }

  async function handleMove(id: string, dir: 'up' | 'down') {
    try {
      if (dir === 'up') await moveAnnouncementUp(id); else await moveAnnouncementDown(id)
      setAnnouncements(await listAnnouncements())
    } catch {
      /* ignore reorder error */
    }
  }

  // --- render ---

  const wantsImage = form.contentType === 'IMAGE' || form.contentType === 'BOTH'
  const wantsText  = form.contentType === 'TEXT'  || form.contentType === 'BOTH'

  return (
    <div style={{ padding: embedded ? 0 : '24px 32px' }}>
      {embedded ? (
        <div style={{ marginBottom: 16 }}>
          <button className="btn btn-primary" onClick={openCreate}>{t('announcements.add')}</button>
        </div>
      ) : (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h1 style={{ margin: 0, fontSize: 22, fontWeight: 700 }}>{t('announcements.title')}</h1>
          <button className="btn btn-primary" onClick={openCreate}>{t('announcements.add')}</button>
        </div>
      )}

      {loading && <p>{t('announcements.loading')}</p>}
      {loadError && <p style={{ color: 'red' }}>{t('announcements.error.load')} <button onClick={load}>{t('announcements.retry')}</button></p>}

      {!loading && !loadError && announcements.length === 0 && (
        <p style={{ color: '#888' }}>{t('announcements.empty')}</p>
      )}

      {!loading && announcements.length > 0 && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #e5e7eb' }}>
              <Th>{t('announcements.col.order')}</Th>
              <Th>{t('announcements.col.type')}</Th>
              <Th>{t('announcements.col.content')}</Th>
              <Th>{t('announcements.col.orientation')}</Th>
              <Th>{t('announcements.col.active')}</Th>
              <Th>{t('announcements.col.actions')}</Th>
            </tr>
          </thead>
          <tbody>
            {announcements.map((a, idx) => (
              <tr key={a.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                <Td>
                  <span style={{ fontWeight: 600, marginRight: 8 }}>{a.sortOrder}</span>
                  <button className="btn btn-sm btn-secondary" disabled={idx === 0} onClick={() => handleMove(a.id, 'up')} style={{ marginRight: 4 }}>↑</button>
                  <button className="btn btn-sm btn-secondary" disabled={idx === announcements.length - 1} onClick={() => handleMove(a.id, 'down')}>↓</button>
                </Td>
                <Td>{t(`announcements.type.${a.contentType.toLowerCase()}`)}</Td>
                <Td>
                  {a.textContent && <span style={{ marginRight: 8 }}>{a.textContent.slice(0, 60)}{a.textContent.length > 60 ? '…' : ''}</span>}
                  {a.imageUrl && <img src={a.imageUrl} alt="" style={{ height: 40, width: 'auto', verticalAlign: 'middle', borderRadius: 4 }} />}
                </Td>
                <Td><Badge orientation={a.imageOrientation} /></Td>
                <Td>
                  <span style={{ color: a.active ? '#16a34a' : '#6b7280', fontWeight: 600 }}>
                    {a.active ? t('announcements.active.yes') : t('announcements.active.no')}
                  </span>
                </Td>
                <Td>
                  <button className="btn btn-sm btn-secondary" onClick={() => openEdit(a)} style={{ marginRight: 8 }}>{t('announcements.edit')}</button>
                  <button className="btn btn-sm btn-danger" onClick={() => handleDelete(a.id)}>{t('announcements.delete')}</button>
                </Td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showForm && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: 8, padding: 32, width: 520, maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 8px 32px rgba(0,0,0,.2)' }}>
            <h2 style={{ marginTop: 0 }}>{editingId ? t('announcements.form.titleEdit') : t('announcements.form.titleCreate')}</h2>

            <label htmlFor="ann-content-type" style={labelStyle}>{t('announcements.form.type')}</label>
            <select id="ann-content-type" style={inputStyle} value={form.contentType} onChange={e => setForm(f => ({ ...f, contentType: e.target.value as ContentType }))}>
              <option value="TEXT">{t('announcements.type.text')}</option>
              <option value="IMAGE">{t('announcements.type.image')}</option>
              <option value="BOTH">{t('announcements.type.both')}</option>
            </select>

            {wantsText && (
              <>
                <label style={labelStyle}>{t('announcements.form.text')}</label>
                <textarea
                  style={{ ...inputStyle, height: 80, resize: 'vertical' }}
                  maxLength={500}
                  value={form.textContent}
                  onChange={e => setForm(f => ({ ...f, textContent: e.target.value }))}
                  placeholder={t('announcements.form.textPlaceholder')}
                />
              </>
            )}

            {wantsImage && (
              <>
                <label style={labelStyle}>{t('announcements.form.image')}</label>
                <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" onChange={handleImageFile} disabled={uploading} />
                {uploading && <p style={{ color: '#666', fontSize: 13 }}>{t('announcements.uploading')}</p>}
                {form.imageUrl && (
                  <div style={{ marginTop: 8, display: 'flex', alignItems: 'center', gap: 12 }}>
                    <img src={form.imageUrl} alt="" style={{ height: 64, width: 'auto', borderRadius: 4, border: '1px solid #e5e7eb' }} />
                    <Badge orientation={form.imageOrientation} />
                  </div>
                )}
              </>
            )}

            <label style={labelStyle}>{t('announcements.form.redirectUrl')}</label>
            <input style={inputStyle} type="url" value={form.redirectUrl} onChange={e => setForm(f => ({ ...f, redirectUrl: e.target.value }))} placeholder="https://…" />

            <label style={labelStyle}>{t('announcements.form.sortOrder')}</label>
            <input style={inputStyle} type="number" min={0} value={form.sortOrder} onChange={e => setForm(f => ({ ...f, sortOrder: parseInt(e.target.value, 10) || 0 }))} />

            <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 12, cursor: 'pointer' }}>
              <input type="checkbox" checked={form.active} onChange={e => setForm(f => ({ ...f, active: e.target.checked }))} />
              {t('announcements.form.active')}
            </label>

            {formError && <p style={{ color: 'red', marginTop: 12 }}>{formError}</p>}

            <div style={{ marginTop: 20, display: 'flex', gap: 12 }}>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving || uploading}>
                {saving ? t('announcements.form.saving') : t('announcements.form.save')}
              </button>
              <button className="btn btn-secondary" onClick={cancelForm} disabled={saving}>{t('announcements.form.cancel')}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const labelStyle: React.CSSProperties = { display: 'block', marginTop: 14, marginBottom: 4, fontWeight: 600, fontSize: 14 }
const inputStyle: React.CSSProperties  = { width: '100%', padding: '8px 10px', borderRadius: 6, border: '1px solid #d1d5db', fontSize: 14, boxSizing: 'border-box' }

function Th({ children }: { children: ReactNode }) {
  return <th style={{ textAlign: 'left', padding: '8px 12px', fontSize: 13, fontWeight: 600, color: '#6b7280' }}>{children}</th>
}
function Td({ children }: { children: ReactNode }) {
  return <td style={{ padding: '10px 12px', verticalAlign: 'middle' }}>{children}</td>
}
