package io.charlag.moviesdbknot.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.charlag.moviesdbknot.MainActivity
import io.charlag.moviesdbknot.ui.DiscoverFragment
import io.charlag.moviesdbknot.ui.MovieDetailsFragment

/**
 * Created by charlag on 18/02/18.
 */
@Module
abstract class UIBuildersModule {
  @ContributesAndroidInjector
  abstract fun contributeMainActivity(): MainActivity

  @ContributesAndroidInjector
  abstract fun contributeDiscoverFragment(): DiscoverFragment

  @ContributesAndroidInjector
  abstract fun contributeMovieDetailsFragment(): MovieDetailsFragment
}