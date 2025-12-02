package com.example.playlistmaker.model

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
) {

    private val ivArtwork: ImageView = itemView.findViewById(R.id.artworkUrl100)
    private val tvTrackName: TextView = itemView.findViewById(R.id.trackName)
    private val tvArtistName: TextView = itemView.findViewById(R.id.artistName)
    private val tvTrackTime: TextView = itemView.findViewById(R.id.trackTime)

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    fun bind(track: Track) {
        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName
        println("DEBUG: FULL TRACK = $track")


        tvTrackTime.text = formatTrackTime(track.trackTimeMillis)

        val radiusInPx = 2.dpToPx(itemView.context)

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_album_place_holder_34)
            .fitCenter()
            .transform(RoundedCorners(radiusInPx))
            .into(ivArtwork)

    }

    private fun formatTrackTime(trackTimeMillis: String?): String {
        println("DEBUG: formatTrackTime input = '$trackTimeMillis'")

        if (trackTimeMillis.isNullOrEmpty()) {
            println("DEBUG: trackTime is null or empty")
            return "--:--"
        }

        return try {
            println("DEBUG: trying to parse '$trackTimeMillis' as Long")

            val timeValue = trackTimeMillis.toLong()
            println("DEBUG: successfully parsed as Long = $timeValue")

            // В iTunes API время приходит в миллисекундах
            val totalSeconds = timeValue / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            val result = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            println("DEBUG: formatted result = $result")
            result

        } catch (e: NumberFormatException) {
            println("DEBUG: NumberFormatException - cannot parse '$trackTimeMillis' as Long")
            "--:--"
        } catch (e: Exception) {
            println("DEBUG: other exception = ${e.message}")
            "--:--"
        }
    }

}
