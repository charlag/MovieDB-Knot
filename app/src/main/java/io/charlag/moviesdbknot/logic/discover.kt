package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.moviesdbknot.logic.StoreImpl.InitEvent
import io.charlag.redukt.Epic
import io.charlag.redukt.Event

/**
 * Created by charlag on 21/02/2018.
 */

data class DiscoverLoadedEvent(val discoverResponse: PagedResponse) : Event

object LoadMoreDiscoverEvent : DispatchableEvent
object RetryLoadDiscoverEvent : DispatchableEvent
data class OpenMovieDetailsEvent(val id: Long) : DispatchableEvent
data class FailedToLoadDiscoverEvent(val page: Int) : Event

data class DiscoverScreenState(val page: Int, val movies: List<Movie>) : ScreenState

fun upcomingEpic(api: Api): Epic<State> {
  return { upstream ->
    upstream.filter { (event) ->
      when (event) {
        InitEvent, LoadMoreDiscoverEvent, RetryLoadDiscoverEvent -> true
        else -> false
      }
    }
        .switchMapSingle { (_, state) ->
          val screenState = state.screens.last { it is DiscoverScreenState } as DiscoverScreenState
          val page = screenState.page + 1
          api.discoverMovies(page)
              .map { DiscoverLoadedEvent(it) as Event }
              .onErrorReturn { FailedToLoadDiscoverEvent(page) }
        }
  }
}

fun discoverScreenReducer(state: DiscoverScreenState, event: Event): DiscoverScreenState {
  return when (event) {
    is DiscoverLoadedEvent -> state.copy(
        page = event.discoverResponse.page,
        movies = state.movies + event.discoverResponse.results
    )
    else -> state
  }
}