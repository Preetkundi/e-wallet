import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/api'
import { useAuth } from '../context/AuthContext'
import Toast from '../components/Toast'

export default function Login() {
  const navigate = useNavigate()
  const { loginUser } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
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
      const res = await login(form)
      loginUser(res.data)
      navigate('/dashboard')
    } catch (err) {
      showToast(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.container}>

        {/* Logo */}
        <div style={styles.logo}>
          <div style={styles.logoIcon}>₹</div>
          <span style={styles.logoText}>Sterling</span>
        </div>

        <h1 style={styles.title}>Welcome back</h1>
        <p style={styles.subtitle}>Sign in to your e-wallet</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div className="input-group">
            <label>Email address</label>
            <input
              type="email"
              placeholder="you@sterling.com"
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>

          <div className="input-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
            style={{ marginTop: 8, padding: '14px' }}
          >
            {loading ? <span className="spin">↻</span> : 'Sign In'}
          </button>
        </form>

        <div style={styles.divider}>
          <span>or</span>
        </div>

        {/* Demo credentials */}
        <div style={styles.demo}>
          <p style={styles.demoTitle}>Demo Accounts</p>
          {[
            { email: 'simran@sterling.com', role: 'User • ₹5,000' },
            { email: 'admin@sterling.com', role: 'Admin • ₹10,000' },
          ].map(d => (
            <button
              key={d.email}
              className="btn btn-ghost"
              style={{ width: '100%', justifyContent: 'space-between', marginBottom: 8 }}
              onClick={() => setForm({ email: d.email, password: 'Password@123' })}
            >
              <span style={{ fontSize: 13 }}>{d.email}</span>
              <span style={{ fontSize: 11, color: 'var(--muted)', fontWeight: 400 }}>{d.role}</span>
            </button>
          ))}
        </div>

        <p style={styles.footer}>
          No account?{' '}
          <Link to="/register" style={styles.link}>Create one</Link>
        </p>
      </div>

      {toast && <Toast msg={toast.msg} type={toast.type} />}
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    position: 'relative',
    zIndex: 1,
  },
  container: {
    width: '100%',
    maxWidth: 420,
    background: 'var(--bg2)',
    border: '1px solid var(--border2)',
    borderRadius: 'var(--radius)',
    padding: '40px 36px',
    boxShadow: 'var(--shadow)',
    animation: 'slideUp 0.3s ease',
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    marginBottom: 32,
  },
  logoIcon: {
    width: 40, height: 40,
    background: 'var(--accent)',
    borderRadius: 12,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 20, fontWeight: 700,
    boxShadow: '0 4px 16px #7c6dfa40',
  },
  logoText: {
    fontFamily: 'Syne, sans-serif',
    fontSize: 22,
    fontWeight: 800,
    color: 'var(--text)',
  },
  title: {
    fontSize: 28,
    fontWeight: 800,
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 14,
    color: 'var(--muted)',
    marginBottom: 28,
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: 16,
  },
  divider: {
    textAlign: 'center',
    position: 'relative',
    margin: '24px 0 16px',
    color: 'var(--muted2)',
    fontSize: 12,
  },
  demo: {
    background: 'var(--bg3)',
    border: '1px solid var(--border)',
    borderRadius: 'var(--radius-sm)',
    padding: 16,
    marginBottom: 20,
  },
  demoTitle: {
    fontSize: 11,
    color: 'var(--muted)',
    fontWeight: 600,
    letterSpacing: '0.08em',
    textTransform: 'uppercase',
    marginBottom: 10,
  },
  footer: {
    textAlign: 'center',
    fontSize: 13,
    color: 'var(--muted)',
  },
  link: {
    color: 'var(--accent2)',
    textDecoration: 'none',
    fontWeight: 500,
  },
}
