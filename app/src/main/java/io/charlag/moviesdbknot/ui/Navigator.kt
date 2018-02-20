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

    open val tag: String get() = toString()
  }
}

class FragmentNavigator(
    activity: FragmentActivity,
    @IdRes private val frameId: Int
) : Navigator {

  override fun goTo(key: Key, forward: Boolean) {
    val tag = key.tag
    if (!forward) {
      for (i in fragmentManager.backStackEntryCount - 1 downTo 0) {
        if (fragmentManager.getBackStackEntryAt(i).name == tag) {
          fragmentManager.popBackStackImmediate(tag, 0)
          return
        }
      }
    }

    fragmentManager.beginTransaction()
        .replace(frameId, fragment(key))
        .run {
          if (forward) addToBackStack(tag) else this
        }
        .commit()
  }

  private val fragmentManager = activity.supportFragmentManager

  private fun fragment(key: Navigator.Key): Fragment {
    return when (key) {
      DiscoverKey -> DiscoverFragment()
      MovieDetailsKey -> MovieDetailsFragment()
    }
  }
}