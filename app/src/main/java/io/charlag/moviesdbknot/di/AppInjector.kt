package io.charlag.moviesdbknot.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import io.charlag.moviesdbknot.MovieDBApp

/**
 * Created by charlag on 18/02/18.
 */

object AppInjector {
  fun init(app: MovieDBApp) {
    DaggerAppComponent.builder().application(app).build().inject(app)
    app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
      override fun onActivityPaused(activity: Activity?) {
      }

      override fun onActivityResumed(activity: Activity?) {
      }

      override fun onActivityStarted(activity: Activity?) {
      }

      override fun onActivityDestroyed(activity: Activity?) {
      }

      override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
      }

      override fun onActivityStopped(activity: Activity?) {
      }

      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        handleActivity(activity)
      }

    })
  }

  private fun handleActivity(activity: Activity) {
    if (activity is Injectable) {
      AndroidInjection.inject(activity)
    }
    if (activity is FragmentActivity) {
      activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
          object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(fm: FragmentManager, f: Fragment,
                savedInstanceState: Bundle?) {
              if (f is Injectable) AndroidSupportInjection.inject(f)
            }
          }, true)
    }
  }
}