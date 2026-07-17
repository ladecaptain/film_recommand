import request from '@/utils/request'

export const recommendApi = {
  getRecommendations() {
    return request.get('/recommend')
  },
  getRandomPick(exclude?: number[]) {
    const params: Record<string, string> = {}
    if (exclude && exclude.length) {
      params.exclude = exclude.join(',')
    }
    return request.get('/recommend/random', { params })
  },
}
