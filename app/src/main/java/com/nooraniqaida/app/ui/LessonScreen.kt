package com.nooraniqaida.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Lesson
import com.nooraniqaida.app.data.QItem
import com.nooraniqaida.app.ml.PronunciationChecker
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(lesson: Lesson, speaker: Speaker, onBack: () -> Unit, onQuiz: () -> Unit) {
    val lang = appLang()
    var selected by remember { mutableStateOf<QItem?>(null) }
    var playingAll by remember { mutableStateOf(false) }
    var times by rememberSaveable { mutableIntStateOf(1) }

    LaunchedEffect(playingAll) {
        if (!playingAll) return@LaunchedEffect
        for (item in lesson.items) {
            selected = item
            speaker.speak(item, times)
            delay((if (speaker.slow) 1900L else 1300L) * times)
        }
        playingAll = false
    }
    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    GardenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Column {
                            Text(
                                if (lang == "ar") lesson.title.ar else lesson.title.pick(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Garden.GreenDark,
                                maxLines = 1,
                            )
                            Text(stringResource(R.string.lesson_n, lesson.number),
                                style = MaterialTheme.typography.labelSmall, color = Garden.Muted)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Garden.GreenDark)
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp),
                )
            },
            bottomBar = { selected?.let { PracticePanel(it, speaker, lang, times) } },
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) {
                // Controls: repeat count, speed, play all, quiz.
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Chip("🔁 ${stringResource(R.string.repeat)} ×$times", times > 1) { times = if (times >= 3) 1 else times + 1 }
                    Chip(if (speaker.slow) "🐢 " + stringResource(R.string.slow) else "🐇", speaker.slow) { speaker.slow = !speaker.slow }
                    Spacer(Modifier.weight(1f))
                    Chip(if (playingAll) "⏹ " + stringResource(R.string.stop) else "▶ " + stringResource(R.string.play_all), playingAll) {
                        playingAll = !playingAll
                    }
                    EmojiButton("🧠", onClick = onQuiz)
                }
                Spacer(Modifier.height(8.dp))
                // Arabic is read right to left, so the grid flows right to left too.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(lesson.columns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(lesson.items, key = { it.id }) { item ->
                            LetterCell(item, item == selected, lang) {
                                playingAll = false
                                selected = item
                                speaker.speak(item, times)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, on: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        color = if (on) Color.White else Garden.GreenDark,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(if (on) Garden.Green else Color.White)
            .border(1.5.dp, Garden.Mint, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun LetterCell(item: QItem, selected: Boolean, lang: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Garden.MintSoft else Color.White)
            .border(2.dp, if (selected) Garden.Green else Garden.Mint, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val long = item.display.length > 6
        Text(item.display, style = quranStyle(if (long) 24.sp else if (item.display.length > 3) 30.sp else 38.sp), color = Garden.Ink)
        if (lang != "ar") {
            Text(
                if (lang == "bn") item.bn else item.en,
                style = MaterialTheme.typography.labelMedium,
                color = Garden.Green,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PracticePanel(item: QItem, speaker: Speaker, lang: String, times: Int) {
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
                    feedback = if (r.best.isBlank()) verdict else verdict + "\n" + context.getString(R.string.heard_x, r.best)
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

    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(2.dp, Garden.Mint, shape)
            .navigationBarsPadding()
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.display, style = quranStyle(40.sp), color = Garden.Ink, modifier = Modifier.padding(end = 14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when (lang) { "bn" -> item.bn; else -> item.en },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Garden.GreenDark,
                )
                item.harf?.let { h ->
                    if (!item.id.startsWith("l_")) {
                        Text(stringResource(R.string.of_letter, if (lang == "bn") h.nameBn else if (lang == "ar") h.nameAr else h.nameEn),
                            style = MaterialTheme.typography.bodySmall, color = Garden.Muted)
                    }
                    Text(stringResource(R.string.makhraj_label, stringResource(h.makhraj.titleRes)),
                        style = MaterialTheme.typography.bodySmall, color = Garden.Green, fontWeight = FontWeight.Bold)
                    Text(stringResource(h.makhraj.descRes), style = MaterialTheme.typography.bodySmall, color = Garden.Muted)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton("🔊 " + stringResource(R.string.listen), Garden.buttonBrush, Modifier.weight(1f)) {
                speaker.speak(item, times)
            }
            GradientButton("🎤 " + stringResource(R.string.say_it), Garden.quizBrush, Modifier.weight(1f)) {
                if (listening) return@GradientButton
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
                when {
                    !checker.isAvailable() -> feedback = context.getString(R.string.speech_unavailable)
                    granted -> startListening()
                    else -> permission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
        feedback?.let {
            Text(it, modifier = Modifier.padding(top = 10.dp), color = Garden.Ink, fontWeight = FontWeight.Bold)
        }
    }
}
