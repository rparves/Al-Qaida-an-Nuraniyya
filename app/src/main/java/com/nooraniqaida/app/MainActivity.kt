package com.nooraniqaida.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Progress
import com.nooraniqaida.app.ui.QaidaApp
import com.nooraniqaida.app.ui.QaidaTheme

class MainActivity : AppCompatActivity() {

    private lateinit var speaker: Speaker
    private lateinit var progress: Progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instructions default to English, whatever the phone's language.
        // A language picked in the app (Arabic, Bangla, English) is remembered and wins.
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
        speaker = Speaker(this)
        progress = Progress(this)
        setContent { QaidaTheme { QaidaApp(speaker, progress) } }
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}
