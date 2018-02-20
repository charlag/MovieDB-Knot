package io.charlag.moviesdbknot.data.models

import com.squareup.moshi.Json

/**
 * Created by charlag on 15/02/18.
 */

class Configuration(
    @Json(name = "images") val imagesConfig: Images
) {
  data class Images(
      @Json(name = "base_url") val baseUrl: String,
      @Json(name = "secure_base_url") val secureBaseUrl: String,
      @Json(name = "poster_sizes") val posterSizes: List<String>,
      @Json(name = "backdrop_sizes") val backdropSizes: List<String>
  )
}