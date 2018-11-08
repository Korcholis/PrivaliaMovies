package com.korcholis.privaliamovies.data

import android.content.Context
import com.korcholis.privaliamovies.exceptions.ConnectionNotAvailableException
import com.korcholis.privaliamovies.utils.ConnectionChecker
import com.korcholis.privaliamovies.utils.Constants
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File


object TMDbApi {
    private var instance: Retrofit? = null
    private var context: Context? = null

    fun instance(context: Context): TMDbApiSignature {
        TMDbApi.context = context
        if (instance == null) {
            instance = Retrofit.Builder()
                    .baseUrl(TMDbApiSignature.TMDB_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(createOkHttpClient())
                    .build()
        }

        return instance!!.create(TMDbApiSignature::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val httpCacheDirectory = File(context!!.cacheDir, Constants.CACHE_DIR)
        val cacheSize = 10L * 1024L * 1024L // 10 MiB
        val cache = Cache(httpCacheDirectory, cacheSize)

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val originalHttpUrl = original.url()
            val url = originalHttpUrl.newBuilder()
                    .addQueryParameter(TMDbApiSignature.QUERY_API_KEY, Constants.TMDB_API_KEY)
                    .build()
            val requestBuilder = original.newBuilder()
                    .url(url)
            val request = requestBuilder.build()
            chain.proceed(request)
        }.addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            if (context != null && ConnectionChecker.isNetworkAvailable(context!!)) {
                val maxAge = 60 // read from cache for 1 minute
                originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=$maxAge")
                        .build()
            } else {
                val maxStale = 60 * 60 * 24 * 7 // tolerate 1-week stale
                originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                        .build()
            }
        }.addInterceptor { chain ->
            if (context != null && ConnectionChecker.isNetworkAvailable(context!!)) {
                chain.proceed(chain.request())
            } else {
                throw ConnectionNotAvailableException()
            }
        }.cache(cache)

        return httpClient.build()
    }
}