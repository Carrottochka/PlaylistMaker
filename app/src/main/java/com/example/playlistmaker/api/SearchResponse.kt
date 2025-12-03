package com.example.playlistmaker.api

import com.example.playlistmaker.model.Track

//класс для параметров запроса
data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)