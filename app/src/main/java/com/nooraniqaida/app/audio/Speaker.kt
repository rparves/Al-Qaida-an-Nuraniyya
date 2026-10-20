package com.nooraniqaida.app.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nooraniqaida.app.data.QItem
import java.util.Locale

/**
 * Plays a recorded clip from assets/audio/<id>.ogg when one is bundled,
 * otherwise falls back to the device's neural Arabic text-to-speech voice
 * (zero bytes added to the app).
 */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    enum class Status { LOADING, READY, MISSING_ARABIC, UNAVAILABLE }

    private val appContext = context.applicationContext
    private val tts = TextToSpeech(appContext, this)
    private val clips: Set<String> = runCatching {
        appContext.assets.list("audio")?.filter { it.endsWith(".ogg") }?.toSet()
    }.getOrNull() ?: emptySet()
    private var player: MediaPlayer? = null

    var status by mutableStateOf(Status.LOADING)
        private set
    var slow by mutableStateOf(false)

    override fun onInit(result: Int) {
        if (result != TextToSpeech.SUCCESS) {
            status = Status.UNAVAILABLE
            return
        }
        val r = tts.setLanguage(Locale.forLanguageTag("ar"))
        status = if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            Status.MISSING_ARABIC
        } else {
            Status.READY
        }
    }

    /** Plays the item [times] times in a row (for "listen and repeat"). */
    fun speak(item: QItem, times: Int = 1) {
        stopClip()
        val clip = "${item.id}.ogg"
        if (clip in clips) {
            playClip("audio/$clip", times)
            return
        }
        tts.setSpeechRate(if (slow) 0.5f else 0.8f)
        for (i in 0 until times) {
            tts.speak(item.speak, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "${item.id}_$i")
            if (i < times - 1) tts.playSilentUtterance(600, TextToSpeech.QUEUE_ADD, null)
        }
    }

    fun stop() {
        stopClip()
        tts.stop()
    }

    private fun playClip(path: String, times: Int) {
        var remaining = times
        runCatching {
            appContext.assets.openFd(path).use { fd ->
                player = MediaPlayer().apply {
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    if (slow && android.os.Build.VERSION.SDK_INT >= 23) {
                        playbackParams = playbackParams.setSpeed(0.7f)
                    }
                    setOnCompletionListener { mp ->
                        remaining--
                        if (remaining > 0 && player === mp) {
                            mp.seekTo(0)
                            mp.start()
                        } else {
                            mp.release()
                            if (player === mp) player = null
                        }
                    }
                    prepare()
                    start()
                }
            }
        }
    }

    private fun stopClip() {
        player?.runCatching { stop(); release() }
        player = null
    }

    fun installVoiceIntent(): Intent =
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun shutdown() {
        stopClip()
        tts.stop()
        tts.shutdown()
    }
}
