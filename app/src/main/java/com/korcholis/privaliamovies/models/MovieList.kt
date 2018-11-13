package com.korcholis.privaliamovies.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieList(
        val results: List<Movie>,
        val page: Int
)