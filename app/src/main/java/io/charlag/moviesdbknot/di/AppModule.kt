package io.charlag.moviesdbknot.di

import dagger.Module
import dagger.Provides
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.data.network.ApiCreator
import io.charlag.moviesdbknot.logic.Store
import io.charlag.moviesdbknot.logic.StoreImpl
import javax.inject.Singleton

/**
 * Created by charlag on 18/02/18.
 */
@Module
class AppModule {
  @Singleton
  @Provides
  fun provideStore(api: Api): Store {
    return StoreImpl(api)
  }

  @Provides
  @Singleton
  fun provideApi(@ApiKey apiKey: String, @BaseUrl baseUrl: String): Api {
    return ApiCreator.createApi(apiKey, baseUrl)
  }

  @Provides
  @ApiKey
  fun provideApiKey(): String {
    return "78033c6a43dac4dc8fedde05d406df13"
  }

  @Provides
  @BaseUrl
  fun provideBaseUrl(): String {
    return "https://api.themoviedb.org/3/"
  }
}