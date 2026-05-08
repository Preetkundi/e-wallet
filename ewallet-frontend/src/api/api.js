import axios from 'axios'

const USER_API        = 'http://localhost:8081'
const WALLET_API      = 'http://localhost:8082'
const TRANSACTION_API = 'http://localhost:8083'

// Attach JWT token to every request automatically
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Auto logout on 401
axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ──────────────────────────────────────────────
export const login = (data) =>
  axios.post(`${USER_API}/api/auth/login`, data)

export const register = (data) =>
  axios.post(`${USER_API}/api/auth/register`, data)

// ── Wallet ────────────────────────────────────────────
export const getWallet = (userId) =>
  axios.get(`${WALLET_API}/api/wallets/${userId}`)

export const getBalance = (userId) =>
  axios.get(`${WALLET_API}/api/wallets/${userId}/balance`)

export const addMoney = (userId, amount) =>
  axios.put(`${WALLET_API}/api/wallets/${userId}/add`, { amount })

export const createWallet = (userId) =>
  axios.post(`${WALLET_API}/api/wallets/create/${userId}`)

// ── Transactions ──────────────────────────────────────
export const transfer = (data) =>
  axios.post(`${TRANSACTION_API}/api/transactions/transfer`, data)

export const merchantPayment = (data) =>
  axios.post(`${TRANSACTION_API}/api/transactions/payment`, data)

export const getHistory = (userId) =>
  axios.get(`${TRANSACTION_API}/api/transactions/history/${userId}`)
