package com.example.playlistmaker.model

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale


class TrackAdapter(
    private var tracks: List<Track>
) : RecyclerView.Adapter<TrackViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {

        holder.bind(tracks[position])
        val track = tracks[position]
        Log.d("ADAPTER_DEBUG", "Binding track $position: ${track.trackName}")
        Log.d("ADAPTER_DEBUG", "Track time: '${track.trackTimeMillis}'")



    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        this.tracks = newTracks
        notifyDataSetChanged()
    }


}

