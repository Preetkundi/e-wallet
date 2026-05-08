import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register, createWallet } from '../api/api'
import { useAuth } from '../context/AuthContext'
import Toast from '../components/Toast'

export default function Register() {
  const navigate = useNavigate()
  const { loginUser } = useAuth()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', phone: '' })
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'error') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await register(form)
      const userData = res.data
      // Auto-create wallet after registration
      try { await createWallet(userData.userId) } catch (_) {}
      loginUser(userData)
      showToast('Account created successfully!', 'success')
      setTimeout(() => navigate('/dashboard'), 800)
    } catch (err) {
      showToast(err.response?.data?.message || 'Registration failed. Try again.')
    } finally {
      setLoading(false)
    }
  }

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <div style={styles.logo}>
          <div style={styles.logoIcon}>₹</div>
          <span style={styles.logoText}>Sterling</span>
        </div>

        <h1 style={styles.title}>Create account</h1>
        <p style={styles.subtitle}>Join Sterling E-Wallet today</p>

        <form onSubmit={handleSubmit} className="form-stack">
          <div className="input-group">
            <label>Full name</label>
            <input placeholder="John Doe" value={form.fullName} onChange={set('fullName')} required />
          </div>
          <div className="input-group">
            <label>Email address</label>
            <input type="email" placeholder="you@example.com" value={form.email} onChange={set('email')} required />
          </div>
          <div className="input-group">
            <label>Phone number</label>
            <input placeholder="10-digit number" maxLength={10} value={form.phone} onChange={set('phone')} required />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input type="password" placeholder="Min 8 characters" value={form.password} onChange={set('password')} required />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
            style={{ marginTop: 8, padding: '14px' }}
          >
            {loading ? <span className="spin">↻</span> : 'Create Account'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" style={styles.link}>Sign in</Link>
        </p>
      </div>

      {toast && <Toast msg={toast.msg} type={toast.type} />}
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    padding: 20, position: 'relative', zIndex: 1,
  },
  container: {
    width: '100%', maxWidth: 420,
    background: 'var(--bg2)', border: '1px solid var(--border2)',
    borderRadius: 'var(--radius)', padding: '40px 36px',
    boxShadow: 'var(--shadow)', animation: 'slideUp 0.3s ease',
  },
  logo: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 32 },
  logoIcon: {
    width: 40, height: 40, background: 'var(--accent)', borderRadius: 12,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 20, fontWeight: 700, boxShadow: '0 4px 16px #7c6dfa40',
  },
  logoText: { fontFamily: 'Syne, sans-serif', fontSize: 22, fontWeight: 800 },
  title: { fontSize: 28, fontWeight: 800, marginBottom: 6 },
  subtitle: { fontSize: 14, color: 'var(--muted)', marginBottom: 28 },
  footer: { textAlign: 'center', fontSize: 13, color: 'var(--muted)', marginTop: 24 },
  link: { color: 'var(--accent2)', textDecoration: 'none', fontWeight: 500 },
}
