import client from './client'

// ---- Auth ----
export const registerUser = (data) => client.post('/api/auth/register', data)
export const loginUser = (data) => client.post('/api/auth/login', data)
export const getCurrentUser = () => client.get('/api/auth/me')

// ---- Groups ----
export const createGroup = (data) => client.post('/api/groups', data)
export const getMyGroups = () => client.get('/api/groups')
export const getGroup = (groupId) => client.get(`/api/groups/${groupId}`)
export const addMember = (groupId, data) => client.post(`/api/groups/${groupId}/members`, data)
export const removeMember = (groupId, userId) =>
  client.delete(`/api/groups/${groupId}/members/${userId}`)
export const searchUsers = (keyword) =>
  client.get(`/api/groups/users/search`, { params: { keyword } })

// ---- Expenses ----
export const addExpense = (groupId, data) => client.post(`/api/groups/${groupId}/expenses`, data)
export const getGroupExpenses = (groupId) => client.get(`/api/groups/${groupId}/expenses`)
export const getExpense = (expenseId) => client.get(`/api/groups/expenses/${expenseId}`)
export const updateExpense = (expenseId, data) =>
  client.put(`/api/groups/expenses/${expenseId}`, data)
export const deleteExpense = (expenseId) => client.delete(`/api/groups/expenses/${expenseId}`)
export const settleUp = (groupId, data) => client.post(`/api/groups/${groupId}/settle`, data)

// ---- Balances ----
export const getBalances = (groupId) => client.get(`/api/groups/${groupId}/balances`)
export const simplifyDebts = (groupId) => client.get(`/api/groups/${groupId}/simplify-debts`)
