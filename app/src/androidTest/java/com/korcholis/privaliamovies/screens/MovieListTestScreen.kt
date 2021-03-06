package com.korcholis.privaliamovies.screens

import android.view.View
import com.agoda.kakao.*
import com.korcholis.privaliamovies.R
import org.hamcrest.Matcher

class MovieListTestScreen : Screen<MovieListTestScreen>() {
    val root = KView { withId(R.id.root) }
    val searchView = KView { withId(R.id.searchView) }
    val searchEditText = KEditText { withId(R.id.searchEditText) }
    val openSearchBtn = KView { withId(R.id.actionOpenSearch) }
    val movieList = KRecyclerView({
        withId(R.id.movieList)
    }, itemTypeBuilder = {
        itemType(::MovieItem)
        itemType(::LoadingItem)
    })
    val emptyList = KView { withId(R.id.emptyList) }

    class MovieItem(parent: Matcher<View>) : KRecyclerItem<MovieItem>(parent) {
        val poster = KImageView { withId(R.id.poster) }
        val title = KTextView { withId(R.id.title) }
        val synopsis = KTextView { withId(R.id.synopsis) }
        val releaseYear = KTextView { withId(R.id.releaseYear) }
    }

    class LoadingItem(parent: Matcher<View>) : KRecyclerItem<LoadingItem>(parent) {
        val loadingPb = KProgressBar { withId(R.id.loadingPb) }
        val loadingText = KTextView { withId(R.id.loadingText) }
    }
}