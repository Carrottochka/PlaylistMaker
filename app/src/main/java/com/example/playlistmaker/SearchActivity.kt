package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import model.MockData
import model.TrackAdapter


class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val mockTracks = MockData.getMockTracks()

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


        setupBackToolbar()
        setupInputEditText()
        setupTextWatcher()
        setupClearButton()

        setupRecyclerView()
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
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        inputEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
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
        val includedView = findViewById<View>(R.id.trackList)
        recyclerView = includedView.findViewById(R.id.trackList)
        adapter = TrackAdapter(mockTracks)
        // НАСТРАИВАЕМ RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)


    }


}