package com.nooraniqaida.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.ui.QaidaApp
import com.nooraniqaida.app.ui.QaidaTheme

class MainActivity : AppCompatActivity() {

    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speaker = Speaker(this)
        setContent { QaidaTheme { QaidaApp(speaker) } }
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}
