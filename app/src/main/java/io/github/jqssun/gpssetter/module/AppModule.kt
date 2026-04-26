package io.github.jqssun.gpssetter.module

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jqssun.gpssetter.module.util.ApplicationScope
import io.github.jqssun.gpssetter.room.AppDatabase
import io.github.jqssun.gpssetter.room.FavoriteDao
import io.github.jqssun.gpssetter.update.GitHubService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun createRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.github.com/repos/jqssun/android-gps-setter/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideGitHubService(retrofit: Retrofit): GitHubService =
        retrofit.create(GitHubService::class.java)

    @Singleton
    @Provides
    fun provideDownloadManager(application: Application): DownloadManager =
        application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    @Provides
    @Singleton
    fun provideDatabase(application: Application, callback: AppDatabase.Callback): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "user_database")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Singleton
    @Provides
    fun providesUserDao(favoriteDatabase: AppDatabase): FavoriteDao =
        favoriteDatabase.favoriteDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())
}
