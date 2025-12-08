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
import com.example.playlistmaker.model.MockData
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
    private lateinit var adapter: TrackAdapter

    private val tracks = ArrayList<Track>()

    //private val tracks = MockData.getMockTracks()
    private var lastSearchQuery: String = ""

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
        Log.d("ADAPTER_DEBUG", "Adapter initialized: ${::adapter.isInitialized}")
        Log.d("ADAPTER_DEBUG", "RecyclerView initialized: ${::recyclerView.isInitialized}")

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        placeholderNoInternetContainer = findViewById(R.id.placeholderNoInternetContainer)
        refreshButton = findViewById(R.id.refreshButton)
        recyclerView = findViewById(R.id.trackList)

        setupBackToolbar()
        setupInputEditText()
        setupTextWatcher()
        setupClearButton()
        setupRecyclerView()
        setupOnEditorActionListener()
        setupRefreshButton()

        showInitialState()
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
        }
    }

    private fun setupTextWatcher() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}


            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearButtonVisibility(s)
                if (s.isNullOrEmpty()) {
                    showInitialState()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        inputEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            showInitialState()
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
    }

    private fun setupRecyclerView() {
        Log.d("ADAPTER_DEBUG", "=== SETUP RECYCLERVIEW ===")

        adapter = TrackAdapter(tracks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        Log.d("ADAPTER_DEBUG", "RecyclerView setup completed")
        Log.d("ADAPTER_DEBUG", "Initial tracks count: ${tracks.size}")
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

    private fun performSearch(searchQuery: String) {
        lastSearchQuery = searchQuery
        hideKeyboard()

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
                        showErrorState(getString(R.string.server_error_1))
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    // Ошибка сети
                    showErrorState(getString(R.string.network_error))
                }
            })
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
    }

    private fun showNoResultsState() {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        placeholderNoInternetContainer.visibility = View.GONE

        placeholderMessage.text = getString(R.string.no_results)
    }

    private fun showErrorState(errorMessage: String) {
        tracks.clear()
        adapter.notifyDataSetChanged()

        recyclerView.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        placeholderNoInternetContainer.visibility = View.VISIBLE


        val errorTextView = findViewById<TextView>(R.id.placeholderNoInternet)
        errorTextView.text = errorMessage
    }
}
