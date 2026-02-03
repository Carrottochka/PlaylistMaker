package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.api.ApiService
import com.example.playlistmaker.api.SearchResponse
import com.google.android.material.appbar.MaterialToolbar
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.model.TrackAdapter
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

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

    private lateinit var searchHistory: SearchHistory
    private val gson = Gson()

    // Handler для реализации debounce
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

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

        searchHistory = SearchHistory(this)

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


        adapter = TrackAdapter(tracks) { track ->
            onTrackClicked(track)
        }

        historyAdapter = TrackAdapter(ArrayList()) { track ->
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


        if (trackJson.length > 100000) {
            Log.w("SEARCH_TRANSFER", "JSON is very long (${trackJson.length} chars)")
        }

        intent.putExtra(PlayerActivity.TRACK_EXTRA, trackJson)

        startActivity(intent)

    }

    private fun performSearch(searchQuery: String) {
        if (isSearchInProgress) {
            Log.d("SEARCH_DEBUG", "Search already in progress, skipping...")
            return
        }

        lastSearchQuery = searchQuery
        hideKeyboard()
        hideHistory()

        // Устанавливаем флаг что поиск начался
        isSearchInProgress = true

        // Показываем ProgressBar
        showLoadingState()

        ApiService.retrofit.search(searchQuery)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(
                    call: Call<SearchResponse>,
                    response: Response<SearchResponse>
                ) {
                    // Сбрасываем флаг поиска
                    isSearchInProgress = false

                    // Скрываем ProgressBar
                    hideLoadingState()


                    if (response.isSuccessful) {
                        val searchResults = response.body()?.results ?: emptyList()


                        if (searchResults.isNotEmpty()) {
                            val firstTrack = searchResults.first()
                            tracks.clear()
                            tracks.addAll(searchResults)
                            adapter.notifyDataSetChanged()
                            showResultsState()

                        } else {

                            showNoResultsState()
                        }
                    } else {

                        showErrorState(getString(R.string.server_error))
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    // Сбрасываем флаг поиска
                    isSearchInProgress = false

                    // Скрываем ProgressBar
                    hideLoadingState()

                    showErrorState(getString(R.string.network_error))
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
        historyAdapter.updateTracks(ArrayList(historyTracks))
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

    companion object {
        const val SEARCH_KEY = "SEARCH_KEY"
        const val SEARCH_DEF = ""
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        userSearch = savedInstanceState.getString(SEARCH_KEY, SEARCH_DEF)
        inputEditText.setText(userSearch)
        updateHistoryVisibility()
    }
}