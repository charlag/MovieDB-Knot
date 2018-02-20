package io.charlag.moviesdbknot.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.R
import io.charlag.moviesdbknot.R.layout
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.DispatchableEvent
import io.charlag.moviesdbknot.logic.LoadMoreDiscoverEvent
import io.charlag.moviesdbknot.logic.OpenMovieDetailsEvent
import io.charlag.moviesdbknot.logic.State.DiscoverScreenState
import io.charlag.moviesdbknot.logic.Store
import io.charlag.moviesdbknot.ui.lib.EndlessRecyclerViewScrollListener
import io.charlag.moviesdbknot.ui.lib.buildImageUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_discover.rvMovies
import kotlinx.android.synthetic.main.layout_discover.toolbar
import javax.inject.Inject


/**
 * Created by charlag on 18/02/18.
 */

class DiscoverFragment : Fragment(), Injectable {

  @Inject
  lateinit var store: Store

  private val adapter = MoviesAdapter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(layout.layout_discover, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.title = "Knot Sample"

    val layoutManager = LinearLayoutManager(context)
    rvMovies.layoutManager = layoutManager
    rvMovies.adapter = adapter

    rvMovies.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
      override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
        store.dispatch(LoadMoreDiscoverEvent)
      }
    })

    adapter.listener = { id ->
      dispatch(OpenMovieDetailsEvent(id))
    }
  }

  override fun onStart() {
    super.onStart()
    store.state.observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scope())
        .subscribe { state ->
          val screenState = state.screens.last()
          if (screenState is DiscoverScreenState) {
            adapter.update(screenState.movies, state.config?.imagesConfig)
          }
        }
  }

  private fun dispatch(event: DispatchableEvent) {
    store.dispatch(event)
  }

  class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    var listener: ((Long) -> Unit)? = null

    fun update(movies: List<Movie>, imagesConfig: Configuration.Images?) {
      this.movies.clear()
      this.movies.addAll(movies)
      this.config = imagesConfig
      this.notifyDataSetChanged()
    }

    private var config: Configuration.Images? = null
    private val movies = mutableListOf<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
          .let(::ViewHolder)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = movies[position]
      holder.movieTitle.text = item.title
      config?.let { c ->
        if (item.posterPath == null) {
          holder.movieImage.setImageDrawable(null)
        } else {
          val url = buildImageUrl(c.posterSizes, c.secureBaseUrl, "w780", item.posterPath)
          Picasso.with(holder.movieImage.context).load(url).into(holder.movieImage)
        }
      }
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
      val movieImage: ImageView = itemView.findViewById(R.id.ivMovie)
      val movieTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)

      init {
        itemView.setOnClickListener(this)
      }

      override fun onClick(v: View) {
        listener?.invoke(movies[adapterPosition].id)
      }
    }
  }
}