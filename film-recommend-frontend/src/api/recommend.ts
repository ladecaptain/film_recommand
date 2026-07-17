import request from '@/utils/request'

export const recommendApi = {
  getRecommendations() {
    return request.get('/recommend')
  },
  getRandomPick() {
    return request.get('/recommend/random')
  },
}
