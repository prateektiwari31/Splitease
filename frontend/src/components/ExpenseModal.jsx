import { useEffect, useMemo, useState } from 'react'
import Modal from './Modal'
import Banner, { extractErrorMessage } from './Banner'
import { addExpense, updateExpense } from '../api/endpoints'

// members: [{ userId, userName }]
// existingExpense (optional): { id, description, totalAmount, splitType, splits: [{userName, amountOwed}] }
// Note: the API doesn't echo back paidByUserId/participant ids on an expense,
// only names, so when editing we resolve names back to member ids.
export default function ExpenseModal({ groupId, members, defaultPayerId, existingExpense, onClose, onSaved }) {
  const isEdit = Boolean(existingExpense)

  const nameToId = useMemo(() => {
    const map = {}
    members.forEach((m) => (map[m.userName] = m.userId))
    return map
  }, [members])

  const [description, setDescription] = useState(existingExpense?.description || '')
  const [totalAmount, setTotalAmount] = useState(existingExpense?.totalAmount ?? '')
  const [paidByUserId, setPaidByUserId] = useState(
    isEdit ? nameToId[existingExpense.paidBy] ?? '' : defaultPayerId ?? ''
  )
  const [splitType, setSplitType] = useState(existingExpense?.splitType || 'EQUAL')
  const [participants, setParticipants] = useState(() => {
    if (isEdit) return existingExpense.splits.map((s) => nameToId[s.userName]).filter(Boolean)
    return members.map((m) => m.userId)
  })
  const [splits, setSplits] = useState(() => {
    if (isEdit && existingExpense.splitType !== 'EQUAL') {
      return existingExpense.splits.map((s) => ({
        userId: nameToId[s.userName],
        value:
          existingExpense.splitType === 'PERCENTAGE'
            ? Math.round(((s.amountOwed / existingExpense.totalAmount) * 100 + Number.EPSILON) * 100) / 100
            : s.amountOwed,
      }))
    }
    return members.map((m) => ({ userId: m.userId, value: '' }))
  })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    // Reset per-member split rows whenever split type changes to a manual mode
    if (splitType !== 'EQUAL' && splits.length !== members.length) {
      setSplits(members.map((m) => ({ userId: m.userId, value: '' })))
    }
  }, [splitType]) // eslint-disable-line react-hooks/exhaustive-deps

  const toggleParticipant = (userId) => {
    setParticipants((prev) =>
      prev.includes(userId) ? prev.filter((id) => id !== userId) : [...prev, userId]
    )
  }

  const updateSplitValue = (userId, value) => {
    setSplits((prev) => prev.map((s) => (s.userId === userId ? { ...s, value } : s)))
  }

  const splitSum = useMemo(
    () => splits.reduce((sum, s) => sum + (parseFloat(s.value) || 0), 0),
    [splits]
  )

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (splitType === 'EQUAL' && participants.length === 0) {
      setError('Pick at least one participant to split with.')
      return
    }
    if (splitType === 'PERCENTAGE' && Math.abs(splitSum - 100) > 0.01) {
      setError(`Percentages must add up to 100 (currently ${splitSum.toFixed(2)}).`)
      return
    }
    if (splitType === 'EXACT' && Math.abs(splitSum - parseFloat(totalAmount)) > 0.01) {
      setError(`Split amounts must add up to ₹${totalAmount} (currently ₹${splitSum.toFixed(2)}).`)
      return
    }

    const payload = {
      description,
      totalAmount: parseFloat(totalAmount),
      paidByUserId: parseInt(paidByUserId, 10),
      splitType,
    }

    if (splitType === 'EQUAL') {
      payload.participants = participants
    } else {
      payload.splits = splits
        .filter((s) => parseFloat(s.value) > 0)
        .map((s) => ({
          userId: s.userId,
          ...(splitType === 'PERCENTAGE'
            ? { percentage: parseFloat(s.value) }
            : { amount: parseFloat(s.value) }),
        }))
    }

    setBusy(true)
    try {
      if (isEdit) {
        await updateExpense(existingExpense.id, payload)
      } else {
        await addExpense(groupId, payload)
      }
      onSaved()
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not save this expense.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={isEdit ? 'Edit expense' : 'Add expense'} onClose={onClose} wide>
      <Banner message={error} />
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="label-text">Description</label>
          <input
            type="text"
            required
            className="input-field"
            placeholder="Dinner at the beach shack"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label-text">Total amount (₹)</label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              required
              className="input-field amount-mono"
              placeholder="0.00"
              value={totalAmount}
              onChange={(e) => setTotalAmount(e.target.value)}
            />
          </div>
          <div>
            <label className="label-text">Paid by</label>
            <select
              required
              className="input-field"
              value={paidByUserId}
              onChange={(e) => setPaidByUserId(e.target.value)}
            >
              <option value="" disabled>
                Select member
              </option>
              {members.map((m) => (
                <option key={m.userId} value={m.userId}>
                  {m.userName}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="label-text">Split type</label>
          <div className="flex gap-2">
            {['EQUAL', 'PERCENTAGE', 'EXACT'].map((type) => (
              <button
                type="button"
                key={type}
                onClick={() => setSplitType(type)}
                className={`flex-1 py-2 rounded-xl text-sm font-semibold border transition-colors ${
                  splitType === type
                    ? 'bg-moss-700 text-paper border-moss-700'
                    : 'bg-white border-ink/15 text-ink/60 hover:border-moss-400'
                }`}
              >
                {type === 'EQUAL' ? 'Equal' : type === 'PERCENTAGE' ? 'Percentage' : 'Exact'}
              </button>
            ))}
          </div>
        </div>

        {splitType === 'EQUAL' ? (
          <div>
            <label className="label-text">Split between</label>
            <div className="space-y-1.5 max-h-44 overflow-y-auto pr-1">
              {members.map((m) => (
                <label
                  key={m.userId}
                  className="flex items-center gap-3 px-3 py-2 rounded-xl border border-ink/10 hover:bg-ink/5 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    checked={participants.includes(m.userId)}
                    onChange={() => toggleParticipant(m.userId)}
                    className="accent-moss-700 w-4 h-4"
                  />
                  <span className="text-sm text-ink">{m.userName}</span>
                  {participants.includes(m.userId) && totalAmount > 0 && (
                    <span className="ml-auto amount-mono text-xs text-ink/50">
                      ₹{(parseFloat(totalAmount) / participants.length).toFixed(2)}
                    </span>
                  )}
                </label>
              ))}
            </div>
          </div>
        ) : (
          <div>
            <label className="label-text">
              {splitType === 'PERCENTAGE' ? 'Percentage per member' : 'Amount per member (₹)'}
            </label>
            <div className="space-y-1.5 max-h-44 overflow-y-auto pr-1">
              {splits.map((s) => {
                const member = members.find((m) => m.userId === s.userId)
                return (
                  <div key={s.userId} className="flex items-center gap-3">
                    <span className="text-sm text-ink w-32 truncate">{member?.userName}</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      className="input-field amount-mono !py-1.5"
                      placeholder="0"
                      value={s.value}
                      onChange={(e) => updateSplitValue(s.userId, e.target.value)}
                    />
                  </div>
                )
              })}
            </div>
            <p className="text-xs font-mono mt-2 text-ink/50">
              Total: {splitSum.toFixed(2)}
              {splitType === 'PERCENTAGE' ? ' / 100' : totalAmount ? ` / ${totalAmount}` : ''}
            </p>
          </div>
        )}

        <button type="submit" disabled={busy} className="btn-primary w-full mt-2">
          {busy ? 'Saving…' : isEdit ? 'Save changes' : 'Add expense'}
        </button>
      </form>
    </Modal>
  )
}
