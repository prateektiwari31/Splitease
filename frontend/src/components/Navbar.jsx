import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="border-b border-ink/10 bg-paper/90 backdrop-blur sticky top-0 z-20">
      <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <span className="w-8 h-8 rounded-full bg-moss-700 text-paper flex items-center justify-center font-display font-bold text-lg">
            H
          </span>
          <span className="font-display text-xl font-semibold tracking-tight text-ink">
            Hisab
          </span>
        </Link>
        <div className="flex items-center gap-4">
          <span className="hidden sm:block text-sm text-ink/60 font-body">
            {user?.name}
          </span>
          <button onClick={handleLogout} className="btn-secondary !py-1.5 !px-4 text-sm">
            Log out
          </button>
        </div>
      </div>
    </header>
  )
}
