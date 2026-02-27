import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<any>(JSON.parse(localStorage.getItem('user') || 'null'))

  // Initialize axios header if token exists
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || '')

  async function login(credentials: any) {
    try {
      const response = await axios.post('/api/auth/login', credentials)
      const data = response.data
      
      if (data.token) {
        token.value = data.token
        // If the backend returns user info merged with token, we store it.
        // Or if it returns { token: '...', user: {...} }
        // Based on previous code: userStore.login(response.data, response.data.token)
        // So likely response.data is the user object which contains the token.
        user.value = data 
        
        localStorage.setItem('token', token.value as string)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
        return true
      }
      return false
    } catch (error) {
      throw error
    }
  }

  async function register(userData: any) {
    try {
      await axios.post('/api/auth/register', userData)
      return true
    } catch (error) {
      throw error
    }
  }

  function logout(redirect = true) {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
    if (redirect) {
      router.push('/login')
    }
  }

  async function checkAuth() {
    if (!token.value) return false
    try {
      // Assuming /api/auth/me returns the user object
      const response = await axios.get('/api/auth/me')
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return true
    } catch (error) {
      // If checkAuth fails (e.g. 401), we should logout but NOT redirect
      // This allows the user to stay on the public page
      logout(false)
      return false
    }
  }

  function initializeFromStorage() {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken) {
      token.value = storedToken
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
    }
    
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        user.value = null
      }
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    userRole,
    login,
    register,
    logout,
    checkAuth,
    initializeFromStorage
  }
})
