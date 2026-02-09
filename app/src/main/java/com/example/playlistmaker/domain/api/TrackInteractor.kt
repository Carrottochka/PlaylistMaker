package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.model.Track

interface TrackInteractor {
    fun searchTrack(expression:String,consumer:TrackConsumer)

    interface TrackConsumer{
        fun consume(foundTracks:List<Track>)// выполняет функцию колбэка
    }
}