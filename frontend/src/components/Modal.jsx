export default function Modal({ title, onClose, children, wide = false }) {
  return (
    <div
      className="fixed inset-0 bg-ink/40 backdrop-blur-sm z-30 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        className={`card w-full ${wide ? 'max-w-lg' : 'max-w-md'} p-6 max-h-[90vh] overflow-y-auto`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-xl font-semibold text-ink">{title}</h3>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-ink/5 text-ink/50 hover:text-ink transition-colors"
            aria-label="Close"
          >
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
