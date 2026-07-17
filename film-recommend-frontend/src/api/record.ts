import request from '@/utils/request'

export const recordApi = {
  getMyRecords(status?: number) {
    return request.get('/records', { params: status != null ? { status } : {} })
  },
  saveRecord(data: { tmdbId?: number; movieId?: number; rating?: number; comment?: string; status: number; watchDate?: string }) {
    return request.post('/records', data)
  },
  deleteRecord(id: number) {
    return request.delete(`/records/${id}`)
  },
  getMyRecordForMovie(movieId: number) {
    return request.get(`/records/movie/${movieId}`)
  },
  getMovieStats(movieId: number) {
    return request.get(`/records/movie/${movieId}/stats`)
  },
  getMovieReviews(movieId: number) {
    return request.get(`/records/movie/${movieId}/reviews`)
  },
  getMyStats() {
    return request.get('/records/stats')
  },
}
