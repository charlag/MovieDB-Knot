package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.moviesdbknot.ui.Navigator
import io.charlag.redukt.Epic
import io.charlag.redukt.Event
import io.charlag.redukt.filterStateChanged
import io.reactivex.Observable

/**
 * When user presses back button.
 */
object BackPressedEvent : DispatchableEvent

/**
 * Dispatched as a signal to shut down the app.
 */
object FinishAppEvent : Event

/**
 * Dispatched as a signal to change current screen
 */
data class NavigateEvent(val key: Navigator.Key, val forward: Boolean) : Event

/**
 * Epic which reacts to state changes and dispatches [NavigateEvent] events when needed.
 */
val navigateEpic: Epic<State> = { upstream ->
  upstream.filterStateChanged()
      .switchMap { (_, newState, oldState) ->
        if (newState.screens.size == oldState.screens.size
            && newState.screens.last()::class == oldState.screens.last()::class) {
          Observable.empty<Event>()
        } else {
          val direction = newState.screens.size > oldState.screens.size
          val key = screenToKey(newState.screens.last())
          Observable.just(NavigateEvent(key, direction))
        }
      }
}

/**
 * Epic which sends [FinishAppEvent] when needed.
 */
val finishAppEpic: Epic<State> = { upstream ->
  upstream.filter { (event, state) ->
    event == BackPressedEvent && state.screens.isEmpty()
  }
      .map { FinishAppEvent }
}

fun screenToKey(screen: ScreenState): Navigator.Key {
  return when (screen) {
    is DiscoverScreenState -> Navigator.Key.DiscoverKey
    is DetailsScreenState -> Navigator.Key.MovieDetailsKey
    else -> throw AssertionError("Unknown screen state")
  }
}

/**
 * Apply proper reducer to each screen.
 */
fun mapScreens(state: List<ScreenState>, event: Event): List<ScreenState> {
  return state.map { page ->
    when (page) {
      is DiscoverScreenState -> discoverScreenReducer(page, event)
      is DetailsScreenState -> detailsScreenReducer(page, event)
      else -> page
    }
  }
}

/**
 * Add or remove screens and also apply reducer to each screen.
 */
fun screensReducer(state: List<ScreenState>, event: Event): List<ScreenState> {
  return when (event) {
    is OpenMovieDetailsEvent -> state + DetailsScreenState(event.id, null)
    BackPressedEvent -> if (state.isNotEmpty()) state.dropLast(1) else state
    else -> mapScreens(state, event)
  }
}