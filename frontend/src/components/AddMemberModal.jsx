import { useState } from 'react'
import Modal from './Modal'
import Banner, { extractErrorMessage } from './Banner'
import { addMember, searchUsers } from '../api/endpoints'

export default function AddMemberModal({ groupId, onClose, onAdded }) {
  const [email, setEmail] = useState('')
  const [keyword, setKeyword] = useState('')
  const [results, setResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const handleSearch = async (value) => {
    setKeyword(value)
    if (value.trim().length < 2) {
      setResults([])
      return
    }
    setSearching(true)
    try {
      const res = await searchUsers(value.trim())
      setResults(res.data)
    } catch {
      setResults([])
    } finally {
      setSearching(false)
    }
  }

  const submitEmail = async (emailToAdd) => {
    setError('')
    setBusy(true)
    try {
      await addMember(groupId, { email: emailToAdd })
      onAdded()
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not add this member.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title="Add a member" onClose={onClose}>
      <Banner message={error} />

      <div className="mb-5">
        <label className="label-text">Search by name or email</label>
        <input
          type="text"
          className="input-field"
          placeholder="Start typing…"
          value={keyword}
          onChange={(e) => handleSearch(e.target.value)}
        />
        {searching && <p className="text-xs text-ink/40 mt-1.5 font-mono">Searching…</p>}
        {results.length > 0 && (
          <div className="mt-2 space-y-1.5">
            {results.map((u) => (
              <button
                key={u.id}
                type="button"
                disabled={busy}
                onClick={() => submitEmail(u.email)}
                className="w-full flex items-center justify-between px-3 py-2 rounded-xl border border-ink/10 hover:border-moss-400 hover:bg-moss-50 text-left transition-colors"
              >
                <span>
                  <span className="block text-sm font-medium text-ink">{u.name}</span>
                  <span className="block text-xs text-ink/50">{u.email}</span>
                </span>
                <span className="text-moss-700 text-sm font-semibold">Add</span>
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="flex items-center gap-3 mb-5">
        <div className="flex-1 h-px bg-ink/10" />
        <span className="text-xs font-mono text-ink/40">OR ADD BY EMAIL</span>
        <div className="flex-1 h-px bg-ink/10" />
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault()
          submitEmail(email)
        }}
        className="flex gap-2"
      >
        <input
          type="email"
          required
          className="input-field"
          placeholder="friend@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <button type="submit" disabled={busy} className="btn-primary whitespace-nowrap">
          {busy ? 'Adding…' : 'Add'}
        </button>
      </form>
    </Modal>
  )
}
