package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.ui.Navigator
import io.charlag.redukt.Event
import io.charlag.redukt.createKnot
import io.charlag.redukt.epicOf
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

object BackPressedEvent : DispatchableEvent

object FinishAppEvent : Event

data class NavigateEvent(val key: Navigator.Key, val forward: Boolean) : Event

data class State(
    val config: Configuration?,
    val screens: List<ScreenState>
) {
  interface ScreenState
}

class StoreImpl(api: Api) : Store {

  override val events: Observable<Event>
  override val state: Observable<State>

  override fun dispatch(event: DispatchableEvent) {
    dispatchedEvents.onNext(event)
  }

  private val dispatchedEvents = PublishSubject.create<DispatchableEvent>()

  object InitEvent : Event

  fun reducer(state: State, event: Event): State {
    return state.copy(
        config = configReducer(state.config, event),
        screens = screensReducer(state.screens, event)
    )
  }

  init {
    val rootEpic = epicOf(
        discoverMoviesEpic(api),
        configurationEpic(api),
        navigateEpic,
        loadMovieDetailsEpic(api),
        finishAppEpic
    )

    val initialState = State(
        config = null,
        screens = listOf(DiscoverScreenState(0, listOf(), showError = false, isLoading = true))
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