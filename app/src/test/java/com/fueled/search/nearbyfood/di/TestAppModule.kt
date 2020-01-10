package com.fueled.search.nearbyfood.di

import androidx.room.Room
import com.fueled.search.nearbyfood.detail.DetailViewModel
import com.fueled.search.nearbyfood.repository.FavoritesRepository
import com.fueled.search.nearbyfood.repository.FavoritesRepositoryImpl
import com.fueled.search.nearbyfood.repository.PlacesRepository
import com.fueled.search.nearbyfood.repository.PlacesRepositoryImpl
import com.fueled.search.nearbyfood.search.SearchViewModel
import com.fueled.search.nearbyfood.storage.DB_NAME
import com.fueled.search.nearbyfood.storage.PlacesDatabase
import com.fueled.search.nearbyfood.util.ResourceProvider
import com.fueled.search.nearbyfood.util.ResourceProviderImpl
import com.fueled.search.nearbyfood.network.FoursquareService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by Kiran.
 */

val testAppModule = module {

    single<OkHttpClient> {
        val builder = OkHttpClient.Builder()
        builder.readTimeout(30, TimeUnit.SECONDS)
        builder.writeTimeout(30, TimeUnit.SECONDS)
        builder.connectTimeout(30, TimeUnit.SECONDS)
        //builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        builder.build()
    }

    single<FoursquareService> {
        Retrofit.Builder()
            .baseUrl(FoursquareService.API_CLIENT_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(FoursquareService::class.java)
    }

    single<Executor> { Executors.newSingleThreadExecutor() }

    single { Room.databaseBuilder(androidApplication(), PlacesDatabase::class.java, DB_NAME).build() }

    single<PlacesRepository> { PlacesRepositoryImpl(get(), get()) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get()) }

    single<ResourceProvider> { ResourceProviderImpl(androidApplication()) }

    viewModel { SearchViewModel(get(), get(), get()) }

    viewModel { (id: String) -> DetailViewModel(id, get(), get(), get()) }
}