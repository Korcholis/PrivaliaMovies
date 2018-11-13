package com.korcholis.privaliamovies.ui

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.korcholis.privaliamovies.R
import com.korcholis.privaliamovies.models.Movie
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.movie_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*


class MovieListAdapter(private val context: Context) : RecyclerView.Adapter<MovieListAdapter.MovieOrLoadingViewHolder>() {

    companion object {
        const val CELL_TYPE_MOVIE = 0
        const val CELL_TYPE_LOADING = 1
        const val CELL_TYPE_NO_MORE_RESULTS = 2
    }

    private var movieList: MutableList<Movie> = mutableListOf()
    private var canLoadMore: Boolean = true

    fun add(movies: List<Movie>) {
        movieList.addAll(movies)
        canLoadMore = movies.isNotEmpty()
        notifyDataSetChanged()
    }

    fun swap(movies: List<Movie>) {
        movieList = movies.toMutableList()
        canLoadMore = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MovieOrLoadingViewHolder {
        return when (getItemViewType(position)) {
            CELL_TYPE_MOVIE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false)
                MovieOrLoadingViewHolder(view, CELL_TYPE_MOVIE)
            }
            CELL_TYPE_LOADING -> {
                val view = LayoutInflater.from(context).inflate(R.layout.loading_movie_item, parent, false)
                MovieOrLoadingViewHolder(view, CELL_TYPE_LOADING)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.no_more_movies_item, parent, false)
                MovieOrLoadingViewHolder(view, CELL_TYPE_NO_MORE_RESULTS)
            }
        }
    }

    override fun getItemCount(): Int {
        return movieList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position >= movieList.size && canLoadMore -> CELL_TYPE_LOADING
            position >= movieList.size && !canLoadMore -> CELL_TYPE_NO_MORE_RESULTS
            else -> CELL_TYPE_MOVIE
        }
    }

    override fun onBindViewHolder(holder: MovieOrLoadingViewHolder, position: Int) {
        if (getItemViewType(position) != CELL_TYPE_MOVIE) return

        val movie = movieList[position]
        holder.title?.text = movie.title
        holder.synopsis?.text = movie.synopsis

        if (movie.releaseDate.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(movie.releaseDate)
            holder.releaseYear?.text = calendar.get(Calendar.YEAR).toString()
        }

        Picasso.with(context).cancelRequest(holder.poster)

        if (movie.poster != null) {
            Picasso.with(context).load(movie.getPosterPath()).placeholder(R.drawable.movie_loading).into(holder.poster)
        } else {
            holder.poster?.setImageResource(R.drawable.movie_error)
        }
    }

    fun getList(): ArrayList<Movie> {
        return movieList as ArrayList<Movie>
    }

    class MovieOrLoadingViewHolder(view: View, type: Int) : RecyclerView.ViewHolder(view) {
        var poster: AppCompatImageView? = null
        var title: AppCompatTextView? = null
        var synopsis: AppCompatTextView? = null
        var releaseYear: AppCompatTextView? = null

        init {
            if (type == CELL_TYPE_MOVIE) {
                poster = view.poster
                title = view.title
                synopsis = view.synopsis
                releaseYear = view.releaseYear
            }
        }
    }
}