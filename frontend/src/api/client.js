import axios from 'axios'

// Change this if your Spring Boot backend runs on a different host/port.
export const BASE_URL = 'https://splitease-b54e.onrender.com'

const client = axios.create({
  baseURL: BASE_URL,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('splitease_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('splitease_token')
      localStorage.removeItem('splitease_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default client
