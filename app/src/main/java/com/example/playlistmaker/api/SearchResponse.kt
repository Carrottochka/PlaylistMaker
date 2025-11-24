package com.example.playlistmaker.api

import model.Track

//класс для параметров запроса
data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)