import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  getGroup,
  getGroupExpenses,
  getBalances,
  simplifyDebts,
  deleteExpense,
  removeMember,
} from '../api/endpoints'
import Banner, { extractErrorMessage } from '../components/Banner'
import ExpenseModal from '../components/ExpenseModal'
import AddMemberModal from '../components/AddMemberModal'
import SettleUpModal from '../components/SettleUpModal'
import { useAuth } from '../context/AuthContext'

export default function GroupDetail() {
  const { groupId } = useParams()
  const { user } = useAuth()

  const [group, setGroup] = useState(null)
  const [expenses, setExpenses] = useState([])
  const [balances, setBalances] = useState([])
  const [suggestions, setSuggestions] = useState([])
  const [tab, setTab] = useState('expenses')
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  const [showAddExpense, setShowAddExpense] = useState(false)
  const [editingExpense, setEditingExpense] = useState(null)
  const [showAddMember, setShowAddMember] = useState(false)
  const [settlePrefill, setSettlePrefill] = useState(null)
  const [showSettle, setShowSettle] = useState(false)

  const loadAll = () => {
    setLoading(true)
    setLoadError('')
    Promise.all([
      getGroup(groupId),
      getGroupExpenses(groupId),
      getBalances(groupId),
      simplifyDebts(groupId),
    ])
      .then(([g, e, b, s]) => {
        setGroup(g.data)
        setExpenses(e.data)
        setBalances(b.data.balances)
        setSuggestions(s.data)
      })
      .catch((err) => setLoadError(extractErrorMessage(err, 'Could not load this group.')))
      .finally(() => setLoading(false))
  }

  useEffect(loadAll, [groupId])

  const members = balances.map((b) => ({ userId: b.userId, userName: b.userName }))
  const currentMember = members.find((m) => m.userName === user?.name)

  const handleDeleteExpense = async (expenseId) => {
    if (!confirm('Delete this expense? This cannot be undone.')) return
    try {
      await deleteExpense(expenseId)
      loadAll()
    } catch (err) {
      alert(extractErrorMessage(err, 'Could not delete this expense.'))
    }
  }

  const handleRemoveMember = async (userId, name) => {
    if (!confirm(`Remove ${name} from the group?`)) return
    try {
      await removeMember(groupId, userId)
      loadAll()
    } catch (err) {
      alert(extractErrorMessage(err, 'Could not remove this member.'))
    }
  }

  if (loading) {
    return <p className="max-w-4xl mx-auto px-6 py-10 text-ink/50 font-mono text-sm">Loading group…</p>
  }

  if (loadError && !group) {
    return (
      <div className="max-w-4xl mx-auto px-6 py-10">
        <Banner message={loadError} />
        <Link to="/" className="text-moss-700 font-semibold text-sm hover:underline">
          ← Back to groups
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-10">
      <Link to="/" className="text-sm text-ink/50 hover:text-moss-700 font-mono mb-4 inline-block">
        ← all groups
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-4 mb-2">
        <div>
          <h1 className="font-display text-3xl font-semibold text-ink">{group.name}</h1>
          <p className="text-ink/50 text-sm mt-1">{group.description}</p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setShowAddMember(true)} className="btn-secondary text-sm !px-4 !py-2">
            + Member
          </button>
          <button
            onClick={() => setShowAddExpense(true)}
            className="btn-primary text-sm !px-4 !py-2"
          >
            + Expense
          </button>
        </div>
      </div>

      <Banner message={loadError} />

      {/* Tabs */}
      <div className="flex gap-1 border-b border-ink/10 mt-6 mb-6">
        {['expenses', 'balances'].map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2.5 text-sm font-semibold capitalize border-b-2 transition-colors ${
              tab === t
                ? 'border-moss-700 text-moss-800'
                : 'border-transparent text-ink/40 hover:text-ink/70'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'expenses' ? (
        <ExpensesTab
          expenses={expenses}
          onEdit={(exp) => setEditingExpense(exp)}
          onDelete={handleDeleteExpense}
        />
      ) : (
        <BalancesTab
          balances={balances}
          suggestions={suggestions}
          currentUserName={user?.name}
          onSettleSuggestion={(s) => {
            const payer = members.find((m) => m.userName === s.fromUser)
            const receiver = members.find((m) => m.userName === s.toUser)
            setSettlePrefill({ payerId: payer?.userId, receiverId: receiver?.userId, amount: s.amount })
            setShowSettle(true)
          }}
          onSettleManually={() => {
            setSettlePrefill(null)
            setShowSettle(true)
          }}
        />
      )}

      {members.length > 0 && (
        <div className="mt-10 pt-6 border-t border-ink/10">
          <h4 className="font-mono text-xs uppercase tracking-widest text-ink/40 mb-3">
            Members ({members.length})
          </h4>
          <div className="flex flex-wrap gap-2">
            {members.map((m) => (
              <span
                key={m.userId}
                className="inline-flex items-center gap-2 bg-white border border-ink/10 rounded-full pl-3 pr-1.5 py-1 text-sm text-ink"
              >
                {m.userName}
                <button
                  onClick={() => handleRemoveMember(m.userId, m.userName)}
                  className="w-5 h-5 rounded-full hover:bg-clay/10 text-ink/30 hover:text-clay flex items-center justify-center text-xs"
                  title={`Remove ${m.userName}`}
                >
                  ✕
                </button>
              </span>
            ))}
          </div>
        </div>
      )}

      {showAddExpense && (
        <ExpenseModal
          groupId={groupId}
          members={members}
          defaultPayerId={currentMember?.userId}
          onClose={() => setShowAddExpense(false)}
          onSaved={() => {
            setShowAddExpense(false)
            loadAll()
          }}
        />
      )}

      {editingExpense && (
        <ExpenseModal
          groupId={groupId}
          members={members}
          existingExpense={editingExpense}
          onClose={() => setEditingExpense(null)}
          onSaved={() => {
            setEditingExpense(null)
            loadAll()
          }}
        />
      )}

      {showAddMember && (
        <AddMemberModal
          groupId={groupId}
          onClose={() => setShowAddMember(false)}
          onAdded={() => {
            setShowAddMember(false)
            loadAll()
          }}
        />
      )}

      {showSettle && (
        <SettleUpModal
          groupId={groupId}
          members={members}
          prefill={settlePrefill}
          onClose={() => setShowSettle(false)}
          onSettled={() => {
            setShowSettle(false)
            loadAll()
          }}
        />
      )}
    </div>
  )
}

function ExpensesTab({ expenses, onEdit, onDelete }) {
  if (expenses.length === 0) {
    return (
      <div className="card p-10 text-center border-dashed">
        <p className="text-ink/50 text-sm">No expenses logged yet. Add the first one.</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {expenses.map((exp) => (
        <div key={exp.id} className="card p-4">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="font-display text-lg text-ink font-medium">{exp.description}</p>
              <p className="text-xs text-ink/45 font-mono mt-0.5">
                paid by {exp.paidBy} · {new Date(exp.createdAt).toLocaleDateString()} ·{' '}
                {exp.splitType.toLowerCase()} split
              </p>
            </div>
            <div className="text-right shrink-0">
              <p className="amount-mono text-lg font-semibold text-ink">
                ₹{exp.totalAmount.toFixed(2)}
              </p>
              <div className="flex gap-3 justify-end mt-1 text-xs">
                <button onClick={() => onEdit(exp)} className="text-moss-700 hover:underline font-semibold">
                  Edit
                </button>
                <button onClick={() => onDelete(exp.id)} className="btn-danger">
                  Delete
                </button>
              </div>
            </div>
          </div>
          {exp.splits?.length > 0 && (
            <div className="mt-3 pt-3 border-t border-ink/5 flex flex-wrap gap-x-4 gap-y-1">
              {exp.splits.map((s, i) => (
                <span key={i} className="text-xs text-ink/50 font-mono">
                  {s.userName}: ₹{s.amountOwed.toFixed(2)}
                </span>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  )
}

function BalancesTab({ balances, suggestions, currentUserName, onSettleSuggestion, onSettleManually }) {
  return (
    <div className="space-y-8">
      <div>
        <h3 className="font-display text-lg font-semibold text-ink mb-3">Net balances</h3>
        <div className="grid sm:grid-cols-2 gap-3">
          {balances.map((b) => (
            <div key={b.userId} className="card p-4 flex items-center justify-between">
              <span className="text-sm font-medium text-ink">
                {b.userName}
                {b.userName === currentUserName && (
                  <span className="text-xs text-ink/40 font-mono ml-1.5">(you)</span>
                )}
              </span>
              <span
                className={`amount-mono text-sm font-semibold ${
                  b.netBalance > 0.005
                    ? 'text-moss-700'
                    : b.netBalance < -0.005
                    ? 'text-clay'
                    : 'text-ink/40'
                }`}
              >
                {b.netBalance > 0.005
                  ? `gets back ₹${b.netBalance.toFixed(2)}`
                  : b.netBalance < -0.005
                  ? `owes ₹${Math.abs(b.netBalance).toFixed(2)}`
                  : 'settled up'}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="font-display text-lg font-semibold text-ink">Suggested settlements</h3>
          <button onClick={onSettleManually} className="text-sm text-moss-700 font-semibold hover:underline">
            Settle manually
          </button>
        </div>
        {suggestions.length === 0 ? (
          <div className="card p-6 text-center border-dashed">
            <p className="text-ink/50 text-sm">Everyone's settled up. Nothing to simplify.</p>
          </div>
        ) : (
          <div className="space-y-2">
            {suggestions.map((s, i) => (
              <div key={i} className="card p-4 flex items-center justify-between">
                <p className="text-sm text-ink">
                  <span className="font-semibold">{s.fromUser}</span>
                  <span className="text-ink/40"> owes </span>
                  <span className="font-semibold">{s.toUser}</span>
                </p>
                <div className="flex items-center gap-3">
                  <span className="amount-mono text-sm font-semibold text-ink">
                    ₹{s.amount.toFixed(2)}
                  </span>
                  <button
                    onClick={() => onSettleSuggestion(s)}
                    className="btn-secondary !px-3 !py-1.5 text-xs"
                  >
                    Settle
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
