package io.charlag.moviesdbknot.data.models

import com.squareup.moshi.Json

/**
 * This one should be generic but seems like Moshi cannot handle generic classes.
 */
data class PagedResponse(
        val results: List<Movie>,
        val page: Int,
        @Json(name = "total_pages") val totalPages: Long,
        @Json(name = "total_results") val totalresults: Long
)