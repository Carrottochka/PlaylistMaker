package com.example.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.api.TrackInteractor
import com.example.playlistmaker.domain.model.Track
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var placeholderNoInternetContainer: View
    private lateinit var refreshButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyContainer: View
    private lateinit var clearHistoryButton: Button
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var progressBar: View

    private val tracks = ArrayList<Track>()
    private var lastSearchQuery: String = ""
    private var isSearchInProgress = false

    private val tracksInteractor: TrackInteractor = Creator.provideTracksInteractor()
    private val searchHistory = SearchHistory(this)
    private val gson = Gson()

    // Handler для реализации debounce
    private val handler = Handler(Looper.getMainLooper())

    // Runnable для выполнения поиска с задержкой
    private val searchRunnable = Runnable {
        val searchText = inputEditText.text.toString().trim()
        if (searchText.isNotEmpty()) {
            performSearch(searchText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupBackToolbar()
        setupInputEditText()
        setupTextWatcher()
        setupClearButton()
        setupRecyclerViews()
        setupOnEditorActionListener()
        setupRefreshButton()
        setupClearHistoryButton()
        setupFocusListener()

        showInitialState()
        updateHistoryVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Удаляем все pending runnables при уничтожении Activity
        handler.removeCallbacks(searchRunnable)
    }

    private fun initViews() {
        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        placeholderNoInternetContainer = findViewById(R.id.placeholderNoInternetContainer)
        refreshButton = findViewById(R.id.refreshButton)
        recyclerView = findViewById(R.id.trackList)
        progressBar = findViewById(R.id.progressBar)
        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
    }

    private fun setupBackToolbar() {
        val backToolbar = findViewById<MaterialToolbar>(R.id.toolbarBack)
        backToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputEditText() {
        inputEditText.setOnClickListener {
            inputEditText.requestFocus()
            showKeyboard()
            updateHistoryVisibility()
        }
    }

    private fun setupFocusListener() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    private fun setupTextWatcher() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    // Если поле пустое, показываем историю поиска
                    updateHistoryVisibility()
                    // Скрываем прогресс бар если был показан
                    hideLoadingState()
                    // Отменяем запланированный поиск
                    handler.removeCallbacks(searchRunnable)
                } else {
                    // Если есть текст, скрываем историю
                    hideHistory()

                    // Отменяем предыдущий запланированный поиск
                    handler.removeCallbacks(searchRunnable)

                    // Запускаем новый поиск с debounce через 2 секунды
                    handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.setText("")
            inputEditText.requestFocus()
            hideKeyboard()
            updateHistoryVisibility()
            // Отменяем все ожидающие поисковые запросы при очистке
            handler.removeCallbacks(searchRunnable)
            // Если поиск в процессе, показываем начальное состояние
            if (isSearchInProgress) {
                showInitialState()
                isSearchInProgress = false
            }
        }
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        clearButton.visibility = if (text.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun setupRecyclerViews() {
        // Адаптер для результатов поиска
        adapter = TrackAdapter(tracks) { track ->
            onTrackClicked(track)
        }

        // Адаптер для истории поиска
        historyAdapter = TrackAdapter(emptyList()) { track ->
            onTrackClicked(track)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.setHasFixedSize(true)
    }

    private fun setupOnEditorActionListener() {
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = inputEditText.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    // Отменяем отложенный поиск и выполняем немедленно
                    handler.removeCallbacks(searchRunnable)
                    performSearch(searchText)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupRefreshButton() {
        refreshButton.setOnClickListener {
            if (lastSearchQuery.isNotEmpty()) {
                performSearch(lastSearchQuery)
            }
        }
    }

    private fun setupClearHistoryButton() {
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }
    }

    private fun onTrackClicked(track: Track) {
        searchHistory.addTrack(track)
        updateHistoryVisibility()

        val intent = Intent(this, PlayerActivity::class.java)
        val trackJson = gson.toJson(track)
        intent.putExtra(PlayerActivity.TRACK_EXTRA, trackJson)
        startActivity(intent)
    }

    private fun performSearch(searchQuery: String) {
        if (isSearchInProgress) {
            return
        }

        lastSearchQuery = searchQuery
        hideKeyboard()
        hideHistory()

        // Устанавливаем флаг что поиск начался
        isSearchInProgress = true

        // Показываем ProgressBar
        showLoadingState()

        // Используем интерактор с callback
        tracksInteractor.searchTrack(searchQuery, object : TrackInteractor.TrackConsumer {
            override fun consume(foundTracks: List<Track>) {
                // Возвращаемся в главный поток для обновления UI
                runOnUiThread {
                    isSearchInProgress = false
                    hideLoadingState()

                    if (foundTracks.isNotEmpty()) {
                        tracks.clear()
                        tracks.addAll(foundTracks)
                        adapter.notifyDataSetChanged()
                        showResultsState()
                    } else {
                        showNoResultsState()
                    }
                }
            }
        })
    }

    private fun showLoadingState() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
    }

    private fun hideLoadingState() {
        progressBar.visibility = View.GONE
    }

    private fun updateHistoryVisibility() {
        val hasFocus = inputEditText.hasFocus()
        val isEmpty = inputEditText.text.isNullOrEmpty()
        val hasHistory = searchHistory.hasHistory()

        val shouldShowHistory = hasFocus && isEmpty && hasHistory

        if (shouldShowHistory) {
            showHistoryState()
        } else {
            hideHistory()
        }
    }

    private fun showHistoryState() {
        val historyTracks = searchHistory.getHistory()
        historyAdapter.updateTracks(historyTracks)
        historyContainer.visibility = View.VISIBLE

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun hideHistory() {
        historyContainer.visibility = View.GONE
    }

    private fun showInitialState() {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showResultsState() {
        recyclerView.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNoResultsState() {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        placeholderNoInternetContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        placeholderMessage.text = getString(R.string.no_results)
    }

    private fun showErrorState(errorMessage: String) {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        val errorTextView = findViewById<TextView>(R.id.placeholderNoInternet)
        errorTextView.text = errorMessage
    }

    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }

    override fun onPause() {
        super.onPause()
        // Отменяем все отложенные операции при паузе
        handler.removeCallbacks(searchRunnable)
    }

    private var userSearch: String = SEARCH_DEF
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, userSearch)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        userSearch = savedInstanceState.getString(SEARCH_KEY, SEARCH_DEF)
        inputEditText.setText(userSearch)
        updateHistoryVisibility()
    }

    companion object {
        const val SEARCH_KEY = "SEARCH_KEY"
        const val SEARCH_DEF = ""
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}