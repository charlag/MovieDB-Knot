package io.charlag.moviesdbknot.ui

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentManager.BackStackEntry
import io.charlag.moviesdbknot.ui.Navigator.Key
import io.charlag.moviesdbknot.ui.Navigator.Key.DiscoverKey
import io.charlag.moviesdbknot.ui.Navigator.Key.MovieDetailsKey

/**
 * Created by charlag on 18/02/18.
 */

interface Navigator {
  fun goTo(key: Key, forward: Boolean)

  sealed class Key {
    object DiscoverKey : Key()
    object MovieDetailsKey : Key()
  }
}

class FragmentNavigator(
    activity: FragmentActivity,
    @IdRes private val frameId: Int
) : Navigator {

  override fun goTo(key: Key, forward: Boolean) {

    fragmentManager.beginTransaction()
        .replace(frameId, fragment(key))
        .commitNowAllowingStateLoss()
  }

  private val fragmentManager = activity.supportFragmentManager

  private fun fragment(key: Navigator.Key): Fragment {
    return when (key) {
      DiscoverKey -> DiscoverFragment()
      MovieDetailsKey -> MovieDetailsFragment()
    }
  }
}