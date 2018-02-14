package io.charlag.moviesdbknot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.charlag.moviesdbknot.data.models.Movie
import io.charlag.moviesdbknot.logic.StoreImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class MainActivity : AppCompatActivity() {

    val store = StoreImpl()
    val adapter = MoviesAdapter()

    val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rvMovies)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        store.state.observeOn(AndroidSchedulers.mainThread())
                .subscribe {state ->
                    adapter.update(state.mainPageState.movies)
                }
                .addTo(disposable)
    }

    class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

        fun update(movies: List<Movie>) {
            this.movies.clear()
            this.movies.addAll(movies)
            this.notifyDataSetChanged()
        }

        private val movies = mutableListOf<Movie>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = TextView(parent.context)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = movies.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.titleTextView.text = movies[position].title
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleTextView: TextView = itemView as TextView
        }
    }
}
