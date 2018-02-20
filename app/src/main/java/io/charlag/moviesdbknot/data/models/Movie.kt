package io.charlag.moviesdbknot.data.models

import com.squareup.moshi.Json
import java.util.*

/**
 * Created by charlag on 14/02/18.
 */

data class Movie(
    val id: Long,
    val title: String,
    val popularity: Float,
    val overview: String?,
    @Json(name = "release_date") val releaseDate: Date?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?
)