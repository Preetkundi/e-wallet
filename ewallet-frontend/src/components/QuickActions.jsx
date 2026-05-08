export default function QuickActions({ onAdd, onTransfer, onPay }) {
  const actions = [
    { icon: '＋', label: 'Add Money', sub: 'Top up wallet', color: '#00d395', bg: '#00d39515', border: '#00d39525', onClick: onAdd },
    { icon: '↗', label: 'Transfer', sub: 'Send to user', color: '#7c6dfa', bg: '#7c6dfa15', border: '#7c6dfa25', onClick: onTransfer },
    { icon: '🏪', label: 'Pay Merchant', sub: 'Make payment', color: '#ffb84d', bg: '#ffb84d15', border: '#ffb84d25', onClick: onPay },
  ]

  return (
    <div>
      <h2 style={styles.title}>Quick Actions</h2>
      <div style={styles.grid}>
        {actions.map(a => (
          <button
            key={a.label}
            onClick={a.onClick}
            style={{ ...styles.btn, background: a.bg, border: `1px solid ${a.border}` }}
          >
            <div style={{ ...styles.icon, color: a.color, background: a.bg, border: `1px solid ${a.border}` }}>
              {a.icon}
            </div>
            <div>
              <p style={styles.btnLabel}>{a.label}</p>
              <p style={styles.btnSub}>{a.sub}</p>
            </div>
          </button>
        ))}
      </div>
    </div>
  )
}

const styles = {
  title: { fontSize: 16, fontWeight: 700, marginBottom: 12 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 },
  btn: {
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
    gap: 10, padding: '20px 12px',
    borderRadius: 16, cursor: 'pointer',
    transition: 'transform 0.15s, box-shadow 0.15s',
    textAlign: 'center',
  },
  icon: {
    width: 44, height: 44, borderRadius: 12,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 20,
  },
  btnLabel: { fontSize: 13, fontWeight: 600, color: 'var(--text)', marginBottom: 2 },
  btnSub: { fontSize: 11, color: 'var(--muted)' },
}
