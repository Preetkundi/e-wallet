export default function WalletCard({ wallet, loading, userId }) {
  return (
    <div style={styles.card}>
      {/* Background decoration */}
      <div style={styles.blob1} />
      <div style={styles.blob2} />

      <div style={styles.top}>
        <div>
          <p style={styles.label}>Total Balance</p>
          {loading ? (
            <div style={styles.balanceSkeleton} className="loading" />
          ) : (
            <h2 style={styles.balance}>
              ₹{wallet ? Number(wallet.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
            </h2>
          )}
        </div>

        <div style={styles.statusPill}>
          <span style={styles.dot} />
          {wallet?.status || 'ACTIVE'}
        </div>
      </div>

      <div style={styles.divider} />

      <div style={styles.bottom}>
        <div style={styles.infoItem}>
          <p style={styles.infoLabel}>Wallet Number</p>
          <p style={styles.infoValue}>{wallet?.walletNumber || '—'}</p>
        </div>
        <div style={styles.infoItem}>
          <p style={styles.infoLabel}>User ID</p>
          <p style={styles.infoValue}>#{userId}</p>
        </div>
      </div>
    </div>
  )
}

const styles = {
  card: {
    background: 'linear-gradient(135deg, #1a1a35 0%, #0f0f22 100%)',
    border: '1px solid #ffffff18',
    borderRadius: 20,
    padding: '28px 28px 24px',
    position: 'relative',
    overflow: 'hidden',
    boxShadow: '0 20px 60px #0006',
  },
  blob1: {
    position: 'absolute', top: -40, right: -30,
    width: 180, height: 180,
    background: 'radial-gradient(circle, #7c6dfa30, transparent 70%)',
    borderRadius: '50%', pointerEvents: 'none',
  },
  blob2: {
    position: 'absolute', bottom: -30, left: 60,
    width: 140, height: 140,
    background: 'radial-gradient(circle, #00d39520, transparent 70%)',
    borderRadius: '50%', pointerEvents: 'none',
  },
  top: {
    display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
    marginBottom: 20, position: 'relative',
  },
  label: { fontSize: 12, color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: 8 },
  balance: { fontSize: 42, fontWeight: 800, fontFamily: 'Syne, sans-serif', letterSpacing: '-1px' },
  balanceSkeleton: {
    height: 46, width: 200, background: 'var(--border)', borderRadius: 8, marginTop: 4,
  },
  statusPill: {
    display: 'flex', alignItems: 'center', gap: 6,
    background: '#00d39515', border: '1px solid #00d39525',
    borderRadius: 20, padding: '5px 12px',
    fontSize: 11, fontWeight: 600, color: 'var(--green)', letterSpacing: '0.05em',
  },
  dot: {
    width: 6, height: 6, borderRadius: '50%',
    background: 'var(--green)',
    boxShadow: '0 0 6px var(--green)',
    animation: 'pulse 2s infinite',
  },
  divider: { height: 1, background: '#ffffff10', marginBottom: 20 },
  bottom: { display: 'flex', gap: 40, position: 'relative' },
  infoItem: {},
  infoLabel: { fontSize: 11, color: 'var(--muted)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.08em' },
  infoValue: { fontSize: 13, fontWeight: 500, fontFamily: 'monospace', letterSpacing: '0.05em' },
}
