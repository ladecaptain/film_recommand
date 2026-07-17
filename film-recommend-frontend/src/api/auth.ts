import request from '@/utils/request'

export const authApi = {
  login(phoneOrEmail: string, password: string) {
    return request.post('/auth/login', { phoneOrEmail, password })
  },
  register(data: { phone?: string; email?: string; password: string; nickname?: string }) {
    return request.post('/auth/register', data)
  },
  getUserInfo() {
    return request.get('/auth/me')
  },
}
