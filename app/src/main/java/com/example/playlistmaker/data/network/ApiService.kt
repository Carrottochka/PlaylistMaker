package com.example.playlistmaker.data.network

import com.example.playlistmaker.domain.api.ItunesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {

    private val musicSearchUrl="https://itunes.apple.com"

      val retrofit = Retrofit.Builder()
        .baseUrl(musicSearchUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ItunesApi::class.java)



}