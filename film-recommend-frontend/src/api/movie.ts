import request from '@/utils/request'

export const movieApi = {
  getPopular(page: number = 1) {
    return request.get('/movies/popular', { params: { page } })
  },
  search(keyword: string, page: number = 1) {
    return request.get('/movies/search', { params: { keyword, page } })
  },
  getDetail(tmdbId: number) {
    return request.get(`/movies/${tmdbId}`)
  },
  discover(params: { genres?: string; year?: string; page?: number; sortBy?: string }) {
    return request.get('/movies/discover', { params })
  },
}
