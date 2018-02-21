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
 * Dispatched when movies were successfully loaded.
 */
data class DiscoverMoviesLoadedEvent(val discoverResponse: PagedResponse) : Event

/**
 * Dispatched when error occurred during movie load.
 */
data class FailedToLoadDiscoverEvent(val page: Int, val error: Throwable?) : Event

/**
 * When more movies should be loaded.
 */
object LoadMoreDiscoverEvent : DispatchableEvent

/**
 * When user presses 'try again' button to try to load movies again.
 */
object RetryLoadDiscoverEvent : DispatchableEvent

/**
 * When movie is selected.
 */
data class OpenMovieDetailsEvent(val id: Long) : DispatchableEvent

/**
 * When year filter is selected. Will filter movies according to the selected year.
 */
data class DiscoverSelectedFilterYear(val year: Int?) : DispatchableEvent

/**
 * Current state of the Discover Movies screen.
 * @property page Current page in the movies list (defined by the server)
 * @property movies List of all loaded movies
 * @property showError If error occurred during last request
 * @property isLoading If request is in process
 * @property yearFilter Current selected year to filter movies
 */
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

/**
 * Epic which loads next page of movies on [InitEvent], [LoadMoreDiscoverEvent] or
 * [RetryLoadDiscoverEvent]. Dispatches [DiscoverMoviesLoadedEvent] on success or
 * [FailedToLoadDiscoverEvent] on failure.
 */
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