package io.charlag.moviesdbknot.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.R
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.Store
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Created by charlag on 18/02/18.
 */
class MovieDetailsFragment : Fragment(), Injectable {

  @Inject
  lateinit var store: Store

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.layout_movie_details, container, false)

    store.state.observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(view.scope())
        .subscribe {
        }
    return view
  }
}