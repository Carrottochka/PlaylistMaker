package com.example.playlistmaker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbarBack = findViewById<Toolbar>(R.id.toolbarBack)
        toolbarBack.setOnClickListener {
            println("Кнопка back нажата!")
            finish()
        }
        setupShareButton()
        setupContackSupport()
        setupUserAgreement()
    }

    fun setupShareButton() {
        val shareApp = findViewById<TextView>(R.id.shareApp)
        shareApp.setOnClickListener {
            val intentShare = Intent(Intent.ACTION_SEND)
            intentShare.type = "text/plain"
            intentShare.putExtra(
                Intent.EXTRA_TEXT,
                "https://practicum.yandex.ru/android-developer/"
            )
            startActivity(Intent.createChooser(intentShare, "Поделиться через"))
        }
    }

    fun setupContackSupport() {
        val contactSupport = findViewById<TextView>(R.id.contactSupport)
        contactSupport.setOnClickListener {
            val intentSupport = Intent(Intent.ACTION_SENDTO).apply {
                val studentEmail = "carrottochka@gmail.com"
                val subject = "Сообщение разработчикам и разработчицам приложения Playlist Maker"
                val body = "Спасибо разработчикам и разработчицам за крутое приложение!"
                val uriText = "mailto:$studentEmail" +
                        "?subject=${Uri.encode(subject)}" +
                        "&body=${Uri.encode(body)}"
                data = Uri.parse(uriText)
                putExtra(
                    Intent.EXTRA_SUBJECT, subject
                )
                putExtra(
                    Intent.EXTRA_TEXT, body
                )
            }
            try {
                startActivity(Intent.createChooser(intentSupport, "Написать в поддержку"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@SettingActivity,
                    "Почтовое приложение не найдено",
                    Toast.LENGTH_LONG
                ).show()

            }

        }
    }

    fun setupUserAgreement() {
        val userAgreement = findViewById<TextView>(R.id.userAgreement)
        userAgreement.setOnClickListener {
            val intentUserAgreement =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/legal/practicum_offer/"))
            startActivity(intentUserAgreement)
        }
    }


}