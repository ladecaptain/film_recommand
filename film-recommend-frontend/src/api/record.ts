import request from '@/utils/request'

export const recordApi = {
  getMyRecords() {
    return request.get('/records')
  },
  createRecord(data: { movieId: number; rating?: number; comment?: string; status: number; watchDate?: string }) {
    return request.post('/records', data)
  },
  updateRecord(id: number, data: { rating?: number; comment?: string; status?: number }) {
    return request.put(`/records/${id}`, data)
  },
  deleteRecord(id: number) {
    return request.delete(`/records/${id}`)
  },
}
