package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.redukt.Epic
import io.charlag.redukt.Event
import io.charlag.redukt.ofEventType

/**
 * Dispatched when movie details have been loaded.
 */
data class DidLoadMovieEvent(val movie: Movie) : Event

/**
 * Dispatched when movie error occurred during loading of movie details.
 */
data class FailedToLoadMovieEvent(val id: Long) : Event

/**
 * Current state of details screen.
 * @property id Movie id
 * @property movie Loaded movie
 */
data class DetailsScreenState(val id: Long, val movie: Movie?) : ScreenState

/**
 * Epic which loads movie on [OpenMovieDetailsEvent]. Disptaches [DidLoadMovieEvent] on success or
 * [FailedToLoadDiscoverEvent] on failure.
 */
fun loadMovieDetailsEpic(api: Api): Epic<State> {
  return { upstream ->
    upstream.ofEventType(OpenMovieDetailsEvent::class.java)
        .flatMapSingle { (event, _, _) ->
          api.getMovie(event.id)
              .map { DidLoadMovieEvent(it) as Event }
              .onErrorReturn { FailedToLoadMovieEvent(event.id) }
        }
  }
}

fun detailsScreenReducer(state: DetailsScreenState, event: Event): DetailsScreenState {
  return when {
    event is DidLoadMovieEvent && state.id == event.movie.id -> state.copy(movie = event.movie)
    else -> state
  }
}
