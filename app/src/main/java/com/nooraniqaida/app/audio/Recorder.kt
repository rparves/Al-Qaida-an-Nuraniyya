package com.nooraniqaida.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Teacher's voice: a qualified teacher records items with proper makharij and tajweed.
 * Recordings (AAC, about 4 KB a second) live in filesDir/recordings/<item id>.m4a and
 * play instead of the phone voice. They can be shared as a zip and imported by students.
 */
class Recorder(context: Context) {
    private val appContext = context.applicationContext
    private var recorder: MediaRecorder? = null

    val dir: File = File(appContext.filesDir, "recordings").apply { mkdirs() }

    /** Id of the item being recorded right now, if any. */
    var recordingId by mutableStateOf<String?>(null)
        private set
    /** Bumped whenever recordings change, so the UI refreshes. */
    var version by mutableIntStateOf(0)
        private set

    fun file(id: String) = File(dir, "$id.m4a")
    private fun temp(id: String) = File(dir, "$id.part")

    fun has(id: String): Boolean = file(id).exists()

    fun count(): Int = dir.listFiles()?.count { it.name.endsWith(".m4a") } ?: 0

    fun start(id: String): Boolean {
        stop()
        return runCatching {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(appContext) else @Suppress("DEPRECATION") MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioChannels(1)
            r.setAudioSamplingRate(22050)
            r.setAudioEncodingBitRate(32000)
            r.setOutputFile(temp(id).absolutePath)
            r.prepare()
            r.start()
            recorder = r
            recordingId = id
            true
        }.getOrElse {
            recorder?.release()
            recorder = null
            false
        }
    }

    fun stop() {
        val id = recordingId ?: return
        val ok = runCatching { recorder?.stop() }.isSuccess
        recorder?.release()
        recorder = null
        recordingId = null
        val t = temp(id)
        if (ok && t.length() > 0) {
            file(id).delete()
            t.renameTo(file(id))
        } else {
            t.delete()
        }
        version++
    }

    fun delete(id: String) {
        file(id).delete()
        version++
    }

    /** Zips all recordings into the cache folder for sharing. */
    fun exportZip(): File? {
        val files = dir.listFiles()?.filter { it.name.endsWith(".m4a") }.orEmpty()
        if (files.isEmpty()) return null
        val out = File(File(appContext.cacheDir, "exports").apply { mkdirs() }, "noorani-qaida-teacher-voice.zip")
        ZipOutputStream(out.outputStream().buffered()).use { zip ->
            files.forEach { f ->
                zip.putNextEntry(ZipEntry(f.name))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        return out
    }

    /** Imports recordings from a shared zip. Only plain "<id>.m4a" entries are accepted. */
    fun importZip(uri: Uri): Int {
        val safe = Regex("^[a-z0-9_]+\\.m4a$")
        var n = 0
        runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input.buffered()).use { zip ->
                    while (true) {
                        val e = zip.nextEntry ?: break
                        val name = e.name.substringAfterLast('/')
                        if (!e.isDirectory && safe.matches(name)) {
                            File(dir, name).outputStream().use { zip.copyTo(it) }
                            n++
                        }
                        zip.closeEntry()
                    }
                }
            }
        }
        version++
        return n
    }
}
