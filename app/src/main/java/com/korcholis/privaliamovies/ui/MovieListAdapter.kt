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
    }

    private var movieList: MutableList<Movie> = mutableListOf()

    fun add(movies: List<Movie>) {
        movieList.addAll(movies)
        notifyDataSetChanged()
    }

    fun swap(movies: List<Movie>) {
        movieList = movies.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MovieOrLoadingViewHolder {
        if (getItemViewType(position) == CELL_TYPE_MOVIE) {
            val view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false)
            return MovieOrLoadingViewHolder(view, CELL_TYPE_MOVIE)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.loading_movie_item, parent, false)
            return MovieOrLoadingViewHolder(view, CELL_TYPE_LOADING)
        }
    }

    override fun getItemCount(): Int {
        return movieList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= movieList.size)
            CELL_TYPE_LOADING
        else
            CELL_TYPE_MOVIE
    }

    override fun onBindViewHolder(holder: MovieOrLoadingViewHolder, position: Int) {
        if (getItemViewType(position) == CELL_TYPE_LOADING) return

        val movie = movieList[position]
        holder.title?.text = movie.title
        holder.synopsis?.text = movie.synopsis

        val calendar = Calendar.getInstance()
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(movie.releaseDate)

        holder.releaseYear?.text = calendar.get(Calendar.YEAR).toString()
        holder?.poster.let {
            Picasso.with(context).load(movie.getPosterPath()).into(holder.poster)
        }
    }

    class MovieOrLoadingViewHolder : RecyclerView.ViewHolder {
        var poster: AppCompatImageView? = null
        var title: AppCompatTextView? = null
        var synopsis: AppCompatTextView? = null
        var releaseYear: AppCompatTextView? = null

        constructor(view: View, type: Int) : super(view) {
            if (type == CELL_TYPE_MOVIE) {
                poster= view.poster
                title = view.title
                synopsis = view.synopsis
                releaseYear = view.releaseYear
            }
        }
    }
}