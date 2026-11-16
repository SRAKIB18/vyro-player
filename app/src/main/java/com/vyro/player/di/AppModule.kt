package com.vyro.player.di

import android.content.Context
import androidx.room.Room
import com.vyro.player.data.database.VyroDatabase
import com.vyro.player.data.database.dao.PlaybackHistoryDao
import com.vyro.player.data.database.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVyroDatabase(
        @ApplicationContext context: Context
    ): VyroDatabase {
        return Room.databaseBuilder(
            context,
            VyroDatabase::class.java,
            "vyro_player_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideVideoDao(database: VyroDatabase): VideoDao = database.videoDao()

    @Provides
    fun providePlaybackHistoryDao(database: VyroDatabase): PlaybackHistoryDao = database.playbackHistoryDao()
}
