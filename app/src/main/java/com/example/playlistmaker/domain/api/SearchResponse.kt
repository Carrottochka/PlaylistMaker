package com.example.playlistmaker.domain.api

import com.example.playlistmaker.data.dto.Response
import com.example.playlistmaker.data.dto.TrackDto

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
): Response()