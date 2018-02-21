package io.charlag.moviesdbknot.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.R
import io.charlag.moviesdbknot.R.layout
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.BackPressedEvent
import io.charlag.moviesdbknot.logic.DetailsScreenState
import io.charlag.moviesdbknot.logic.State
import io.charlag.moviesdbknot.logic.Store
import io.charlag.moviesdbknot.ui.lib.buildImageUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_movie_details.ivBackground
import kotlinx.android.synthetic.main.layout_movie_details.ivCover
import kotlinx.android.synthetic.main.layout_movie_details.toolbar
import kotlinx.android.synthetic.main.layout_movie_details.tvOverview
import kotlinx.android.synthetic.main.layout_movie_details.tvTitle
import kotlinx.android.synthetic.main.layout_movie_details.tvYear
import java.text.SimpleDateFormat
import javax.inject.Inject

/**
 * Created by charlag on 18/02/18.
 */
class MovieDetailsFragment : Fragment(), Injectable {

  @Inject
  lateinit var store: Store

  private val dateFormat = SimpleDateFormat.getDateInstance()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(layout.layout_movie_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener {
      store.dispatch(BackPressedEvent)
    }
    val icon = ContextCompat.getDrawable(context!!, R.drawable.ic_arrow_back_black_24dp)!!.apply {
      mutate()
      setTint(Color.WHITE)
    }
    toolbar.navigationIcon = icon
  }

  override fun onStart() {
    super.onStart()
    store.state.observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scope())
        .subscribe(this::setState)
  }

  private fun setState(state: State) {
    val screenState = state.screens.lastOrNull { it is DetailsScreenState } as? DetailsScreenState
        ?: return
    val movie = screenState.movie
    tvTitle.text = movie?.title
    tvYear.text = movie?.releaseDate?.let(dateFormat::format)
    tvOverview.text = movie?.overview
    setPoster(movie?.posterPath, state.config?.imagesConfig)
    setBackdrop(movie?.backdropPath, state.config?.imagesConfig)
  }

  private fun setPoster(path: String?, config: Configuration.Images?) {
    if (path != null && config != null) {
      val url = buildImageUrl(config.posterSizes, config.secureBaseUrl, null, path)
      Picasso.with(context)
          .load(url)
          .into(ivCover)
    }
  }

  private fun setBackdrop(path: String?, config: Configuration.Images?) {
    if (path != null && config != null) {
      val url = buildImageUrl(config.backdropSizes, config.secureBaseUrl, null, path)
      Picasso.with(context)
          .load(url)
          .into(ivBackground)
    }
  }
}