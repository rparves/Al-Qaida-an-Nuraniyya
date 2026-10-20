package com.nooraniqaida.app.ml

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nooraniqaida.app.data.ArabicText
import com.nooraniqaida.app.data.QItem

/**
 * Pronunciation check using the platform speech recogniser (Google's on-device /
 * cloud ASR model). Nothing is bundled in the APK. On Android 13+ the expected
 * answer is passed as biasing text so short syllables are recognised more reliably.
 */
class PronunciationChecker(private val context: Context) {

    sealed interface Result {
        data object Listening : Result
        data class Heard(val matched: Boolean, val best: String) : Result
        data class Failed(val code: Int) : Result
    }

    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(item: QItem, onResult: (Result) -> Unit) {
        cancel()
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onResult(Result.Listening)
            override fun onResults(results: Bundle?) {
                val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                onResult(Result.Heard(ArabicText.matches(heard, item.accept), heard.firstOrNull().orEmpty()))
            }
            override fun onError(error: Int) = onResult(Result.Failed(error))
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            if (Build.VERSION.SDK_INT >= 33) {
                putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, ArrayList(item.accept + item.speak))
            }
        }
        r.startListening(intent)
    }

    fun cancel() {
        recognizer?.destroy()
        recognizer = null
    }
}
