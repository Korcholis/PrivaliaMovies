package com.korcholis.privaliamovies.data

import com.korcholis.privaliamovies.models.MovieList
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface TMDbApiSignature {

    companion object {
        const val PATH_MOVIES = "movie/popular"
        const val PATH_SEARCH = "search/movie"
        const val QUERY_SEARCH = "query"
        const val QUERY_PAGE = "page"
        const val TMDB_URL = "https://api.themoviedb.org/3/"
        const val QUERY_API_KEY = "api_key"
    }

    @GET(PATH_MOVIES)
    fun movieList(@Query(QUERY_PAGE) page: Int = 1): Single<MovieList>

    @GET(PATH_SEARCH)
    fun search(@Query(QUERY_SEARCH) query: String, @Query(QUERY_PAGE) page: Int = 1): Single<MovieList>
}