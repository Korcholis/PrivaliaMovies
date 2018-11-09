package com.korcholis.privaliamovies

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.korcholis.privaliamovies.data.TMDbApi
import com.korcholis.privaliamovies.models.MovieList
import com.korcholis.privaliamovies.ui.MovieListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.movie_list_content.*

class MovieListActivity : AppCompatActivity() {

    private lateinit var moviesAdapter: MovieListAdapter
    private val disposables = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)

        moviesAdapter = MovieListAdapter(this)

        movieList.adapter = moviesAdapter

        getMovieList()
    }

    private fun getMovieList() {
        disposables.add(
                TMDbApi.instance(this)
                        .movieList()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { response: MovieList ->
                            updateList(response)
                        }
        )
    }

    private fun updateList(response: MovieList) {
        moviesAdapter.swap(response.results)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}
