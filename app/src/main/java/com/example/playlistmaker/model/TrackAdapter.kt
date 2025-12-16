package com.example.playlistmaker.model

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        Log.d("ADAPTER_DEBUG", "Creating new ViewHolder")
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.clearTexts()

        Log.d("ADAPTER_DEBUG", "Binding track $position: ${track.trackName}")
        Log.d("ADAPTER_DEBUG", "Track time: '${track.trackTimeMillis}'")

        holder.bind(track)

        holder.itemView.setOnClickListener {
            Log.d("ADAPTER_DEBUG", "Track clicked: ${track.trackName}, ID: ${track.trackId}")
            onTrackClick(track)
        }
    }

    override fun getItemCount(): Int {
        Log.d("ADAPTER_DEBUG", "Item count: ${tracks.size}")
        return tracks.size
    }

    fun updateTracks(newTracks: List<Track>) {
        Log.d("ADAPTER_DEBUG", "Updating tracks, old size: ${tracks.size}, new size: ${newTracks.size}")
        this.tracks = newTracks
        notifyDataSetChanged()
    }
}