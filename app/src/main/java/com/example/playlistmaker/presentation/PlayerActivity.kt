package com.example.playlistmaker.presentation

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.google.gson.Gson
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var track: Track? = null
    private val gson = Gson()
    private lateinit var playButton: ImageView
    private lateinit var currentTimeView: TextView
    private lateinit var totalTimeView: TextView
    private var mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playButton = findViewById(R.id.play)
        currentTimeView = findViewById(R.id.durationValue)
        totalTimeView = findViewById(R.id.durationTime)


        playButton.isEnabled = false
        playButton.alpha = 0.5f


        totalTimeView.text = formatTime(PREVIEW_DURATION)
        currentTimeView.text = formatTime(0)

        parseTrackFromIntent()
        setupBackButton()
        setupPlayButton()

        if (track != null) {
            displayTrackInfo()
            preparePlayer()
        }
    }

    private fun parseTrackFromIntent() {
        val trackJson = intent.getStringExtra(TRACK_EXTRA)
        track = trackJson?.let { gson.fromJson(it, Track::class.java) }
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun setupPlayButton() {
        playButton.setOnClickListener {
            when (playerState) {
                STATE_PREPARED -> {
                    startPlayback()
                    playButton.setImageResource(R.drawable.ic_pause)
                }
                STATE_PLAYING -> {
                    pausePlayback()
                    playButton.setImageResource(R.drawable.ic_play_button)
                }
                STATE_PAUSED -> {
                    startPlayback()
                    playButton.setImageResource(R.drawable.ic_pause)
                }
                else -> {
                    preparePlayer()
                }
            }
        }
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
                formatTime(timeValue)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.Companion.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.Companion.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun preparePlayer() {
        track?.previewUrl?.let { previewUrl ->
            try {
                mediaPlayer.setDataSource(previewUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    playButton.isEnabled = true
                    playButton.alpha = 1.0f
                    playerState = STATE_PREPARED
                    // Запускаем обновление времени
                    startTimeUpdater()
                }
                mediaPlayer.setOnCompletionListener {
                    playerState = STATE_PREPARED
                    playButton.setImageResource(R.drawable.ic_play_button)
                    // Сбрасываем время
                    currentTimeView.text = formatTime(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                playButton.isEnabled = false
            }
        } ?: run {
            playButton.isEnabled = false
        }
    }

    private fun startTimeUpdater() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    val currentPosition = mediaPlayer.currentPosition
                    currentTimeView.text = formatTime(currentPosition)


                    if (currentPosition >= PREVIEW_DURATION) {
                        mediaPlayer.pause()
                        playerState = STATE_PREPARED
                        playButton.setImageResource(R.drawable.ic_play_button)
                        currentTimeView.text = formatTime(PREVIEW_DURATION)
                    }
                }
                handler.postDelayed(this, TIME_UPDATE_DELAY_MS)
            }
        }, TIME_UPDATE_DELAY_MS)
    }

    private fun startPlayback() {
        mediaPlayer.start()
        playerState = STATE_PLAYING
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            pausePlayback()
        }

        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }

    companion object {
        const val TRACK_EXTRA = "track_extra"
        const val PREVIEW_DURATION = 30000
        private const val TIME_UPDATE_DELAY_MS = 500L

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }
}