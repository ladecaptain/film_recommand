import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || '请求失败，请稍后重试'
    ElMessage.error(message)
    if (error.response?.status === 401) {
      const isLoginPage = window.location.pathname === '/login'
      if (!isLoginPage) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

const request: { [K in 'get' | 'post' | 'put' | 'delete']: (...args: any[]) => Promise<any> } = http as any

export default request
