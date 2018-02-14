package io.charlag.moviesdbknot.data.models

import java.util.*

/**
 * Created by charlag on 14/02/18.
 */

data class Movie(
        val id: Long,
        val title: String,
        val popularity: Float,
        val overview: String,
        val releaseDate: Date?,
        val genreIds: IntArray?,
        val posterPath: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Movie

        if (id != other.id) return false
        if (title != other.title) return false
        if (popularity != other.popularity) return false
        if (overview != other.overview) return false
        if (releaseDate != other.releaseDate) return false
        if (!Arrays.equals(genreIds, other.genreIds)) return false
        if (posterPath != other.posterPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + popularity.hashCode()
        result = 31 * result + overview.hashCode()
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + Arrays.hashCode(genreIds)
        result = 31 * result + (posterPath?.hashCode() ?: 0)
        return result
    }
}