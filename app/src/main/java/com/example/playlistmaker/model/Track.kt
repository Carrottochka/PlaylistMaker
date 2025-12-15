package com.example.playlistmaker.model

/* data class Track(
    val trackId:Long,
    val trackName:String,
    val artistName:String,
    val trackTimeMillis: String?,
    val artworkUrl100: String,
    val collectionName:String?,
    val releaseDate:String?,
    val primaryGenreName:String,
    val country:String
    )*/
import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("previewUrl") val previewUrl: String?
)
