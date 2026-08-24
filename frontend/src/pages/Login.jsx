import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Banner, { extractErrorMessage } from '../components/Banner'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await login(form.email, form.password)
      navigate('/')
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not log in. Check your credentials.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <span className="inline-flex w-12 h-12 rounded-full bg-moss-700 text-paper items-center justify-center font-display font-bold text-2xl mb-3">
            S
          </span>
          <h1 className="font-display text-3xl font-semibold text-ink">Welcome back</h1>
          <p className="text-ink/50 text-sm mt-1">Log in to settle up with your groups.</p>
        </div>

        <div className="card p-6">
          <Banner message={error} />
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-text">Email</label>
              <input
                type="email"
                required
                className="input-field"
                placeholder="you@example.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </div>
            <div>
              <label className="label-text">Password</label>
              <input
                type="password"
                required
                className="input-field"
                placeholder="••••••••"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </div>
            <button type="submit" disabled={busy} className="btn-primary w-full mt-2">
              {busy ? 'Logging in…' : 'Log in'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-ink/50 mt-6">
          New here?{' '}
          <Link to="/register" className="text-moss-700 font-semibold hover:underline">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  )
}
