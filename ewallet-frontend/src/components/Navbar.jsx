import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav style={styles.nav}>
      <div style={styles.inner}>
        <div style={styles.logo}>
          <div style={styles.logoIcon}>₹</div>
          <span style={styles.logoText}>Sterling</span>
        </div>

        <div style={styles.right}>
          <div style={styles.userInfo}>
            <div style={styles.avatar}>
              {user?.fullName?.[0]?.toUpperCase() || 'U'}
            </div>
            <div style={styles.userDetails}>
              <span style={styles.userName}>{user?.fullName}</span>
              <span style={styles.userRole}>{user?.role}</span>
            </div>
          </div>
          <button className="btn btn-ghost" style={{ padding: '8px 16px', fontSize: 13 }} onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}

const styles = {
  nav: {
    background: '#0a0a1488',
    backdropFilter: 'blur(20px)',
    borderBottom: '1px solid var(--border)',
    position: 'sticky', top: 0,
    zIndex: 50,
  },
  inner: {
    maxWidth: 760,
    margin: '0 auto',
    padding: '0 20px',
    height: 64,
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
  },
  logo: { display: 'flex', alignItems: 'center', gap: 8 },
  logoIcon: {
    width: 34, height: 34, background: 'var(--accent)', borderRadius: 10,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 17, fontWeight: 700,
  },
  logoText: { fontFamily: 'Syne, sans-serif', fontSize: 18, fontWeight: 800 },
  right: { display: 'flex', alignItems: 'center', gap: 16 },
  userInfo: { display: 'flex', alignItems: 'center', gap: 10 },
  avatar: {
    width: 36, height: 36,
    background: 'linear-gradient(135deg, var(--accent), var(--accent2))',
    borderRadius: '50%',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 15, fontWeight: 700,
  },
  userDetails: { display: 'flex', flexDirection: 'column' },
  userName: { fontSize: 13, fontWeight: 600, lineHeight: 1.3 },
  userRole: { fontSize: 11, color: 'var(--muted)', textTransform: 'capitalize' },
}
