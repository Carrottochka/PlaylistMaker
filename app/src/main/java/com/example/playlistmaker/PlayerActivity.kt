package com.example.playlistmaker

import android.os.Bundle
import android.util.Log
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

        Log.d("PLAYER_DEBUG", "=== PLAYER ACTIVITY CREATED ===")

        // Проверяю intent
        if (intent == null) {
            Log.e("PLAYER_DEBUG", "Intent is null!")
            finish()
            return
        }

        // Проверяю extras
        val extras = intent.extras
        if (extras == null) {
            Log.e("PLAYER_DEBUG", "Intent extras are null!")
            finish()
            return
        }

        Log.d("PLAYER_DEBUG", "Intent extras keys: ${extras.keySet()}")
        Log.d("PLAYER_DEBUG", "Has TRACK_EXTRA: ${intent.hasExtra(TRACK_EXTRA)}")

        parseTrackFromIntent()

        setupBackButton()


        if (track != null) {
            displayTrackInfo()
        } else {
            Log.e("PLAYER_DEBUG", "Track is null, cannot display info")
            showErrorMessage()
        }
    }

    private fun parseTrackFromIntent() {
        try {
            Log.d("PLAYER_DEBUG", "Parsing track from intent...")

            val trackJson = intent.getStringExtra(TRACK_EXTRA)

            if (trackJson.isNullOrEmpty()) {
                Log.e("PLAYER_DEBUG", "Track JSON is null or empty!")
                Log.d("PLAYER_DEBUG", "JSON value: '$trackJson'")
                return
            }

            Log.d("PLAYER_DEBUG", "JSON length: ${trackJson.length}")
            Log.d("PLAYER_DEBUG", "JSON (first 200 chars): ${trackJson.take(200)}...")


            track = gson.fromJson(trackJson, Track::class.java)

            if (track != null) {
                Log.d("PLAYER_DEBUG", "=== TRACK PARSED SUCCESSFULLY ===")
                Log.d("PLAYER_DEBUG", "Track name: ${track?.trackName}")
                Log.d("PLAYER_DEBUG", "Artist: ${track?.artistName}")
                Log.d("PLAYER_DEBUG", "Album: ${track?.collectionName}")
                Log.d("PLAYER_DEBUG", "Duration: ${track?.trackTimeMillis}")
                Log.d("PLAYER_DEBUG", "Artwork URL: ${track?.artworkUrl100}")
                Log.d("PLAYER_DEBUG", "Release date: ${track?.releaseDate}")
                Log.d("PLAYER_DEBUG", "Genre: ${track?.primaryGenreName}")
                Log.d("PLAYER_DEBUG", "Country: ${track?.country}")
            } else {
                Log.e("PLAYER_DEBUG", "Failed to parse track from JSON")
            }

        } catch (e: Exception) {
            Log.e("PLAYER_DEBUG", "Error parsing track: ${e.message}", e)
            track = null
        }
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun displayTrackInfo() {
        val currentTrack = track ?: run {
            Log.e("PLAYER_DEBUG", "Track is null in displayTrackInfo")
            return
        }

        Log.d("PLAYER_DEBUG", "=== DISPLAYING TRACK INFO ===")

        val albumCover = findViewById<ImageView>(R.id.albumCover)
        val trackName = findViewById<TextView>(R.id.trackTitle)
        val artistName = findViewById<TextView>(R.id.artistName)
        val collectionName = findViewById<TextView>(R.id.collectionName)
        val durationTime = findViewById<TextView>(R.id.durationTime)
        val releaseDate = findViewById<TextView>(R.id.releaseDate)
        val primaryGenreName = findViewById<TextView>(R.id.primaryGenreName)
        val country = findViewById<TextView>(R.id.country)


        trackName.text = currentTrack.trackName ?: "Без названия"
        artistName.text = currentTrack.artistName ?: "Неизвестный исполнитель"
        collectionName.text = currentTrack.collectionName ?: "Неизвестный альбом"

        durationTime.text = formatTrackTime(currentTrack.trackTimeMillis)
        releaseDate.text = formatReleaseDate(currentTrack.releaseDate)
        primaryGenreName.text = currentTrack.primaryGenreName ?: "Неизвестно"
        country.text = currentTrack.country ?: "Неизвестно"


        val artworkUrl = currentTrack.artworkUrl100?.replace("100x100", "600x600")
        artworkUrl?.let { url ->
            Log.d("PLAYER_DEBUG", "Loading image from: $url")
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_big)
                .transform(RoundedCorners(8.dpToPx()))
                .into(albumCover)
        } ?: run {
            Log.d("PLAYER_DEBUG", "No artwork URL, using placeholder")
            albumCover.setImageResource(R.drawable.ic_placeholder_big)
        }
    }

    private fun showErrorMessage() {
        val trackName = findViewById<TextView>(R.id.trackTitle)
        trackName.text = "Ошибка загрузки трека"
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

    private fun formatReleaseDate(releaseDate: String?): String {
        return try {
            releaseDate?.let { date ->
                if (date.isNotEmpty() && date.length >= 4) {
                    date.substring(0, 4)
                } else {
                    "Неизвестно"
                }
            } ?: "Неизвестно"
        } catch (e: Exception) {
            "Неизвестно"
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