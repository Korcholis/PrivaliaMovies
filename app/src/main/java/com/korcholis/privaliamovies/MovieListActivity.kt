package com.korcholis.privaliamovies

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.ferfalk.simplesearchview.SimpleSearchView
import com.korcholis.privaliamovies.data.TMDbApi
import com.korcholis.privaliamovies.models.Movie
import com.korcholis.privaliamovies.models.MovieList
import com.korcholis.privaliamovies.ui.EndlessRecyclerViewScrollListener
import com.korcholis.privaliamovies.ui.MovieListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_movie_list.*
import kotlinx.android.synthetic.main.movie_list_content.*

class MovieListActivity : AppCompatActivity() {

    private var loadedUpTo: Int = 0
    private var currentSearch: String = ""

    private var moviesAdapter: MovieListAdapter = MovieListAdapter(this)
    private val disposables = CompositeDisposable()
    private var layoutManager: LinearLayoutManager = LinearLayoutManager(this)
    private val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
            getMoreMovies(currentSearch, page)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        setSupportActionBar(toolbar)

        title = resources.getString(R.string.app_name)

        movieList.adapter = moviesAdapter
        movieList.layoutManager = layoutManager
        movieList.addOnScrollListener(scrollListener)

        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (newText.length > 3 || newText.isEmpty()) {
                        getMoreMovies(newText)
                    }
                }

                return true
            }

            override fun onQueryTextCleared(): Boolean {
                getMoreMovies()

                return true
            }

        })
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.actionOpenSearch -> {
                searchView.showSearch()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getMoreMovies(query: String = "", page: Int = 1) {
        if (query != currentSearch) {
            loadedUpTo = 1
            currentSearch = query
            resetList(mutableListOf())
        } else if (page <= loadedUpTo) {
            return
        }

        disposables.clear()

        if (query.isEmpty()) {
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
                                    resetList(response.results)
                                } else {
                                    updateList(response.results)
                                }
                            }
            )
        } else {
            disposables.add(
                    TMDbApi.instance(this)
                            .search(query, page)
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
                                    resetList(response.results)
                                } else {
                                    updateList(response.results)
                                }
                            }
            )
        }
    }

    private fun resetList(movies: List<Movie>) {
        moviesAdapter.swap(movies)
    }

    private fun updateList(movies: List<Movie>) {
        moviesAdapter.add(movies)
    }
}
