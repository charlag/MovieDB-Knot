package io.charlag.moviesdbknot

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.BackPressedEvent
import io.charlag.moviesdbknot.logic.FinishAppEvent
import io.charlag.moviesdbknot.logic.NavigateEvent
import io.charlag.moviesdbknot.logic.Store
import io.charlag.moviesdbknot.ui.FragmentNavigator
import io.charlag.moviesdbknot.ui.Navigator.Key.DiscoverKey
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : AppCompatActivity(), Injectable {

  @Inject
  lateinit var store: Store

  private val navigator = lazy(mode = NONE) { FragmentNavigator(this, R.id.frameMain) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Set up initial view
    navigator.value.goTo(DiscoverKey, true)

    store.events.ofType<NavigateEvent>()
        .observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scope())
        .subscribe { event ->
          navigator.value.goTo(event.key, event.forward)
        }

    store.events.ofType<FinishAppEvent>()
        .autoDisposable(scope())
        .subscribe {
          finish()
        }
  }

  override fun onBackPressed() {
    store.dispatch(BackPressedEvent)
  }

}
