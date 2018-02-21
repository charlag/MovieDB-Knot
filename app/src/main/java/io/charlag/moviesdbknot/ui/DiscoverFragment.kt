package io.charlag.moviesdbknot.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.charlag.moviesdbknot.R
import io.charlag.moviesdbknot.R.layout
import io.charlag.moviesdbknot.data.models.Configuration
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.di.Injectable
import io.charlag.moviesdbknot.logic.DiscoverScreenState
import io.charlag.moviesdbknot.logic.DiscoverSelectedFilterYear
import io.charlag.moviesdbknot.logic.DispatchableEvent
import io.charlag.moviesdbknot.logic.LoadMoreDiscoverEvent
import io.charlag.moviesdbknot.logic.OpenMovieDetailsEvent
import io.charlag.moviesdbknot.logic.RetryLoadDiscoverEvent
import io.charlag.moviesdbknot.logic.Store
import io.charlag.moviesdbknot.logic.filteredMovies
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.FooterState.Error
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.FooterState.Loading
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.FooterState.None
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.Item.FooterItem
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.Item.MovieItem
import io.charlag.moviesdbknot.ui.DiscoverFragment.MoviesAdapter.Listener
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

    toolbar.inflateMenu(R.menu.discover)
    toolbar.setOnMenuItemClickListener {
      showYearPicker()
      true
    }

    val layoutManager = LinearLayoutManager(context)
    rvMovies.layoutManager = layoutManager
    rvMovies.adapter = adapter

    rvMovies.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
      override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
        store.dispatch(LoadMoreDiscoverEvent)
      }
    })

    adapter.listener = object : Listener {
      override fun onMovieSelected(id: Long) {
        dispatch(OpenMovieDetailsEvent(id))
      }

      override fun onTryAgain() {
        dispatch(RetryLoadDiscoverEvent)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    store.state.observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scope())
        .subscribe { state ->
          val screenState = state.screens.last()
          if (screenState is DiscoverScreenState) {
            val footerState = when {
              screenState.isLoading -> Loading
              screenState.showError -> Error
              else -> None
            }
            adapter.update(screenState.filteredMovies(), state.config?.imagesConfig, footerState)
          }
        }
  }

  private fun dispatch(event: DispatchableEvent) {
    store.dispatch(event)
  }

  private fun showYearPicker() {
    val minYear = 1900
    val years = (minYear..2100)
    val yearStrings = years.map { it.toString() }.toTypedArray()
    AlertDialog.Builder(context!!)
        .setItems(yearStrings, { _, which ->
          val year = minYear + which
          dispatch(DiscoverSelectedFilterYear(year))
        })
        .setNeutralButton("None", { _, _ -> dispatch(DiscoverSelectedFilterYear(null)) })
        .show()
  }

  class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    var listener: Listener? = null

    enum class FooterState {
      None, Loading, Error
    }

    interface Listener {
      fun onMovieSelected(id: Long)
      fun onTryAgain()
    }

    fun update(movies: List<Movie>, imagesConfig: Configuration.Images?, footerState: FooterState) {
      this.config = imagesConfig
      this.footerState = footerState
      this.items.clear()
      this.items.addAll(movies.map(::MovieItem) + FooterItem(footerState))
      this.notifyDataSetChanged()
    }

    init {
      setHasStableIds(true)
    }

    private companion object {
      private const val MOVIE_TYPE = 0
      private const val FOOTER_TYPE = 1
    }

    private sealed class Item {
      data class MovieItem(val movie: Movie) : Item() {
        override val itemType: Int = MOVIE_TYPE
        override val itemId: Long = movie.id
      }

      data class FooterItem(val footerState: FooterState) : Item() {
        override val itemType: Int = FOOTER_TYPE
        override val itemId: Long = -1
      }

      abstract val itemType: Int
      abstract val itemId: Long
    }

    private val items = mutableListOf<Item>()
    private var config: Configuration.Images? = null
    private var footerState = FooterState.None

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return LayoutInflater.from(parent.context).run {
        when (viewType) {
          MOVIE_TYPE -> inflate(R.layout.item_movie, parent, false).let(::MovieViewHolder)
          FOOTER_TYPE -> inflate(R.layout.item_footer, parent, false).let(::FooterViewHolder)
          else -> throw AssertionError("Unknown item type: $viewType")
        }
      }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].itemId

    override fun getItemViewType(position: Int): Int = items[position].itemType

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      when (holder) {
        is MovieViewHolder -> holder.bindItem(items[position] as MovieItem)
        is FooterViewHolder -> holder.bindItem(items[position] as FooterItem)
      }
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private inner class MovieViewHolder(itemView: View) :
        ViewHolder(itemView),
        View.OnClickListener {
      val movieImage: ImageView = itemView.findViewById(R.id.ivMovie)
      val movieTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)

      init {
        itemView.setOnClickListener(this)
      }

      fun bindItem(item: MovieItem) {
        val movie = item.movie
        movieTitle.text = movie.title
        config?.let { c ->
          if (movie.posterPath == null) {
            movieImage.setImageDrawable(null)
          } else {
            val url = buildImageUrl(c.posterSizes, c.secureBaseUrl, "w780", movie.posterPath)
            Picasso.with(movieImage.context).load(url).into(movieImage)
          }
        }
      }

      override fun onClick(v: View) {
        listener?.onMovieSelected(items[adapterPosition].itemId)
      }
    }

    private inner class FooterViewHolder(itemView: View) :
        ViewHolder(itemView),
        View.OnClickListener {
      val progressBar: ProgressBar = itemView.findViewById(R.id.progress)
      val tryAgainButton: TextView = itemView.findViewById(R.id.btnTryAgain)
      var state = FooterState.None

      init {
        itemView.setOnClickListener(this)
      }

      fun bindItem(item: FooterItem) {
        val value = item.footerState
        this.state = value
        progressBar.visibility = if (value == Loading) View.VISIBLE else View.GONE
        tryAgainButton.visibility = if (value == Error) View.VISIBLE else View.GONE
      }

      override fun onClick(v: View?) {
        if (state == Error) {
          listener?.onTryAgain()
        }
      }

    }
  }
}