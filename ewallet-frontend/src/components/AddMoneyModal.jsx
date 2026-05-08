import { useState } from 'react'
import { addMoney } from '../api/api'

const QUICK_AMOUNTS = [500, 1000, 2000, 5000]

export default function AddMoneyModal({ userId, onClose, onSuccess }) {
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!amount || Number(amount) < 1) return setError('Enter a valid amount')
    setLoading(true)
    setError('')
    try {
      await addMoney(userId, Number(amount))
      onSuccess()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add money')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <button className="modal-close" onClick={onClose}>✕</button>
        <p className="modal-title">💰 Add Money</p>

        <form onSubmit={handleSubmit} className="form-stack">
          <div className="input-group">
            <label>Quick select</label>
            <div style={styles.quickGrid}>
              {QUICK_AMOUNTS.map(a => (
                <button
                  key={a}
                  type="button"
                  className="btn btn-ghost"
                  style={{ ...styles.quickBtn, ...(Number(amount) === a ? styles.quickBtnActive : {}) }}
                  onClick={() => setAmount(String(a))}
                >
                  ₹{a.toLocaleString()}
                </button>
              ))}
            </div>
          </div>

          <div className="input-group">
            <label>Or enter amount (₹)</label>
            <input
              type="number"
              placeholder="0.00"
              value={amount}
              min="1"
              onChange={e => setAmount(e.target.value)}
              style={{ fontSize: 22, padding: '14px 16px', fontFamily: 'Syne, sans-serif', fontWeight: 700 }}
            />
          </div>

          {error && <p style={styles.error}>{error}</p>}

          <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
            <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-green" style={{ flex: 2 }} disabled={loading}>
              {loading ? <span className="spin">↻</span> : '+ Add Money'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const styles = {
  quickGrid: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 },
  quickBtn: { padding: '10px 8px', fontSize: 13, justifyContent: 'center' },
  quickBtnActive: { borderColor: 'var(--accent)', color: 'var(--accent2)', background: '#7c6dfa15' },
  error: { fontSize: 13, color: 'var(--red)', background: '#ff537010', padding: '10px 14px', borderRadius: 8 },
}
