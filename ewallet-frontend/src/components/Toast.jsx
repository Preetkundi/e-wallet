export default function Toast({ msg, type = 'success' }) {
  const icon = type === 'success' ? '✅' : '❌'
  return (
    <div className={`toast ${type}`}>
      <span>{icon}</span>
      <span>{msg}</span>
    </div>
  )
}
