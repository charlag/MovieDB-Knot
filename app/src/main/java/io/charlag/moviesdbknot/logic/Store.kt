package io.charlag.moviesdbknot.logic

import android.util.Log
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.State.DetailsScreenState
import io.charlag.moviesdbknot.logic.State.DiscoverScreenState
import io.charlag.moviesdbknot.logic.State.ScreenState
import io.charlag.moviesdbknot.ui.Navigator
import io.charlag.redukt.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by charlag on 14/02/18.
 */

interface Store {
  val events: Observable<Event>
  fun dispatch(event: DispatchableEvent)
  val state: Observable<State>
}

interface DispatchableEvent : Event
object LoadMoreDiscoverEvent : DispatchableEvent
object RetryLoadDiscoverEvent : DispatchableEvent
data class OpenMovieDetailsEvent(val id: String) : DispatchableEvent

data class FailedToLoadDiscoverEvent(val page: Int) : Event

data class NavigateEvent(val key: Navigator.Key, val forward: Boolean) : Event

data class State(
    val config: Configuration?,
    val screens: List<ScreenState>
) {
  interface ScreenState
  data class DiscoverScreenState(val page: Int, val movies: List<Movie>) : ScreenState
  data class DetailsScreenState(val id: String, val movie: Movie?) : ScreenState
}

class StoreImpl(private val api: Api) : Store {

  override val events: Observable<Event>
  override val state: Observable<State>

  override fun dispatch(event: DispatchableEvent) {
    dispatchedEvents.onNext(event)
  }

  private val dispatchedEvents = PublishSubject.create<DispatchableEvent>()

  private object InitEvent : Event
  private data class ConfigLoadedEvent(val config: Configuration) : Event
  private data class DiscoverLoadedEvent(val discoverResponse: PagedResponse) : Event


  val configurationEpic: Epic<State> = { upstream ->
    upstream.ofEventType(InitEvent::class.java)
        .switchMapSingle {
          api.configuration()
        }
        .map(::ConfigLoadedEvent)
  }

  val upcomingEpic: Epic<State> = { upstream ->
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
        .doOnNext { event -> Log.d("Store", event.toString()) }
  }

  private fun screenToKey(screen: ScreenState): Navigator.Key {
    return when (screen) {
      is DiscoverScreenState -> Navigator.Key.DiscoverKey
      is DetailsScreenState -> Navigator.Key.MovieDetailsKey
      else -> throw AssertionError("Unknown screen state")
    }
  }

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

  fun reducer(state: State, event: Event): State {
    return state.copy(
        config = configReducer(state.config, event),
        screens = screensReducer(state.screens, event)
    )
  }

  fun discoveryScreenReducer(state: DiscoverScreenState, event: Event): DiscoverScreenState {
    return when (event) {
      is DiscoverLoadedEvent -> state.copy(
          page = event.discoverResponse.page,
          movies = state.movies + event.discoverResponse.results
      )
      else -> state
    }
  }

  fun detailsScreenReducer(state: DetailsScreenState, event: Event): DetailsScreenState {
    // TODO
    return state
  }

  fun mapScreens(state: List<ScreenState>, event: Event): List<ScreenState> {
    return state.map { page ->
      when (page) {
        is DiscoverScreenState -> discoveryScreenReducer(page, event)
        is DetailsScreenState -> detailsScreenReducer(page, event)
        else -> page
      }
    }
  }

  fun screensReducer(state: List<ScreenState>, event: Event): List<ScreenState> {
    return when (event) {
      is OpenMovieDetailsEvent -> state + DetailsScreenState(event.id, null)
      else -> mapScreens(state, event)
    }
  }

  fun configReducer(config: Configuration?, event: Event): Configuration? {
    return (event as? ConfigLoadedEvent)?.config ?: config
  }

  init {
    val rootEpic = epicOf(upcomingEpic, configurationEpic, navigateEpic)

    val initialState = State(
        config = null,
        screens = listOf(DiscoverScreenState(0, listOf()))
    )

    val externalEvents: Observable<Event> = dispatchedEvents.cast(Event::class.java)
        .startWith(InitEvent)

    val (eventsKnot, state) = createKnot(
        initial = initialState,
        eventsSource = externalEvents,
        reducer = this::reducer,
        rootEpic = rootEpic
    )

    this.state = state
    events = eventsKnot

    eventsKnot.connect()
  }

}