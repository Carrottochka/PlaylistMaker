package com.example.playlistmaker.data.dto

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.api.TracksRepository
import com.example.playlistmaker.domain.model.Track

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    val trackMapper: TrackMapper
) : TracksRepository {
    override fun searchTrack(expression: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))

       return if (response.resultCode == 200) {
            // Преобразуем DTO в Domain-модели
            trackMapper.mapList(response.data)
        } else {
            emptyList()
        }

    }
}


