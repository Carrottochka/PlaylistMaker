package com.example.playlistmaker

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

    private val tracks = ArrayList<Track>() // результаты поиска
    private var lastSearchQuery: String = ""


    private lateinit var searchHistory: SearchHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("SEARCH_DEBUG", "=== SEARCH ACTIVITY CREATED ===")


        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        placeholderNoInternetContainer = findViewById(R.id.placeholderNoInternetContainer)
        refreshButton = findViewById(R.id.refreshButton)
        recyclerView = findViewById(R.id.trackList)


        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)


        searchHistory = SearchHistory(this)

        setupBackToolbar()
        setupInputEditText()
        setupTextWatcher()
        setupClearButton()
        setupRecyclerViews() //  настраиваем оба RecyclerView
        setupOnEditorActionListener()
        setupRefreshButton()
        setupClearHistoryButton()

        setupFocusListener()

        showInitialState()
        updateHistoryVisibility()
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
            // При клике на поле ввода обновляю видимость истории
            updateHistoryVisibility()
        }
    }

    private fun setupFocusListener() {
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            // При изменении фокуса обновляю видимость истории
            updateHistoryVisibility()
        }
    }

    private fun setupTextWatcher() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearButtonVisibility(s)
                if (s.isNullOrEmpty()) {

                    updateHistoryVisibility()
                } else {

                    hideHistory()
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
        Log.d("ADAPTER_DEBUG", "=== SETUP RECYCLERVIEWS ===")


        adapter = TrackAdapter(tracks) { track ->
            onTrackClicked(track)
        }


        historyAdapter = TrackAdapter(ArrayList()) { track ->
            onTrackClicked(track)
        }

        // Настройка RecyclerView для результатов поиска
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        // Настройка RecyclerView для истории поиска
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.setHasFixedSize(true)

        Log.d("ADAPTER_DEBUG", "RecyclerViews setup completed")
    }

    private fun setupOnEditorActionListener() {
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = inputEditText.text.toString().trim()
                if (searchText.isNotEmpty()) {
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

        // TODO: Здесь будет переход на экран плеера
        Log.d("SEARCH_DEBUG", "Track clicked: ${track.trackName}")
    }

    private fun performSearch(searchQuery: String) {
        lastSearchQuery = searchQuery
        hideKeyboard()
        hideHistory()

        Log.d("SEARCH_DEBUG", "=== SEARCH STARTED ===")
        Log.d("SEARCH_DEBUG", "Search query: '$searchQuery'")

        ApiService.retrofit.search(searchQuery)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(
                    call: Call<SearchResponse>,
                    response: Response<SearchResponse>
                ) {

                    Log.d("SEARCH_DEBUG", "=== SEARCH RESPONSE ===")
                    Log.d("SEARCH_DEBUG", "Response isSuccessful: ${response.isSuccessful}")
                    Log.d("SEARCH_DEBUG", "Response code: ${response.code()}")
                    if (response.isSuccessful) {

                        val searchResults = response.body()?.results ?: emptyList()

                        Log.d("SEARCH_DEBUG", "Results count: ${searchResults.size}")

                        if (searchResults.isNotEmpty()) {

                            val firstTrack = searchResults.first()
                            Log.d("SEARCH_DEBUG", "First track: ${firstTrack.trackName}")
                            Log.d(
                                "SEARCH_DEBUG",
                                "First track time: '${firstTrack.trackTimeMillis}'"
                            )
                            Log.d("SEARCH_DEBUG", "First track artist: ${firstTrack.artistName}")

                            tracks.clear()
                            tracks.addAll(searchResults)
                            adapter.notifyDataSetChanged()
                            showResultsState()

                            Log.d("SEARCH_DEBUG", "Showing results state")
                        } else {
                            // Нет результатов
                            Log.d("SEARCH_DEBUG", "No results found")
                            showNoResultsState()
                        }
                    } else {
                        // Ошибка сервера
                        Log.d("SEARCH_DEBUG", "Server error: ${response.code()}")
                        showErrorState(getString(R.string.server_error))
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    // Ошибка сети
                    showErrorState(getString(R.string.network_error))
                }
            })
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

    }

    private fun showResultsState() {
        recyclerView.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
    }

    private fun showNoResultsState() {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        placeholderNoInternetContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE

        placeholderMessage.text = getString(R.string.no_results)
    }

    private fun showErrorState(errorMessage: String) {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE

        val errorTextView = findViewById<TextView>(R.id.placeholderNoInternet)
        errorTextView.text = errorMessage
    }

    // Добавляю обновление истории при возобновлении активности
    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }

    private var userSearch: String = SEARCH_DEF
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, userSearch)
    }

    companion object {
        const val SEARCH_KEY = "SEARCH_KEY"
        const val SEARCH_DEF = ""
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        userSearch = savedInstanceState.getString(SEARCH_KEY, SEARCH_DEF)
        inputEditText.setText(userSearch)
        updateHistoryVisibility()
    }
}