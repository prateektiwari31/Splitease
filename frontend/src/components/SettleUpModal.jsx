import { useState } from 'react'
import Modal from './Modal'
import Banner, { extractErrorMessage } from './Banner'
import { settleUp } from '../api/endpoints'

// members: [{ userId, userName }]
// prefill (optional): { payerId, receiverId, amount } — from a simplified-debt suggestion
export default function SettleUpModal({ groupId, members, prefill, onClose, onSettled }) {
  const [payerId, setPayerId] = useState(prefill?.payerId ?? '')
  const [receiverId, setReceiverId] = useState(prefill?.receiverId ?? '')
  const [amount, setAmount] = useState(prefill?.amount ?? '')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (payerId === receiverId) {
      setError('Payer and receiver cannot be the same person.')
      return
    }
    setBusy(true)
    try {
      await settleUp(groupId, {
        payerId: parseInt(payerId, 10),
        receiverId: parseInt(receiverId, 10),
        amount: parseFloat(amount),
      })
      onSettled()
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not record this settlement.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title="Settle up" onClose={onClose}>
      <Banner message={error} />
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label-text">From (pays)</label>
            <select
              required
              className="input-field"
              value={payerId}
              onChange={(e) => setPayerId(e.target.value)}
            >
              <option value="" disabled>
                Select
              </option>
              {members.map((m) => (
                <option key={m.userId} value={m.userId}>
                  {m.userName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label-text">To (receives)</label>
            <select
              required
              className="input-field"
              value={receiverId}
              onChange={(e) => setReceiverId(e.target.value)}
            >
              <option value="" disabled>
                Select
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
          <label className="label-text">Amount (₹)</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            required
            className="input-field amount-mono"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
        </div>
        <button type="submit" disabled={busy} className="btn-primary w-full mt-2">
          {busy ? 'Recording…' : 'Record settlement'}
        </button>
      </form>
    </Modal>
  )
}
