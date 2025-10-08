package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<MaterialButton>(R.id.button1)
        button1.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


        /* val button1ClickListener: View.OnClickListener = object: View.OnClickListener{
             override fun onClick(v: View?) {

                 Toast.makeText(this@MainActivity, "Нажали на кноку!", Toast.LENGTH_LONG).show()
             }
         }
             button1.setOnClickListener(button1ClickListener)*/

        val button2 = findViewById<MaterialButton>(R.id.button2)
        button2.setOnClickListener {
            val intent = Intent(this, MediatekaActivity::class.java)
            startActivity(intent)
            // Toast.makeText(this@MainActivity, "Нажали на кнопку!", Toast.LENGTH_LONG).show()
        }

        val button3 = findViewById<MaterialButton>(R.id.button3)
        button3.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            //Toast.makeText(this@MainActivity, "Нажали на кнопку!", Toast.LENGTH_LONG).show()
        }

    }
}
