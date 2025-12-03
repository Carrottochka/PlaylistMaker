package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val button1 = findViewById<MaterialButton>(R.id.button1)
        button1.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<MaterialButton>(R.id.button2)
        button2.setOnClickListener {
            val intent = Intent(this, MediatekaActivity::class.java)
            startActivity(intent)

        }

        val button3 = findViewById<MaterialButton>(R.id.button3)
        button3.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)

        }

    }
}
