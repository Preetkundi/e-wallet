export default function TransactionList({ transactions, loading, userId }) {
  if (loading) return (
    <div>
      <h2 style={styles.title}>Transaction History</h2>
      {[1,2,3].map(i => (
        <div key={i} style={styles.skeleton} className="loading" />
      ))}
    </div>
  )

  return (
    <div>
      <div style={styles.header}>
        <h2 style={styles.title}>Transaction History</h2>
        <span style={styles.count}>{transactions.length} total</span>
      </div>

      {transactions.length === 0 ? (
        <div style={styles.empty}>
          <div style={styles.emptyIcon}>📋</div>
          <p style={styles.emptyText}>No transactions yet</p>
          <p style={styles.emptySub}>Your transaction history will appear here</p>
        </div>
      ) : (
        <div style={styles.list}>
          {transactions.map(tx => (
            <TxRow key={tx.id} tx={tx} userId={userId} />
          ))}
        </div>
      )}
    </div>
  )
}

function TxRow({ tx, userId }) {
  const isSender = tx.senderUserId === userId
  const isTransfer = tx.type === 'TRANSFER'
  const isPayment = tx.type === 'MERCHANT_PAYMENT'

  const icon = isPayment ? '🏪' : isSender ? '↗' : '↙'
  const iconBg = isPayment ? '#ffb84d15' : isSender ? '#7c6dfa15' : '#00d39515'
  const iconColor = isPayment ? 'var(--amber)' : isSender ? 'var(--accent2)' : 'var(--green)'
  const amountColor = isSender ? 'var(--red)' : 'var(--green)'
  const amountPrefix = isSender ? '−' : '+'

  return (
    <div style={styles.row}>
      <div style={{ ...styles.txIcon, background: iconBg, color: iconColor }}>
        {icon}
      </div>

      <div style={styles.txInfo}>
        <p style={styles.txDesc}>{tx.description || tx.type.replace('_', ' ')}</p>
        <p style={styles.txMeta}>
          {isTransfer
            ? (isSender ? `To User #${tx.receiverUserId}` : `From User #${tx.senderUserId}`)
            : `Merchant #${tx.receiverUserId}`}
          <span style={styles.dot}>·</span>
          {new Date(tx.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}
        </p>
      </div>

      <div style={styles.txRight}>
        <p style={{ ...styles.txAmount, color: amountColor }}>
          {amountPrefix}₹{Number(tx.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
        </p>
        <span className={`badge badge-${tx.status.toLowerCase() === 'success' ? 'success' : tx.status.toLowerCase() === 'failed' ? 'failed' : 'pending'}`}>
          {tx.status}
        </span>
      </div>
    </div>
  )
}

const styles = {
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
  title: { fontSize: 16, fontWeight: 700 },
  count: { fontSize: 12, color: 'var(--muted)', background: 'var(--border)', padding: '3px 10px', borderRadius: 20 },
  skeleton: { height: 64, borderRadius: 12, background: 'var(--border)', marginBottom: 8 },
  list: {
    background: 'var(--bg2)', border: '1px solid var(--border)',
    borderRadius: 16, overflow: 'hidden',
  },
  row: {
    display: 'flex', alignItems: 'center', gap: 14,
    padding: '14px 18px',
    borderBottom: '1px solid var(--border)',
    transition: 'background 0.15s',
    cursor: 'default',
  },
  txIcon: {
    width: 40, height: 40, borderRadius: 12,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 18, flexShrink: 0,
  },
  txInfo: { flex: 1, minWidth: 0 },
  txDesc: { fontSize: 14, fontWeight: 500, marginBottom: 3, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  txMeta: { fontSize: 12, color: 'var(--muted)' },
  dot: { margin: '0 6px', opacity: 0.5 },
  txRight: { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 },
  txAmount: { fontSize: 15, fontWeight: 700, fontFamily: 'Syne, sans-serif' },
  empty: {
    background: 'var(--bg2)', border: '1px solid var(--border)',
    borderRadius: 16, padding: '48px 24px', textAlign: 'center',
  },
  emptyIcon: { fontSize: 36, marginBottom: 12 },
  emptyText: { fontSize: 16, fontWeight: 600, marginBottom: 6 },
  emptySub: { fontSize: 13, color: 'var(--muted)' },
}
