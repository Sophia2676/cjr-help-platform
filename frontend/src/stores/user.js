import { defineStore } from 'pinia'
import { login as apiLogin, getInfo } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cjr_token') || '',
    user: null
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isWorker: (state) => state.user?.role === 'WORKER' || state.user?.role === 'ADMIN'
  },
  actions: {
    async login(form) {
      const data = await apiLogin(form)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('cjr_token', data.token)
    },
    async fetchInfo() {
      const data = await getInfo()
      this.user = data
    },
    setUser(user) {
      this.user = user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('cjr_token')
    }
  }
})
