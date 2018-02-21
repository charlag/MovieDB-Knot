package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.moviesdbknot.ui.Navigator
import io.charlag.redukt.Epic
import io.charlag.redukt.Event
import io.charlag.redukt.filterStateChanged
import io.reactivex.Observable

/**
 * Created by charlag on 21/02/2018.
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

fun mapScreens(state: List<ScreenState>, event: Event): List<ScreenState> {
  return state.map { page ->
    when (page) {
      is DiscoverScreenState -> discoverScreenReducer(page, event)
      is DetailsScreenState -> detailsScreenReducer(page, event)
      else -> page
    }
  }
}

fun screensReducer(state: List<ScreenState>, event: Event): List<ScreenState> {
  return when (event) {
    is OpenMovieDetailsEvent -> state + DetailsScreenState(event.id, null)
    BackPressedEvent -> if (state.isNotEmpty()) state.dropLast(1) else state
    else -> mapScreens(state, event)
  }
}