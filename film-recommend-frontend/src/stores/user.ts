import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(phoneOrEmail: string, password: string) {
    const res = await authApi.login(phoneOrEmail, password)
    token.value = res.token
    localStorage.setItem('token', res.token)
    userInfo.value = res.userInfo
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    const info = await authApi.getUserInfo()
    userInfo.value = info
  }

  return { token, userInfo, isLoggedIn, login, logout, fetchUserInfo }
})
