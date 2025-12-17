package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.model.Track
import com.google.gson.Gson
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var track: Track? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        parseTrackFromIntent()
        setupBackButton()

        if (track != null) {
            displayTrackInfo()
        }
    }

    private fun parseTrackFromIntent() {
        val trackJson = intent.getStringExtra(TRACK_EXTRA)
        track = trackJson?.let { gson.fromJson(it, Track::class.java) }
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun displayTrackInfo() {
        val currentTrack = track ?: return


        findViewById<TextView>(R.id.trackTitle).text = currentTrack.trackName
        findViewById<TextView>(R.id.artistName).text = currentTrack.artistName


        val albumCover = findViewById<ImageView>(R.id.albumCover)
        currentTrack.artworkUrl100?.replace("100x100", "600x600")?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_big)
                .transform(RoundedCorners(8.dpToPx()))
                .into(albumCover)
        } ?: albumCover.setImageResource(R.drawable.ic_placeholder_big)


        setupMetadata(R.id.collectionName, currentTrack.collectionName)
        setupMetadata(R.id.durationTime, formatTrackTime(currentTrack.trackTimeMillis))
        setupMetadata(R.id.releaseDate, currentTrack.releaseDate?.takeIf { it.isNotEmpty() }?.substring(0, 4))
        setupMetadata(R.id.primaryGenreName, currentTrack.primaryGenreName)
        setupMetadata(R.id.country, currentTrack.country)
    }

    private fun setupMetadata(viewId: Int, value: String?) {
        val metadataView = findViewById<TextView>(viewId)
        val labelView = findLabelForMetadata(viewId)

        if (!value.isNullOrEmpty()) {
            metadataView.text = value
            metadataView.visibility = View.VISIBLE
            labelView?.visibility = View.VISIBLE
        } else {
            // Скрываем и метку, и значение
            metadataView.visibility = View.GONE
            labelView?.visibility = View.GONE
        }
    }

    private fun findLabelForMetadata(viewId: Int): TextView? {
        return when (viewId) {
            R.id.collectionName -> findViewById(R.id.collection)
            R.id.durationTime -> findViewById(R.id.duration)
            R.id.releaseDate -> findViewById(R.id.release)
            R.id.primaryGenreName -> findViewById(R.id.primaryGenre)
            R.id.country -> findViewById(R.id.countryTrack)
            else -> null
        }
    }

    private fun formatTrackTime(trackTimeMillis: String?): String? {
        return try {
            trackTimeMillis?.toLong()?.let { timeValue ->
                val totalSeconds = timeValue / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
    }
}