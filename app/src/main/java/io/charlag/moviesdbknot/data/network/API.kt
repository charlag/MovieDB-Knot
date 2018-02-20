package io.charlag.moviesdbknot.data.network

import android.annotation.SuppressLint
import com.squareup.moshi.*
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by charlag on 14/02/18.
 */

object ApiCreator {
  fun createApi(apiKey: String, baseUrl: String): Api {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, DateFormatJsonAdapter().nullSafe())
        .build()

    val http = OkHttpClient.Builder()
        .addInterceptor(apiInterceptor(apiKey))
        .addInterceptor(HttpLoggingInterceptor().setLevel(Level.BODY))
        .build()

    return Retrofit.Builder()
        .client(http)
        .baseUrl(baseUrl)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(Api::class.java)
  }

  private fun apiInterceptor(apiKey: String): Interceptor {
    return Interceptor { chain ->
      val url = chain.request().url().newBuilder()
          .addQueryParameter("api_key", apiKey)
          .build()
      val request = chain.request().newBuilder().url(url).build()
      chain.proceed(request)
    }
  }

  class DateFormatJsonAdapter : JsonAdapter<Date>() {
    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("YYYY-mm-dd")

    override fun fromJson(reader: JsonReader): Date? {
      return try {
        format.parse(reader.nextString())
      } catch (_: ParseException) {
        null
      }
    }

    override fun toJson(writer: JsonWriter?, value: Date?) {
    }

  }
}

interface Api {
  @GET("configuration")
  fun configuration(): Single<Configuration>

  @GET("movie/upcoming")
  fun upcoming(): Single<PagedResponse>

  @GET("discover/movie")
  fun discoverMovies(@Query("page") page: Int): Single<PagedResponse>

  @GET("movie/{movieId}")
  fun getMovie(@Path("movieId") movieId: Long): Single<Movie>
}