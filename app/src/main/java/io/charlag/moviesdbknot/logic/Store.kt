package io.charlag.moviesdbknot.logic

import android.util.Log
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.charlag.moviesdbknot.data.network.ApiCreator
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

data class State(
        val mainPageState: MainPageState
)

data class MainPageState(val page: Long, val movies: List<Movie>)

class StoreImpl : Store {

    override val events: Observable<Event>
    override val state: Observable<State>

    override fun dispatch(event: DispatchableEvent) {
        dispatchedEvents.onNext(event)
    }

    private val dispatchedEvents = PublishSubject.create<DispatchableEvent>()

    private data class DiscoverLoadedEvent(val discoverResponse: PagedResponse) : Event

    val api = ApiCreator().createApi()

    val upcomingEpic: Epic<State> = { upstream ->
        Observable.just("Meh")
                .switchMap {
                    api.discoverMovies().toObservable()
                }
                .map(::DiscoverLoadedEvent)
                .doOnNext { event -> Log.d("Store", event.toString()) }
    }

    fun reducer(state: State, event: Event): State {
        return state.copy(mainPageState = mainPageReducer(state.mainPageState, event))
    }

    fun mainPageReducer(state: MainPageState, event: Event): MainPageState {
        return when (event) {
            is DiscoverLoadedEvent -> state.copy(
                    page = event.discoverResponse.page,
                    movies = state.movies + event.discoverResponse.results
            )
            else -> state
        }
    }

    init {

        val rootEpic = epicOf(upcomingEpic)

        val initialState = State(MainPageState(-1, listOf()))

        val (eventsKnot, state) = createKnot(
                initial = initialState,
                eventsSource = Observable.never(),
                reducer = this::reducer,
                rootEpic = rootEpic
        )

        this.state = state
        events = eventsKnot

        eventsKnot.connect()
    }

}