import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { getWallet, getHistory } from '../api/api'
import Navbar from '../components/Navbar'
import WalletCard from '../components/WalletCard'
import QuickActions from '../components/QuickActions'
import TransactionList from '../components/TransactionList'
import AddMoneyModal from '../components/AddMoneyModal'
import TransferModal from '../components/TransferModal'
import PaymentModal from '../components/PaymentModal'
import Toast from '../components/Toast'

export default function Dashboard() {
  const { user } = useAuth()
  const [wallet, setWallet] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null) // 'add' | 'transfer' | 'pay'
  const [toast, setToast] = useState(null)

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  const fetchData = useCallback(async () => {
    try {
      const [w, t] = await Promise.all([
        getWallet(user.userId),
        getHistory(user.userId),
      ])
      setWallet(w.data)
      setTransactions(t.data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }, [user.userId])

  useEffect(() => { fetchData() }, [fetchData])

  const handleSuccess = (msg) => {
    setModal(null)
    showToast(msg)
    fetchData()
  }

  return (
    <div style={styles.page}>
      <Navbar />

      <main style={styles.main}>

        {/* Greeting */}
        <div style={styles.greeting}>
          <h1 style={styles.greetTitle}>
            Good {getTimeOfDay()}, {user.fullName?.split(' ')[0]} 👋
          </h1>
          <p style={styles.greetSub}>Here's your wallet overview</p>
        </div>

        {/* Wallet Card */}
        <WalletCard wallet={wallet} loading={loading} userId={user.userId} />

        {/* Quick Actions */}
        <QuickActions
          onAdd={() => setModal('add')}
          onTransfer={() => setModal('transfer')}
          onPay={() => setModal('pay')}
        />

        {/* Transaction History */}
        <TransactionList
          transactions={transactions}
          loading={loading}
          userId={user.userId}
        />

      </main>

      {/* Modals */}
      {modal === 'add' && (
        <AddMoneyModal
          userId={user.userId}
          onClose={() => setModal(null)}
          onSuccess={() => handleSuccess('Money added successfully!')}
        />
      )}
      {modal === 'transfer' && (
        <TransferModal
          userId={user.userId}
          onClose={() => setModal(null)}
          onSuccess={() => handleSuccess('Transfer successful!')}
        />
      )}
      {modal === 'pay' && (
        <PaymentModal
          userId={user.userId}
          onClose={() => setModal(null)}
          onSuccess={() => handleSuccess('Payment successful!')}
        />
      )}

      {toast && <Toast msg={toast.msg} type={toast.type} />}
    </div>
  )
}

function getTimeOfDay() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}

const styles = {
  page: { minHeight: '100vh', position: 'relative', zIndex: 1 },
  main: {
    maxWidth: 760,
    margin: '0 auto',
    padding: '24px 20px 60px',
    display: 'flex',
    flexDirection: 'column',
    gap: 20,
  },
  greeting: { marginBottom: 4 },
  greetTitle: { fontSize: 28, fontWeight: 800, marginBottom: 4 },
  greetSub: { fontSize: 14, color: 'var(--muted)' },
}
