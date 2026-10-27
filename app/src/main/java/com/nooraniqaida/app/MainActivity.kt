package com.nooraniqaida.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nooraniqaida.app.audio.Recorder
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Progress
import com.nooraniqaida.app.ui.QaidaApp
import com.nooraniqaida.app.ui.QaidaTheme

class MainActivity : AppCompatActivity() {

    private lateinit var speaker: Speaker
    private lateinit var progress: Progress
    private lateinit var recorder: Recorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instructions default to English, whatever the phone's language.
        // A language picked in the app (Arabic, Bangla, English) is remembered and wins.
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
        speaker = Speaker(this)
        progress = Progress(this)
        recorder = Recorder(this)
        setContent { QaidaTheme { QaidaApp(speaker, progress, recorder) } }
    }

    override fun onDestroy() {
        recorder.stop()
        speaker.shutdown()
        super.onDestroy()
    }
}
