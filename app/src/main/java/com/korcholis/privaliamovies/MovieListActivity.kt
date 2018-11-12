package com.korcholis.privaliamovies

import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.ferfalk.simplesearchview.SimpleSearchView
import com.korcholis.privaliamovies.data.TMDbApi
import com.korcholis.privaliamovies.exceptions.ConnectionNotAvailableException
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

    companion object {
        private const val BUNDLE_LIST_STATE = "listState"
        private const val BUNDLE_LIST_CONTENT = "movieList"
        private const val BUNDLE_QUERY = "currentSearch"
        private const val BUNDLE_PAGE = "loadedUpTo"
    }

    private var loadedUpTo: Int = 0
    private var currentSearch: String = ""
    private var listState: Parcelable? = null

    private var moviesAdapter: MovieListAdapter = MovieListAdapter(this)
    private val disposables = CompositeDisposable()

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    private var errorSnackbar: Snackbar? = null
    private var noConnSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        setSupportActionBar(toolbar)

        title = resources.getString(R.string.app_name)

        movieList.adapter = moviesAdapter

        movieList.layoutManager = LinearLayoutManager(this)

        savedInstanceState?.let {
            moviesAdapter.swap(savedInstanceState.getParcelableArrayList(BUNDLE_LIST_CONTENT))
            listState = savedInstanceState.getParcelable(BUNDLE_LIST_STATE)
            movieList.layoutManager!!.onRestoreInstanceState(listState)
            loadedUpTo = savedInstanceState.getInt(BUNDLE_PAGE)
            currentSearch = savedInstanceState.getString(BUNDLE_QUERY)
        }

        scrollListener = object : EndlessRecyclerViewScrollListener(movieList.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                Log.i("PAGES", "Listener: $page $loadedUpTo $totalItemsCount")
                getMoreMovies(currentSearch, page)
            }
        }

        movieList.addOnScrollListener(scrollListener)

        if (loadedUpTo != 0) {
            scrollListener.restoreInPosition(loadedUpTo)
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(BUNDLE_LIST_STATE, movieList.layoutManager?.onSaveInstanceState())
        outState.putParcelableArrayList(BUNDLE_LIST_CONTENT, moviesAdapter.getList())
        outState.putString(BUNDLE_QUERY, currentSearch)
        outState.putInt(BUNDLE_PAGE, loadedUpTo)
    }

    private fun showErrorSnack() {
        errorSnackbar = Snackbar.make(
                root,
                "You can't get more movies for now.",
                Snackbar.LENGTH_INDEFINITE
        ).setAction("Try again") {
            getMoreMovies(currentSearch, loadedUpTo, forceRefresh = true)
        }

        errorSnackbar!!.show()
    }

    private fun showNoConnSnack() {
        noConnSnackbar = Snackbar.make(
                root,
                "You are not connected to the internet.",
                Snackbar.LENGTH_INDEFINITE
        ).setAction("Try again") {
            getMoreMovies(currentSearch, loadedUpTo, forceRefresh = true)
        }

        noConnSnackbar!!.show()
    }

    private fun getMoreMovies(query: String = "", page: Int = 1, forceRefresh: Boolean = false) {
        if (query != currentSearch) {
            loadedUpTo = 1
            currentSearch = query
            resetList(listOf())
        } else if (page <= loadedUpTo && !forceRefresh) {
            return
        }

        Log.i("PAGES", "Function: $page $loadedUpTo")

        disposables.clear()

        if (query.isEmpty()) {
            disposables.add(
                    TMDbApi.instance(this)
                            .movieList(page)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                if (it is ConnectionNotAvailableException) {
                                    showNoConnSnack()
                                } else {
                                    showErrorSnack()
                                }
                            }
                            .onErrorReturn {
                                MovieList(listOf(), loadedUpTo)
                            }
                            .subscribe { response: MovieList, issue ->
                                if (issue != null) {
                                    if (issue is ConnectionNotAvailableException) {
                                        showNoConnSnack()
                                    } else {
                                        showErrorSnack()
                                    }
                                    return@subscribe
                                }

                                loadedUpTo = page

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
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                if (it is ConnectionNotAvailableException) {
                                    showNoConnSnack()
                                } else {
                                    showErrorSnack()
                                }
                            }
                            .onErrorReturn {
                                MovieList(listOf(), loadedUpTo)
                            }
                            .subscribe { response: MovieList, issue ->
                                if (issue != null) {
                                    if (issue is ConnectionNotAvailableException) {
                                        showNoConnSnack()
                                    } else {
                                        showErrorSnack()
                                    }
                                    return@subscribe
                                }

                                loadedUpTo = page

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
