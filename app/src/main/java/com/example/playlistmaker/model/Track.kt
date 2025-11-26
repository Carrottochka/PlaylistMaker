package com.example.playlistmaker.model

data class Track(
    val trackName:String,
    val artistName:String,
    val trackTime: String?, // Продолжительность трека
    val artworkUrl100: String // Ссылка на изображение обложки
    )

