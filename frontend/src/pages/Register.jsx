import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Banner, { extractErrorMessage } from '../components/Banner'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await register(form.name, form.email, form.password)
      navigate('/')
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not create your account.'))
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
          <h1 className="font-display text-3xl font-semibold text-ink">Create your account</h1>
          <p className="text-ink/50 text-sm mt-1">Start splitting expenses in seconds.</p>
        </div>

        <div className="card p-6">
          <Banner message={error} />
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-text">Name</label>
              <input
                type="text"
                required
                className="input-field"
                placeholder="Prateek Tiwari"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </div>
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
                minLength={6}
                className="input-field"
                placeholder="At least 6 characters"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </div>
            <button type="submit" disabled={busy} className="btn-primary w-full mt-2">
              {busy ? 'Creating account…' : 'Create account'}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-ink/50 mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-moss-700 font-semibold hover:underline">
            Log in
          </Link>
        </p>
      </div>
    </div>
  )
}
