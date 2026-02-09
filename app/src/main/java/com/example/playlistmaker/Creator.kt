package com.example.playlistmaker

import com.example.playlistmaker.data.dto.TrackRepositoryImpl
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.domain.api.TrackInteractor
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {

    // 1. Создаем маппер для преобразования DTO в Domain-модели
    private fun provideTrackMapper(): TrackMapper {
        return TrackMapper()
    }

    // 2. Создаем Network Client
    private fun provideNetworkClient(): RetrofitNetworkClient {
        return RetrofitNetworkClient()
    }

    // 3. Создаем репозиторий с ВСЕМИ необходимыми зависимостями
    private fun getTrackRepository(): TracksRepository {
        return TrackRepositoryImpl(
            networkClient = provideNetworkClient(),
            trackMapper = provideTrackMapper() // Добавляем маппер
        )
    }

    // 4. Создаем интерактор для UI слоя
    fun provideTracksInteractor(): TrackInteractor {
        return TracksInteractorImpl(getTrackRepository())
    }
}