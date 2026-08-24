import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import GroupDetail from './pages/GroupDetail'

function PrivateRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <FullScreenLoader />
  if (!user) return <Navigate to="/login" replace />
  return children
}

function FullScreenLoader() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="font-mono text-sm text-ink/50 tracking-widest animate-pulse">
        LOADING…
      </div>
    </div>
  )
}

export default function App() {
  const { user, loading } = useAuth()

  return (
    <div className="min-h-screen flex flex-col">
      {user && <Navbar />}
      <div className="flex-1">
        <Routes>
          <Route
            path="/login"
            element={loading ? <FullScreenLoader /> : user ? <Navigate to="/" replace /> : <Login />}
          />
          <Route
            path="/register"
            element={loading ? <FullScreenLoader /> : user ? <Navigate to="/" replace /> : <Register />}
          />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
          <Route
            path="/groups/:groupId"
            element={
              <PrivateRoute>
                <GroupDetail />
              </PrivateRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </div>
  )
}
