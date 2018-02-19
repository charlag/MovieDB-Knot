package io.charlag.moviesdbknot.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.charlag.moviesdbknot.MovieDBApp
import javax.inject.Singleton

/**
 * Created by charlag on 18/02/18.
 */

@Singleton
@Component(modules = [
  AndroidInjectionModule::class,
  AppModule::class,
  UIBuildersModule::class
])
interface AppComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance
    fun application(application: Application): Builder

    fun build(): AppComponent
  }
  fun inject(app: MovieDBApp)
}