package com.korcholis.privaliamovies.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Movie(
        val id: Int,
        @Json(name = "poster_path") val poster: String?,
        val title: String,
        @Json(name = "overview") val synopsis: String,
        @Json(name = "release_date") val releaseDate: String,
        @Json(name = "vote_average") val voteAverage: Double
) : Parcelable {
    companion object {
        private const val TMDB_IMAGE_URL = "http://image.tmdb.org/t/p/w185/"
    }

    fun getPosterPath(relativePath: Boolean = false) = (if (relativePath) "" else TMDB_IMAGE_URL) + poster
}