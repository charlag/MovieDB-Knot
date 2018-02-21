package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.moviesdbknot.logic.StoreImpl.InitEvent
import io.charlag.redukt.Epic
import io.charlag.redukt.Event
import java.util.Calendar

/**
 * Created by charlag on 21/02/2018.
 */

data class DiscoverMoviesLoadedEvent(val discoverResponse: PagedResponse) : Event

data class FailedToLoadDiscoverEvent(val page: Int, val error: Throwable?) : Event

object LoadMoreDiscoverEvent : DispatchableEvent
object RetryLoadDiscoverEvent : DispatchableEvent
data class OpenMovieDetailsEvent(val id: Long) : DispatchableEvent
data class DiscoverSelectedFilterYear(val year: Int?) : DispatchableEvent

data class DiscoverScreenState(
    val page: Int,
    val movies: List<Movie>,
    val showError: Boolean,
    val isLoading: Boolean,
    val yearFilter: Int? = null
) : ScreenState

/**
 * Return movies which satisfy selected year filter.
 * This is really suboptimal as this should be implemented with some kind of a memoization.
 */
fun DiscoverScreenState.filteredMovies(): List<Movie> {
  if (yearFilter == null) return movies
  val cal = Calendar.getInstance()
  return movies.filter {
    if (it.releaseDate == null) return@filter false
    cal.timeInMillis = it.releaseDate.time
    cal.get(Calendar.YEAR) == yearFilter
  }
}

fun discoverMoviesEpic(api: Api): Epic<State> {
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
              .map { DiscoverMoviesLoadedEvent(it) as Event }
              .onErrorReturn { exception -> FailedToLoadDiscoverEvent(page, exception) }
        }
  }
}

fun discoverScreenReducer(state: DiscoverScreenState, event: Event): DiscoverScreenState {
  return when (event) {
    is DiscoverMoviesLoadedEvent -> state.copy(
        page = event.discoverResponse.page,
        movies = state.movies + event.discoverResponse.results,
        isLoading = false
    )
    LoadMoreDiscoverEvent -> state.copy(
        isLoading = true,
        showError = false
    )
    is FailedToLoadDiscoverEvent -> state.copy(
        isLoading = false,
        showError = true
    )
    is DiscoverSelectedFilterYear -> state.copy(yearFilter = event.year)
    else -> state
  }
}