package com.nooraniqaida.app.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nooraniqaida.app.data.QItem
import java.util.Locale

/**
 * Plays a recorded clip from assets/audio/<id>.ogg when one is bundled, otherwise the
 * device's Arabic text-to-speech voice. The learner can choose which Arabic voice to
 * use (a male one by default where the phone has one) and how deep it sounds.
 */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    enum class Status { LOADING, READY, MISSING_ARABIC, UNAVAILABLE }

    companion object {
        /** Al-Fatiha 1:1 recited by Sheikh Ayman Suwaid, streamed from EveryAyah (not bundled). */
        const val BASMALA_URL = "https://everyayah.com/data/Ayman_Sowaid_64kbps/001001.mp3"
        const val BASMALA_TEXT = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("voice", Context.MODE_PRIVATE)
    private val tts = TextToSpeech(appContext, this)
    private val clips: Set<String> = runCatching {
        appContext.assets.list("audio")?.filter { it.endsWith(".ogg") }?.toSet()
    }.getOrNull() ?: emptySet()
    private var player: MediaPlayer? = null
    private var arabicVoices: List<Voice> = emptyList()

    var status by mutableStateOf(Status.LOADING)
        private set
    var slow by mutableStateOf(false)
    /** Names of the installed Arabic voices. */
    var voices by mutableStateOf<List<String>>(emptyList())
        private set
    var voiceName by mutableStateOf(prefs.getString("voice", null))
        private set
    var pitch by mutableStateOf(prefs.getFloat("pitch", 0.85f))
        private set

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
        arabicVoices = runCatching {
            tts.voices.orEmpty()
                .filter { it.locale.language == "ar" }
                .sortedWith(compareBy({ it.isNetworkConnectionRequired }, { it.name }))
        }.getOrDefault(emptyList())
        voices = arabicVoices.map { it.name }
        val chosen = arabicVoices.firstOrNull { it.name == voiceName } ?: guessMale(arabicVoices)
        chosen?.let { runCatching { tts.voice = it; voiceName = it.name } }
        tts.setPitch(pitch)
    }

    /** Engines rarely label gender, so use any hint available, else the first voice. */
    private fun guessMale(list: List<Voice>): Voice? = list.firstOrNull { v ->
        val n = v.name.lowercase()
        ("male" in n && "female" !in n) || v.features.orEmpty().any { it.lowercase().let { f -> "male" in f && "female" !in f } }
    } ?: list.firstOrNull()

    fun selectVoice(name: String) {
        arabicVoices.firstOrNull { it.name == name }?.let {
            runCatching { tts.voice = it }
            voiceName = name
            prefs.edit().putString("voice", name).apply()
        }
    }

    fun updatePitch(value: Float) {
        pitch = value
        tts.setPitch(value)
        prefs.edit().putFloat("pitch", value).apply()
    }

    private fun prepareTts() {
        tts.setSpeechRate(if (slow) 0.5f else 0.8f)
        tts.setPitch(pitch)
    }

    /** Queues [parts] with short pauses between them, the whole sequence [times] times. */
    private fun sayParts(parts: List<String>, times: Int = 1) {
        prepareTts()
        var first = true
        for (t in 0 until times) {
            parts.forEachIndexed { i, part ->
                tts.speak(part, if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "p_${t}_$i")
                first = false
                if (i < parts.size - 1) tts.playSilentUtterance(380, TextToSpeech.QUEUE_ADD, null)
            }
            if (t < times - 1) tts.playSilentUtterance(800, TextToSpeech.QUEUE_ADD, null)
        }
    }

    /**
     * Plays the item [times] times. With [spelling], each letter is named with its
     * marks first (Furqan style) and then the whole item is read.
     */
    fun speak(item: QItem, times: Int = 1, spelling: List<String>? = null) {
        stopPlayer()
        val clip = "${item.id}.ogg"
        if (spelling.isNullOrEmpty() && clip in clips) {
            playAsset("audio/$clip", times)
            return
        }
        sayParts(spelling.orEmpty() + item.speak, times)
    }

    fun say(text: String) {
        stopPlayer()
        sayParts(listOf(text))
    }

    /** Lesson opening: Bismillah (lesson 1) then the lesson title. */
    fun playIntro(title: String, withBasmala: Boolean) {
        stop()
        if (!withBasmala) {
            sayParts(listOf(title))
            return
        }
        runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(BASMALA_URL)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { mp ->
                    mp.release()
                    if (player === mp) player = null
                    sayParts(listOf(title))
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (player === mp) player = null
                    sayParts(listOf(BASMALA_TEXT, title))
                    true
                }
                prepareAsync()
            }
        }.onFailure { sayParts(listOf(BASMALA_TEXT, title)) }
    }

    private fun playAsset(path: String, times: Int) {
        var remaining = times
        runCatching {
            appContext.assets.openFd(path).use { fd ->
                player = MediaPlayer().apply {
                    setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    if (slow && Build.VERSION.SDK_INT >= 23) playbackParams = playbackParams.setSpeed(0.7f)
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

    private fun stopPlayer() {
        player?.runCatching { stop(); release() }
        player = null
    }

    fun stop() {
        stopPlayer()
        tts.stop()
    }

    fun installVoiceIntent(): Intent =
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun shutdown() {
        stop()
        tts.shutdown()
    }
}
