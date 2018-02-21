package io.charlag.moviesdbknot.logic

import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.network.Api
import io.charlag.moviesdbknot.logic.StoreImpl.InitEvent
import io.charlag.redukt.Epic
import io.charlag.redukt.Event
import io.charlag.redukt.ofEventType
import io.reactivex.Observable

/**
 * Created by charlag on 21/02/2018.
 */

data class ConfigLoadedEvent(val config: Configuration) : Event

fun configurationEpic(api: Api): Epic<State> = { upstream ->
  upstream.ofEventType(InitEvent::class.java)
      .switchMap {
        api.configuration().retry(2)
            .toObservable()
            .onErrorResumeNext(Observable.never<Configuration>())
      }
      .map(::ConfigLoadedEvent)
}


fun configReducer(config: Configuration?, event: Event): Configuration? {
  return (event as? ConfigLoadedEvent)?.config ?: config
}