import { createContext, useContext, useEffect, useState } from 'react'
import { loginUser, registerUser, getCurrentUser } from '../api/endpoints'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('splitease_user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('splitease_token')
    if (!token) {
      setLoading(false)
      return
    }
    getCurrentUser()
      .then((res) => {
        setUser(res.data)
        localStorage.setItem('splitease_user', JSON.stringify(res.data))
      })
      .catch(() => {
        localStorage.removeItem('splitease_token')
        localStorage.removeItem('splitease_user')
        setUser(null)
      })
      .finally(() => setLoading(false))
  }, [])

  // AuthResponse only carries token/name/email, but the rest of the app needs
  // the numeric user id (e.g. to default "paid by" on an expense), so we
  // fetch /api/auth/me right after authenticating.
  const login = async (email, password) => {
    const res = await loginUser({ email, password })
    localStorage.setItem('splitease_token', res.data.token)
    const me = await getCurrentUser()
    localStorage.setItem('splitease_user', JSON.stringify(me.data))
    setUser(me.data)
    return me.data
  }

  const register = async (name, email, password) => {
    const res = await registerUser({ name, email, password })
    localStorage.setItem('splitease_token', res.data.token)
    const me = await getCurrentUser()
    localStorage.setItem('splitease_user', JSON.stringify(me.data))
    setUser(me.data)
    return me.data
  }

  const logout = () => {
    localStorage.removeItem('splitease_token')
    localStorage.removeItem('splitease_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
