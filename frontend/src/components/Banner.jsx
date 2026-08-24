export default function Banner({ type = 'error', message }) {
  if (!message) return null
  const styles =
    type === 'error'
      ? 'bg-clay/10 border-clay/30 text-clay'
      : 'bg-moss-100 border-moss-300 text-moss-800'
  return (
    <div className={`border rounded-xl px-4 py-2.5 text-sm font-medium mb-4 ${styles}`}>
      {message}
    </div>
  )
}

export function extractErrorMessage(err, fallback = 'Something went wrong. Please try again.') {
  const data = err?.response?.data
  if (!data) return fallback
  if (data.message) return data.message
  // Bean-validation errors come back as a flat { field: "message" } map.
  if (typeof data === 'object') {
    const first = Object.values(data)[0]
    if (typeof first === 'string') return first
  }
  return fallback
}
