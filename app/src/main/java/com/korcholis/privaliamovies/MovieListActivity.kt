package com.korcholis.privaliamovies

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.korcholis.privaliamovies.data.TMDbApi
import com.korcholis.privaliamovies.models.MovieList
import com.korcholis.privaliamovies.ui.EndlessRecyclerViewScrollListener
import com.korcholis.privaliamovies.ui.MovieListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.movie_list_content.*

class MovieListActivity : AppCompatActivity() {

    private var loadedUpTo: Int = 0

    private var moviesAdapter: MovieListAdapter = MovieListAdapter(this)
    private val disposables = CompositeDisposable()
    private var layoutManager: LinearLayoutManager = LinearLayoutManager(this)
    private val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
            getMoreMovies(page)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)

        movieList.adapter = moviesAdapter
        movieList.layoutManager = layoutManager
        movieList.addOnScrollListener(scrollListener)
    }

    private fun getMoreMovies(page: Int = 1) {
        if (page <= loadedUpTo) return

        disposables.add(
                TMDbApi.instance(this)
                        .movieList(page)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError {
                            it.localizedMessage
                        }
                        .subscribe { response: MovieList, issue ->
                            loadedUpTo = page

                            if (issue != null) {
                                issue.localizedMessage
                            }
                            if (loadedUpTo == 1) {
                                resetList(response)
                            } else {
                                updateList(response)
                            }
                        }
        )
    }

    private fun resetList(response: MovieList) {
        moviesAdapter.swap(response.results)
    }

    private fun updateList(response: MovieList) {
        moviesAdapter.add(response.results)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
