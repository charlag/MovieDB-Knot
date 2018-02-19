package io.charlag.moviesdbknot

import android.app.Activity
import android.app.Application
import android.support.v4.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import io.charlag.moviesdbknot.di.AppInjector
import javax.inject.Inject

/**
 * Created by charlag on 18/02/18.
 */

class MovieDBApp : Application(), HasActivityInjector, HasSupportFragmentInjector {

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

  override fun activityInjector(): AndroidInjector<Activity> = activityInjector

  @Inject
  lateinit var activityInjector: DispatchingAndroidInjector<Activity>
  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  override fun onCreate() {
    super.onCreate()
    AppInjector.init(this)
  }
}