import { useState } from 'react'
import { transfer } from '../api/api'

export default function TransferModal({ userId, onClose, onSuccess }) {
  const [form, setForm] = useState({ receiverUserId: '', amount: '', description: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (Number(form.receiverUserId) === userId) return setError("You can't transfer to yourself")
    if (!form.amount || Number(form.amount) < 1) return setError('Enter a valid amount')
    setLoading(true)
    setError('')
    try {
      const res = await transfer({
        senderUserId: userId,
        receiverUserId: Number(form.receiverUserId),
        amount: Number(form.amount),
        description: form.description || 'Transfer',
      })
      if (res.data.status === 'FAILED') {
        setError(res.data.failureReason || 'Transfer failed')
      } else {
        onSuccess()
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <button className="modal-close" onClick={onClose}>✕</button>
        <p className="modal-title">↗ Transfer Money</p>

        <form onSubmit={handleSubmit} className="form-stack">
          <div className="input-group">
            <label>Recipient User ID</label>
            <input
              type="number"
              placeholder="e.g. 3"
              value={form.receiverUserId}
              onChange={set('receiverUserId')}
              required
            />
            <span style={styles.hint}>Users: Simran=2, Preetinder=3, Allen=4</span>
          </div>

          <div className="input-group">
            <label>Amount (₹)</label>
            <input
              type="number"
              placeholder="0.00"
              value={form.amount}
              min="1"
              onChange={set('amount')}
              required
              style={{ fontSize: 20, fontFamily: 'Syne, sans-serif', fontWeight: 700 }}
            />
          </div>

          <div className="input-group">
            <label>Description (optional)</label>
            <input placeholder="e.g. Rent, Lunch, etc." value={form.description} onChange={set('description')} />
          </div>

          {error && <p style={styles.error}>{error}</p>}

          {form.amount && form.receiverUserId && (
            <div style={styles.summary}>
              <span>Sending</span>
              <span style={{ color: 'var(--accent2)', fontWeight: 700 }}>₹{Number(form.amount).toLocaleString()}</span>
              <span>to User #{form.receiverUserId}</span>
            </div>
          )}

          <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" style={{ flex: 2 }} disabled={loading}>
              {loading ? <span className="spin">↻</span> : 'Send Money ↗'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const styles = {
  hint: { fontSize: 11, color: 'var(--muted2)', marginTop: 2 },
  error: { fontSize: 13, color: 'var(--red)', background: '#ff537010', padding: '10px 14px', borderRadius: 8 },
  summary: {
    background: '#7c6dfa10', border: '1px solid #7c6dfa25',
    borderRadius: 10, padding: '12px 16px',
    display: 'flex', gap: 6, alignItems: 'center',
    fontSize: 14, color: 'var(--muted)',
  },
}
