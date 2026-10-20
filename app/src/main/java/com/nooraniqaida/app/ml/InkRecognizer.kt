package com.nooraniqaida.app.ml

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionContext
import com.google.mlkit.vision.digitalink.WritingArea
import kotlinx.coroutines.tasks.await

data class InkPoint(val x: Float, val y: Float, val t: Long)

/**
 * On-device Arabic handwriting recognition with ML Kit Digital Ink.
 * The model is downloaded once on first use, so it adds nothing to the install size.
 */
class InkRecognizer {

    private val model: DigitalInkRecognitionModel? = runCatching {
        DigitalInkRecognitionModelIdentifier.fromLanguageTag("ar")
    }.getOrNull()?.let { DigitalInkRecognitionModel.builder(it).build() }

    private val recognizer = model?.let {
        DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(it).build())
    }

    private val manager = RemoteModelManager.getInstance()

    /** Returns true when the model is ready. Calls [onDownloading] if a download starts. */
    suspend fun ensureModel(onDownloading: () -> Unit): Boolean {
        val m = model ?: return false
        return runCatching {
            if (!manager.isModelDownloaded(m).await()) {
                onDownloading()
                manager.download(m, DownloadConditions.Builder().build()).await()
            }
            true
        }.getOrDefault(false)
    }

    suspend fun recognize(strokes: List<List<InkPoint>>, width: Float, height: Float): List<String> {
        val r = recognizer ?: return emptyList()
        val ink = Ink.builder().apply {
            strokes.forEach { stroke ->
                val sb = Ink.Stroke.builder()
                stroke.forEach { p -> sb.addPoint(Ink.Point.create(p.x, p.y, p.t)) }
                addStroke(sb.build())
            }
        }.build()
        val ctx = RecognitionContext.builder()
            .setPreContext("")
            .setWritingArea(WritingArea(width, height))
            .build()
        return runCatching { r.recognize(ink, ctx).await().candidates.map { it.text } }
            .getOrDefault(emptyList())
    }

    fun close() {
        recognizer?.close()
    }
}
