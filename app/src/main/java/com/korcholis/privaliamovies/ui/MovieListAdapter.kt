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

class MovieListAdapter(private val context: Context) : RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>() {

    private var movieList: List<Movie> = listOf()

    fun swap(movies: List<Movie>) {
        movieList = movies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MovieViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false)
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movieList[position]
        holder.title.text = movie.title
        holder.synopsis.text = movie.synopsis
        holder.releaseYear.text = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(movie.releaseDate).year.toString()
        Picasso.with(context).load(movie.getPosterPath()).into(holder.poster)
    }

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val poster : AppCompatImageView = view.poster
        val title : AppCompatTextView = view.title
        val synopsis : AppCompatTextView = view.synopsis
        val releaseYear : AppCompatTextView = view.releaseYear
    }
}