import { authedFetch } from './authApi'

/** Response for the recipients count endpoint. */
export interface CampaignRecipientsCount {
  count: number
}

/** Payload for sending a campaign. */
export interface SendCampaignPayload {
  subject: string
  body: string
}

/** Response after a campaign has been sent. */
export interface CampaignSentResponse {
  id: string
  recipientCount: number
  sentAt: string
}

const JSON_HEADERS = { 'Content-Type': 'application/json' }

/** Returns the number of active buyers who have consented to marketing emails. */
export async function getCampaignRecipientsCount(): Promise<CampaignRecipientsCount> {
  const res = await authedFetch('/api/vendor/campaigns/recipients/count')
  if (!res.ok) throw new Error('load_failed')
  return res.json()
}

/**
 * Sends a marketing campaign to all consenting buyers.
 * Throws with error code {@code NO_CONSENTING_BUYERS} if no buyer has opted in.
 */
export async function sendCampaign(payload: SendCampaignPayload): Promise<CampaignSentResponse> {
  const res = await authedFetch('/api/vendor/campaigns/send', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error(body.error ?? 'send_failed')
  }
  return res.json()
}
