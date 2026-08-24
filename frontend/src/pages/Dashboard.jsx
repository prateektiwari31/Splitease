import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { createGroup, getMyGroups } from '../api/endpoints'
import Modal from '../components/Modal'
import Banner, { extractErrorMessage } from '../components/Banner'

export default function Dashboard() {
  const [groups, setGroups] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [showCreate, setShowCreate] = useState(false)

  const loadGroups = () => {
    setLoading(true)
    getMyGroups()
      .then((res) => setGroups(res.data))
      .catch((err) => setLoadError(extractErrorMessage(err, 'Could not load your groups.')))
      .finally(() => setLoading(false))
  }

  useEffect(loadGroups, [])

  return (
    <div className="max-w-5xl mx-auto px-6 py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <p className="font-mono text-xs uppercase tracking-widest text-moss-700 mb-1">
            Your groups
          </p>
          <h1 className="font-display text-3xl font-semibold text-ink">The ledger</h1>
        </div>
        <button onClick={() => setShowCreate(true)} className="btn-primary">
          + New group
        </button>
      </div>

      <Banner message={loadError} />

      {loading ? (
        <p className="text-ink/50 font-mono text-sm">Loading groups…</p>
      ) : groups.length === 0 ? (
        <EmptyState onCreate={() => setShowCreate(true)} />
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {groups.map((g) => (
            <Link
              key={g.id}
              to={`/groups/${g.id}`}
              className="card p-5 hover:border-moss-400 hover:shadow-md transition-all group"
            >
              <div className="flex items-start justify-between">
                <h3 className="font-display text-lg font-semibold text-ink group-hover:text-moss-700 transition-colors">
                  {g.name}
                </h3>
                <span className="font-mono text-xs bg-moss-100 text-moss-800 rounded-full px-2.5 py-1">
                  {g.memberCount} {g.memberCount === 1 ? 'member' : 'members'}
                </span>
              </div>
              <p className="text-ink/60 text-sm mt-1.5 line-clamp-2">{g.description}</p>
              <p className="text-ink/40 text-xs font-mono mt-3">created by {g.createdBy}</p>
            </Link>
          ))}
        </div>
      )}

      {showCreate && (
        <CreateGroupModal
          onClose={() => setShowCreate(false)}
          onCreated={() => {
            setShowCreate(false)
            loadGroups()
          }}
        />
      )}
    </div>
  )
}

function EmptyState({ onCreate }) {
  return (
    <div className="card p-12 text-center border-dashed">
      <p className="font-display text-xl text-ink mb-2">No groups yet</p>
      <p className="text-ink/50 text-sm mb-5">
        Create a group for a trip, a flat, or a project and start splitting expenses.
      </p>
      <button onClick={onCreate} className="btn-primary">
        Create your first group
      </button>
    </div>
  )
}

function CreateGroupModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', description: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await createGroup(form)
      onCreated()
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not create group.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title="New group" onClose={onClose}>
      <Banner message={error} />
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="label-text">Group name</label>
          <input
            type="text"
            required
            maxLength={100}
            className="input-field"
            placeholder="Goa Trip"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
        </div>
        <div>
          <label className="label-text">Description</label>
          <input
            type="text"
            required
            maxLength={255}
            className="input-field"
            placeholder="Weekend trip with the college gang"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </div>
        <button type="submit" disabled={busy} className="btn-primary w-full mt-2">
          {busy ? 'Creating…' : 'Create group'}
        </button>
      </form>
    </Modal>
  )
}
