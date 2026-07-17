export interface UserInfo {
  id: number
  phone?: string
  email?: string
  nickname?: string
  avatar?: string
  createTime: string
}

export interface Movie {
  id: number
  tmdbId: number
  title: string
  originalTitle?: string
  posterPath?: string
  posterUrl?: string
  overview?: string
  releaseDate?: string
  voteAverage: number
  voteCount: number
  runtime?: number
  genres?: string
  director?: string
  cast?: string
  matchScore?: number
  matchGenre?: string
}

export interface WatchRecord {
  id: number
  userId: number
  movieId: number
  movie: Movie
  rating?: number
  comment?: string
  status: number // 1=想看 2=看过
  watchDate?: string
  createTime: string
}

export interface RecommendMovie extends Movie {
  matchScore: number
  matchGenre: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}
