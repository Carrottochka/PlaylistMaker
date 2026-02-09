package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.model.Track

interface TracksRepository {
    fun searchTrack(expression: String): List<Track>
}