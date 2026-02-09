// data/mapper/TrackMapper.kt
package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.domain.model.Track

class TrackMapper {

    fun map(trackDto: TrackDto): Track {
        return Track(
            trackId = trackDto.trackId ?: 0,
            trackName = trackDto.trackName ?: "Unknown Track",
            artistName = trackDto.artistName ?: "Unknown Artist",
            collectionName = trackDto.collectionName ?: "",
            releaseDate = trackDto.releaseDate ?: "",
            primaryGenreName = trackDto.primaryGenreName ?: "",
            country = trackDto.country ?: "",
            trackTimeMillis = trackDto.trackTimeMillis, // Теперь типы совпадают
            artworkUrl100 = trackDto.artworkUrl100 ?: "",
            previewUrl = trackDto.previewUrl ?: ""
        )
    }


    fun mapList(trackDtoList: List<TrackDto>): List<Track> {
        return trackDtoList.map { map(it) }
    }
}