package com.vyro.player.di

import com.vyro.player.data.repository.AudioRepositoryImpl
import com.vyro.player.data.repository.SettingsRepositoryImpl
import com.vyro.player.data.repository.VideoRepositoryImpl
import com.vyro.player.domain.repository.AudioRepository
import com.vyro.player.domain.repository.SettingsRepository
import com.vyro.player.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
