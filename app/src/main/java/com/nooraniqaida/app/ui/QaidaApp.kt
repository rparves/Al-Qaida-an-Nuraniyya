package com.nooraniqaida.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Lesson
import com.nooraniqaida.app.data.QItem
import com.nooraniqaida.app.data.Qaida
import com.nooraniqaida.app.ml.PronunciationChecker
import kotlinx.coroutines.delay

private const val HOME = -1
private const val WRITING = -2

@Composable
fun QaidaApp(speaker: Speaker) {
    // Saveable so the screen survives the activity recreate after a language switch.
    var route by rememberSaveable { mutableIntStateOf(HOME) }
    BackHandler(enabled = route != HOME) { route = HOME }
    when (route) {
        HOME -> HomeScreen(speaker, onLesson = { route = it }, onWriting = { route = WRITING })
        WRITING -> WritingScreen(speaker, onBack = { route = HOME })
        else -> LessonScreen(Qaida.lessons[route], speaker, onBack = { route = HOME })
    }
}

@Composable
fun isBangla(): Boolean = LocalConfiguration.current.locales[0].language == "bn"

fun arabicStyle(size: TextUnit) = TextStyle(
    fontSize = size,
    lineHeight = size * 1.6f,
    textAlign = TextAlign.Center,
    textDirection = TextDirection.Rtl,
)

// ---------------------------------------------------------------- Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(speaker: Speaker, onLesson: (Int) -> Unit, onWriting: () -> Unit) {
    val bn = isBangla()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(if (bn) "en" else "bn")
                        )
                    }) { Text(stringResource(R.string.switch_language)) }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (speaker.status) {
                Speaker.Status.MISSING_ARABIC -> item {
                    NoticeCard(
                        title = stringResource(R.string.tts_missing_title),
                        body = stringResource(R.string.tts_missing_desc),
                        action = stringResource(R.string.install_voice),
                    ) { runCatching { context.startActivity(speaker.installVoiceIntent()) } }
                }
                Speaker.Status.UNAVAILABLE -> item {
                    NoticeCard(title = stringResource(R.string.tts_unavailable), body = null, action = null) {}
                }
                else -> {}
            }

            item {
                Text(stringResource(R.string.lessons), style = MaterialTheme.typography.titleMedium)
            }

            items(Qaida.lessons.indices.toList()) { index ->
                val lesson = Qaida.lessons[index]
                MenuCard(
                    badge = lesson.number.toString(),
                    title = stringResource(lesson.titleRes),
                    subtitle = stringResource(lesson.descRes),
                    arabic = lesson.titleAr,
                ) { onLesson(index) }
            }

            item {
                MenuCard(
                    badge = "✍",
                    title = stringResource(R.string.writing_title),
                    subtitle = stringResource(R.string.writing_desc),
                    arabic = "الكتابة",
                    onClick = onWriting,
                )
            }
        }
    }
}

@Composable
private fun MenuCard(badge: String, title: String, subtitle: String, arabic: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) { Text(badge, color = MaterialTheme.colorScheme.onPrimary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Text(arabic, style = arabicStyle(20.sp), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NoticeCard(title: String, body: String?, action: String?, onAction: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            body?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            action?.let { TextButton(onClick = onAction) { Text(it) } }
        }
    }
}

// ---------------------------------------------------------------- Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonScreen(lesson: Lesson, speaker: Speaker, onBack: () -> Unit) {
    val bn = isBangla()
    var selected by remember { mutableStateOf<QItem?>(null) }
    var playingAll by remember { mutableStateOf(false) }

    LaunchedEffect(playingAll) {
        if (!playingAll) return@LaunchedEffect
        for (item in lesson.items) {
            selected = item
            speaker.speak(item)
            delay(if (speaker.slow) 1900 else 1300)
        }
        playingAll = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(lesson.titleRes), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.lesson_n, lesson.number), style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = { speaker.slow = !speaker.slow }) {
                        Text(if (speaker.slow) "🐢 " + stringResource(R.string.slow) else "🐇")
                    }
                    IconButton(onClick = { playingAll = !playingAll }) {
                        if (playingAll) Icon(Icons.Filled.Close, stringResource(R.string.stop))
                        else Icon(Icons.Filled.PlayArrow, stringResource(R.string.play_all))
                    }
                },
            )
        },
        bottomBar = { selected?.let { PracticePanel(it, speaker, bn) } },
    ) { pad ->
        // Arabic is read right to left, so the grid flows right to left too.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(lesson.columns),
                modifier = Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(lesson.items, key = { it.id }) { item ->
                    LetterCell(item, item == selected, bn) {
                        playingAll = false
                        selected = item
                        speaker.speak(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterCell(item: QItem, selected: Boolean, bn: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(item.display, style = arabicStyle(if (item.display.length > 4) 26.sp else 36.sp))
            Text(
                if (bn) item.bn else item.en,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PracticePanel(item: QItem, speaker: Speaker, bn: Boolean) {
    val context = LocalContext.current
    val checker = remember { PronunciationChecker(context) }
    DisposableEffect(item.id) { onDispose { checker.cancel() } }

    var feedback by remember(item.id) { mutableStateOf<String?>(null) }
    var listening by remember(item.id) { mutableStateOf(false) }

    fun startListening() {
        listening = true
        feedback = null
        checker.listen(item) { r ->
            when (r) {
                PronunciationChecker.Result.Listening -> feedback = context.getString(R.string.listening)
                is PronunciationChecker.Result.Heard -> {
                    listening = false
                    val verdict = if (r.matched) "✅ " + context.getString(R.string.correct)
                    else "🔁 " + context.getString(R.string.try_again)
                    feedback = if (r.best.isBlank()) verdict
                    else verdict + "\n" + context.getString(R.string.heard_x, r.best)
                }
                is PronunciationChecker.Result.Failed -> {
                    listening = false
                    feedback = speechErrorMessage(context, r.code)
                }
            }
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else feedback = context.getString(R.string.mic_permission_needed)
    }

    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.display, style = arabicStyle(40.sp), modifier = Modifier.padding(end = 14.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (bn) item.bn else item.en, style = MaterialTheme.typography.titleLarge)
                    item.harf?.let { h ->
                        if (!item.id.startsWith("l_")) {
                            Text(
                                stringResource(R.string.of_letter, if (bn) h.nameBn else h.nameEn),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            stringResource(R.string.makhraj_label, stringResource(h.makhraj.titleRes)),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(stringResource(h.makhraj.descRes), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.size(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { speaker.speak(item) }, modifier = Modifier.weight(1f)) {
                    Text("🔊 " + stringResource(R.string.listen))
                }
                OutlinedButton(
                    enabled = !listening,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        when {
                            !checker.isAvailable() -> feedback = context.getString(R.string.speech_unavailable)
                            granted -> startListening()
                            else -> permission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                ) { Text("🎤 " + stringResource(R.string.say_it)) }
            }
            feedback?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

private fun speechErrorMessage(context: Context, code: Int): String = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
        context.getString(R.string.speech_no_match)
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
        context.getString(R.string.speech_network)
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.mic_permission_needed)
    12, 13 -> context.getString(R.string.speech_lang_missing) // LANGUAGE_NOT_SUPPORTED / LANGUAGE_UNAVAILABLE
    else -> context.getString(R.string.speech_error, code)
}
