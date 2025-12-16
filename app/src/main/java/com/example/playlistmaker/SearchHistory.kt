package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SearchHistory(private val context: Context) {



    private val gson = Gson()

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
    }

    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {

            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
    }

    fun hasHistory(): Boolean {

        return getHistory().isNotEmpty()
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, json)
            .apply()
    }
    companion object {
        private const val HISTORY_PREFS = "search_history"
        private const val HISTORY_KEY = "history_tracks"
        private const val MAX_HISTORY_SIZE = 10
    }
}