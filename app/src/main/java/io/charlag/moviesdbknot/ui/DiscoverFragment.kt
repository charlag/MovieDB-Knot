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
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.R
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.State.DiscoverScreenState
import io.charlag.moviesdbknot.logic.Store
import io.reactivex.android.schedulers.AndroidSchedulers
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
    val view = inflater.inflate(R.layout.layout_discover, container, false)

    val recyclerView = view.findViewById<RecyclerView>(R.id.rvMovies)
    recyclerView.layoutManager = LinearLayoutManager(context!!)
    recyclerView.adapter = adapter

    return view
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

  class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

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
          val url = buildImageUrl(c.secureBaseUrl, c.posterSizes, item.posterPath)
          Picasso.with(holder.movieImage.context).load(url).into(holder.movieImage)
        }
      }
    }

    private fun buildImageUrl(baseUrl: String, sizes: List<String>, filepath: String): String {
      return baseUrl + preferredSize(sizes) + filepath
    }

    private fun preferredSize(sizes: List<String>): String {
      val preferable = "w780"
      return if (sizes.contains(preferable)) preferable else sizes[sizes.size - 1]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val movieImage: ImageView = itemView.findViewById(R.id.ivMovie)
      val movieTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)
    }
  }
}