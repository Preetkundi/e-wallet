import { useState } from 'react'
import { merchantPayment } from '../api/api'

const MERCHANTS = [
  { id: 4, name: 'Allen Store', icon: '🏪' },
  { id: 4, name: 'Starbucks', icon: '☕' },
  { id: 4, name: 'Amazon', icon: '📦' },
]

export default function PaymentModal({ userId, onClose, onSuccess }) {
  const [form, setForm] = useState({ merchantId: '', merchantName: '', amount: '', description: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const selectMerchant = (m) => setForm({ ...form, merchantId: String(m.id), merchantName: m.name })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.amount || Number(form.amount) < 1) return setError('Enter a valid amount')
    setLoading(true)
    setError('')
    try {
      const res = await merchantPayment({
        userId,
        merchantId: Number(form.merchantId),
        amount: Number(form.amount),
        merchantName: form.merchantName,
        description: form.description || `Payment to ${form.merchantName}`,
      })
      if (res.data.status === 'FAILED') {
        setError(res.data.failureReason || 'Payment failed')
      } else {
        onSuccess()
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Payment failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <button className="modal-close" onClick={onClose}>✕</button>
        <p className="modal-title">🏪 Pay Merchant</p>

        <form onSubmit={handleSubmit} className="form-stack">
          <div className="input-group">
            <label>Select merchant</label>
            <div style={styles.merchantGrid}>
              {MERCHANTS.map((m, i) => (
                <button
                  key={i}
                  type="button"
                  className="btn btn-ghost"
                  style={{ ...styles.merchantBtn, ...(form.merchantName === m.name ? styles.merchantActive : {}) }}
                  onClick={() => selectMerchant(m)}
                >
                  <span style={{ fontSize: 20 }}>{m.icon}</span>
                  <span style={{ fontSize: 12 }}>{m.name}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="input-group">
            <label>Merchant ID</label>
            <input type="number" placeholder="e.g. 4" value={form.merchantId} onChange={set('merchantId')} required />
          </div>

          <div className="input-group">
            <label>Merchant Name</label>
            <input placeholder="e.g. Starbucks" value={form.merchantName} onChange={set('merchantName')} required />
          </div>

          <div className="input-group">
            <label>Amount (₹)</label>
            <input
              type="number" placeholder="0.00" min="1"
              value={form.amount} onChange={set('amount')} required
              style={{ fontSize: 20, fontFamily: 'Syne, sans-serif', fontWeight: 700 }}
            />
          </div>

          <div className="input-group">
            <label>Description (optional)</label>
            <input placeholder="e.g. Coffee, Groceries" value={form.description} onChange={set('description')} />
          </div>

          {error && <p style={styles.error}>{error}</p>}

          <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" style={{ flex: 2, background: 'var(--amber)', boxShadow: '0 4px 16px #ffb84d30' }} disabled={loading}>
              {loading ? <span className="spin">↻</span> : 'Pay Now'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const styles = {
  merchantGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 },
  merchantBtn: { flexDirection: 'column', padding: '12px 8px', gap: 6, fontSize: 12 },
  merchantActive: { borderColor: 'var(--amber)', background: '#ffb84d15', color: 'var(--amber)' },
  error: { fontSize: 13, color: 'var(--red)', background: '#ff537010', padding: '10px 14px', borderRadius: 8 },
}
