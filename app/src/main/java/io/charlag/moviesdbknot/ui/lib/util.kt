package io.charlag.moviesdbknot.ui.lib

/**
 * Created by charlag on 20/02/18.
 */

fun buildImageUrl(sizes: List<String>, baseUrl: String, preferredSize: String?,
    filePath: String): String {
  return baseUrl + pickSize(preferredSize, sizes) + filePath
}

private fun pickSize(preferredSize: String?, sizes: List<String>): String {
  return if (sizes.contains(preferredSize)) preferredSize!! else sizes[sizes.size - 1]
}