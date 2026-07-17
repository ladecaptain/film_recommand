import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Movie } from '@/types'

export const useMovieStore = defineStore('movie', () => {
  const currentMovie = ref<Movie | null>(null)
  const searchQuery = ref('')
  const selectedGenre = ref<number | null>(null)

  function setCurrentMovie(movie: Movie) {
    currentMovie.value = movie
  }

  function setSearchQuery(query: string) {
    searchQuery.value = query
  }

  function setSelectedGenre(genre: number | null) {
    selectedGenre.value = genre
  }

  return { currentMovie, searchQuery, selectedGenre, setCurrentMovie, setSearchQuery, setSelectedGenre }
})
