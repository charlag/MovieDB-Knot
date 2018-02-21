package io.charlag.moviesdbknot.logic

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.data.models.PagedResponse
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.StoreImpl.InitEvent
import io.charlag.redukt.Event
import io.charlag.redukt.EventBundle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.schedulers.ImmediateThinScheduler
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Created by charlag on 21/02/2018.
 */

class DiscoverLogicTest {

  @Before
  fun before() {
    RxJavaPlugins.setIoSchedulerHandler { ImmediateThinScheduler.INSTANCE }
  }

  @Test
  fun testLoadDiscoverOnInitEvent() {
    val state = State(
        config = null,
        screens = listOf(DiscoverScreenState(1, listOf(), isLoading = true, showError = false))
    )
    val response = PagedResponse(listOf(), 2, 2, 100)
    val apiMock = mock<Api> {
      on { discoverMovies(any()) } doReturn Single.just(response)
    }
    val epic = discoverMoviesEpic(apiMock)
    val testObserver = TestObserver<Event>()
    val upstream = Observable.just(EventBundle(InitEvent, state, state)).publish()

    epic(upstream).subscribe(testObserver)
    upstream.connect()

    testObserver.assertNoErrors()
    testObserver.assertValue(DiscoverMoviesLoadedEvent(response))
  }

  @Test
  fun testLoadFailOnInitEvent() {
    val state = State(
        config = null,
        screens = listOf(DiscoverScreenState(1, listOf(), isLoading = true, showError = false))
    )
    val exception = IOException()
    val apiMock = mock<Api> {
      on { discoverMovies(any()) } doReturn Single.error(exception)
    }
    val epic = discoverMoviesEpic(apiMock)
    val testObserver = TestObserver<Event>()
    val upstream = Observable.just(EventBundle(InitEvent, state, state)).publish()

    epic(upstream).subscribe(testObserver)
    upstream.connect()

    testObserver.assertNoErrors()
    testObserver.assertValue(FailedToLoadDiscoverEvent(2, exception))
  }

  @Test
  fun discoverScreenReducerOnDiscoverLoaded() {
    val state = DiscoverScreenState(1, createMovies(1..10L),
        isLoading = true,
        showError = false
    )
    val event = DiscoverMoviesLoadedEvent(PagedResponse(createMovies(11..21L), 2, 10, 100))

    val result = discoverScreenReducer(state, event)
    Assert.assertEquals(DiscoverScreenState(
        page = event.discoverResponse.page,
        movies = state.movies + event.discoverResponse.results,
        isLoading = false,
        showError = false
    ), result)
  }

  @Test
  fun discoverScreenReducerOnDiscoverFailed() {
    val state = DiscoverScreenState(1, createMovies(1..10L),
        isLoading = true,
        showError = false
    )
    val event = FailedToLoadDiscoverEvent(page = 2, error = null)

    val result = discoverScreenReducer(state, event)

    Assert.assertEquals(DiscoverScreenState(
        page = state.page,
        movies = state.movies,
        isLoading = false,
        showError = true
    ), result)
  }

  @Test
  fun discoverScreenReducerOnLoadMoreStarted() {
    val state = DiscoverScreenState(1, createMovies(1..10L),
        isLoading = true,
        showError = false
    )
    val event = LoadMoreDiscoverEvent

    val result = discoverScreenReducer(state, event)
    Assert.assertEquals(
        DiscoverScreenState(
            page = state.page,
            movies = state.movies,
            isLoading = true,
            showError = false
        ),
        result)
  }

  private fun sampleMovie(id: Long): Movie {
    return Movie(id, "Movie$id", id.toFloat(), "MovieOverview$id", null, null, null)
  }

  private fun createMovies(range: LongRange): List<Movie> = range.map(::sampleMovie)
}