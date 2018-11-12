package com.korcholis.privaliamovies

import android.os.AsyncTask
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.action.GeneralLocation
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.korcholis.privaliamovies.screens.MovieListTestScreen
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieListActivityEspressoTest {
    @JvmField
    @Rule
    val testRule = ActivityTestRule<MovieListActivity>(MovieListActivity::class.java)

    val appScreen = MovieListTestScreen()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            RxJavaPlugins.setIoSchedulerHandler {
                Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            RxJavaPlugins.reset()
        }
    }

    @Test
    fun appOpens() {
        appScreen {
            root {
                isVisible()
            }
        }
    }

    @Test
    fun moviesLoad() {
        appScreen {
            movieList {
                hasSize(21)

                firstChild<MovieListTestScreen.MovieItem> {
                    isVisible()
                }
            }
        }
    }

    @Test
    fun searchShowsUpAndWorks() {
        appScreen {
            searchView {
                isInvisible()
            }
        }

        appScreen {
            openSearchBtn {
                click(GeneralLocation.CENTER)
            }

            searchView {
                isVisible()
            }
        }

        appScreen {
            searchEditText {
                typeText("venom")
            }

            movieList {
                firstChild<MovieListTestScreen.MovieItem> {
                    isVisible()
                }
            }
        }
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.korcholis.privaliamovies", appContext.packageName)
    }
}
