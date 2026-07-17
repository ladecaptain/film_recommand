export const GENRE_MAP: Record<number, string> = {
  28: '动作',
  12: '冒险',
  16: '动画',
  35: '喜剧',
  80: '犯罪',
  99: '纪录',
  18: '剧情',
  10751: '家庭',
  14: '奇幻',
  36: '历史',
  27: '恐怖',
  10402: '音乐',
  9648: '悬疑',
  10749: '爱情',
  878: '科幻',
  10770: '电视电影',
  53: '惊悚',
  10752: '战争',
  37: '西部',
}

export const GENRE_LIST = Object.entries(GENRE_MAP).map(([id, name]) => ({
  id: Number(id),
  name,
}))

export const TMDB_IMAGE_BASE = 'https://image.tmdb.org/t/p'

export const POSTER_SIZES = {
  small: 'w185',
  medium: 'w342',
  large: 'w500',
  original: 'original',
}

export function getPosterUrl(path: string | null, size: string = POSTER_SIZES.medium): string {
  if (!path) return ''
  return `${TMDB_IMAGE_BASE}/${size}${path}`
}
