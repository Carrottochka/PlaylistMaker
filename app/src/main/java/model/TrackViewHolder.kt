package model

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R

class TrackViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
) {

    private val ivArtwork: ImageView = itemView.findViewById(R.id.artworkUrl100)
    private val tvTrackName: TextView = itemView.findViewById(R.id.trackName)
    private val tvArtistName: TextView = itemView.findViewById(R.id.artistName)
    private val tvTrackTime: TextView = itemView.findViewById(R.id.trackTime)

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    fun bind(track: Track) {
        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName
        tvTrackTime.text = track.trackTime

        val radiusInPx = 2.dpToPx(itemView.context)

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_album_place_holder_34)
            .fitCenter()
            .transform(RoundedCorners(radiusInPx))
            .into(ivArtwork)

    }


}
