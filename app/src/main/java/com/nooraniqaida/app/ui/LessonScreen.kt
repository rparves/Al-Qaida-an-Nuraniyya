package com.nooraniqaida.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.nooraniqaida.app.audio.Recorder
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Makhraj
import com.nooraniqaida.app.data.Sifa
import com.nooraniqaida.app.data.Tajweed
import com.nooraniqaida.app.data.Lesson
import com.nooraniqaida.app.data.QItem
import com.nooraniqaida.app.data.Spelling
import com.nooraniqaida.app.ml.PronunciationChecker
import kotlinx.coroutines.delay

/** Lessons 1 to 3 are letter names already; spelling starts with the harakat. */
private fun canSpell(lesson: Lesson) = lesson.number >= 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lesson: Lesson,
    speaker: Speaker,
    recorder: Recorder,
    teacherMode: Boolean,
    onBack: () -> Unit,
    onQuiz: () -> Unit,
) {
    val lang = appLang()
    var selected by remember { mutableStateOf<QItem?>(null) }
    var playingAll by remember { mutableStateOf(false) }
    var times by rememberSaveable { mutableIntStateOf(1) }
    var spell by rememberSaveable { mutableStateOf(canSpell(lesson)) }

    fun partsFor(item: QItem) = if (spell && canSpell(lesson)) Spelling.parts(Spelling.source(item)) else emptyList()
    fun play(item: QItem) = speaker.speak(item, times, partsFor(item).map { it.speak })

    // Say the lesson title when the lesson opens (with Bismillah first in lesson 1).
    LaunchedEffect(lesson.number) {
        delay(400)
        speaker.playIntro(lesson.titleAr, withBasmala = lesson.number == 1)
    }

    LaunchedEffect(playingAll) {
        if (!playingAll) return@LaunchedEffect
        for (item in lesson.items) {
            selected = item
            play(item)
            val units = partsFor(item).size + 1
            delay((if (speaker.slow) 1700L else 1150L) * units * times + 300L)
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
                    actions = { EmojiButton("🧠", onClick = onQuiz) },
                    modifier = Modifier.padding(top = 24.dp),
                )
            },
            bottomBar = {
                selected?.let { item ->
                    PracticePanel(
                        item, speaker, recorder, teacherMode, lang, times,
                        spellParts = if (canSpell(lesson)) Spelling.parts(Spelling.source(item)) else emptyList(),
                        showDiagramByDefault = lesson.number == 1,
                    )
                }
            },
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) {
                // Controls: repeat count, spelling, speed, play all.
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Chip("🔁 ${stringResource(R.string.repeat)} ×$times", times > 1) { times = if (times >= 5) 1 else times + 1 }
                    if (canSpell(lesson)) {
                        Chip("🔤 " + stringResource(R.string.spell), spell) { spell = !spell }
                    }
                    Chip(if (speaker.slow) "🐢 " + stringResource(R.string.slow) else "🐇", speaker.slow) { speaker.slow = !speaker.slow }
                    Chip(if (playingAll) "⏹ " + stringResource(R.string.stop) else "▶ " + stringResource(R.string.play_all), playingAll) {
                        playingAll = !playingAll
                    }
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
                        item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
                            LessonTitleCard(lesson, lang) {
                                speaker.playIntro(lesson.titleAr, withBasmala = lesson.number == 1)
                            }
                        }
                        items(lesson.items, key = { it.id }) { item ->
                            LetterCell(item, item == selected, lang) {
                                playingAll = false
                                selected = item
                                play(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonTitleCard(lesson: Lesson, lang: String, onPlay: () -> Unit) {
    GardenCard(borderColor = Garden.Yellow) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (lesson.number == 1) {
                Text(Speaker.BASMALA_TEXT, style = quranStyle(26.sp), color = Garden.GreenDark, fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 6.dp).width(120.dp), color = Garden.Yellow)
            }
            Text(lesson.titleAr, style = quranStyle(28.sp), color = Garden.Ink, fontWeight = FontWeight.Bold)
            if (lang != "ar") {
                Text(lesson.title.pick(lang), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = Garden.Green, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            CompositionLocalProvider(LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Chip("🔊 " + stringResource(R.string.play_title), false, onPlay)
                    lesson.rule?.let { rule ->
                        Column(
                            Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Garden.MintSoft)
                                .padding(12.dp),
                        ) {
                            Text("📏 " + stringResource(R.string.rule_title), style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold, color = Garden.GreenDark)
                            Text(rule.pick(lang), style = MaterialTheme.typography.bodyMedium, color = Garden.Ink)
                        }
                    }
                    Text(
                        "📚 " + stringResource(R.string.references) + "\n🔈 " + stringResource(R.string.tts_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = Garden.Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
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
        val len = item.display.length
        Text(item.display, style = quranStyle(if (len > 6) 24.sp else if (len > 3) 30.sp else 38.sp), color = Garden.Ink)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticePanel(
    item: QItem,
    speaker: Speaker,
    recorder: Recorder,
    teacherMode: Boolean,
    lang: String,
    times: Int,
    spellParts: List<Spelling.Part>,
    showDiagramByDefault: Boolean,
) {
    val context = LocalContext.current
    val checker = remember { PronunciationChecker(context) }
    DisposableEffect(item.id) {
        onDispose {
            checker.cancel()
            if (recorder.recordingId == item.id) recorder.stop()
        }
    }

    var feedback by remember(item.id) { mutableStateOf<String?>(null) }
    var listening by remember(item.id) { mutableStateOf(false) }
    var showDiagram by rememberSaveable { mutableStateOf(showDiagramByDefault) }
    var sifaInfo by remember(item.id) { mutableStateOf<Sifa?>(null) }
    var pendingRecord by remember { mutableStateOf(false) }
    val hasRecording = remember(recorder.version, item.id) { recorder.has(item.id) }
    val isRecording = recorder.recordingId == item.id

    fun startRecording() {
        speaker.stop()
        if (!recorder.start(item.id)) feedback = context.getString(R.string.recording_failed)
    }

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
        when {
            !granted -> feedback = context.getString(R.string.mic_permission_needed)
            pendingRecord -> startRecording()
            else -> startListening()
        }
        pendingRecord = false
    }
    fun micGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(2.dp, Garden.Mint, shape)
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.display, style = quranStyle(38.sp), color = Garden.Ink, modifier = Modifier.padding(end = 14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (lang == "bn") item.bn else item.en,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Garden.GreenDark,
                )
                item.harf?.let { h ->
                    if (!item.id.startsWith("l_")) {
                        Text(stringResource(R.string.of_letter, when (lang) { "bn" -> h.nameBn; "ar" -> h.nameAr; else -> h.nameEn }),
                            style = MaterialTheme.typography.bodySmall, color = Garden.Muted)
                    }
                    Text(stringResource(R.string.makhraj_label, h.makhraj.title.pick(lang)),
                        style = MaterialTheme.typography.bodySmall, color = Garden.Green, fontWeight = FontWeight.Bold)
                }
                if (hasRecording) {
                    Text("🎓 " + stringResource(R.string.recorded), style = MaterialTheme.typography.labelSmall,
                        color = Garden.Purple, fontWeight = FontWeight.Bold)
                }
            }
            if (item.harf != null) {
                EmojiButton(if (showDiagram) "🙂" else "👄", Garden.MintSoft, Garden.Mint) { showDiagram = !showDiagram }
            }
        }

        // Furqan-style spelling line, e.g. "Hamzah fatha · Baa sukoon → ab".
        if (spellParts.isNotEmpty()) {
            val joined = spellParts.joinToString("  ·  ") { when (lang) { "bn" -> it.bn; "ar" -> it.speak; else -> it.en } }
            Text(
                "🔤 $joined  →  " + if (lang == "ar") item.display.substringAfterLast('\n') else if (lang == "bn") item.bn else item.en,
                style = MaterialTheme.typography.bodyMedium,
                color = Garden.Brown,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Garden.YellowSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        // Makharij anatomy.
        val h = item.harf
        if (h != null && showDiagram) {
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                MakhrajDiagram(
                    h.makhraj,
                    secondary = if (Sifa.GHUNNAH in Tajweed.sifaat[h.id].orEmpty()) Makhraj.KHAYSHUM else null,
                    modifier = Modifier
                        .size(width = 150.dp, height = 135.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF8F0))
                        .border(1.5.dp, Garden.Mint, RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("👄 " + stringResource(R.string.makharij_title), style = MaterialTheme.typography.labelLarge,
                        color = Garden.Muted, fontWeight = FontWeight.Bold)
                    Text(h.makhraj.title.pick(lang), style = MaterialTheme.typography.titleMedium,
                        color = Garden.GreenDark, fontWeight = FontWeight.ExtraBold)
                    Text(h.makhraj.desc.pick(lang), style = MaterialTheme.typography.bodySmall, color = Garden.Ink)
                    if (lang != "ar") {
                        Text(h.makhraj.title.ar, style = quranStyle(15.sp, TextAlign.Left), color = Garden.Muted)
                    }
                }
            }
        }

        // Sifaat (letter qualities) and a pronunciation tip.
        if (h != null) {
            val sifaat = Tajweed.sifaat[h.id].orEmpty()
            if (sifaat.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    sifaat.forEach { sf ->
                        val heavy = sf == Sifa.HEAVY || sf == Sifa.ITBAQ
                        Pill(
                            sf.title.pick(lang),
                            if (sifaInfo == sf) Garden.Green else if (heavy) Color(0xFFFFE4CC) else Garden.MintSoft,
                            if (sifaInfo == sf) Color.White else Garden.GreenDark,
                            modifier = Modifier.clip(RoundedCornerShape(50)).clickable { sifaInfo = if (sifaInfo == sf) null else sf },
                        )
                    }
                }
                sifaInfo?.let { Text(it.desc.pick(lang), style = MaterialTheme.typography.bodySmall, color = Garden.Ink,
                    modifier = Modifier.padding(top = 4.dp)) }
            }
            Tajweed.tips[h.id]?.let {
                Text("💡 " + it.pick(lang), style = MaterialTheme.typography.bodySmall, color = Garden.Ink,
                    fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
            }
        }

        // Teacher mode: record this item with proper tajweed.
        if (teacherMode) {
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    if (isRecording) "⏹ " + stringResource(R.string.stop_record) else "⏺ " + stringResource(R.string.record),
                    if (isRecording) Brush.linearGradient(listOf(Color(0xFFE5484D), Color(0xFFC62828))) else Garden.traceBrush,
                    Modifier.weight(1f),
                ) {
                    when {
                        isRecording -> recorder.stop()
                        micGranted() -> startRecording()
                        else -> { pendingRecord = true; permission.launch(Manifest.permission.RECORD_AUDIO) }
                    }
                }
                if (hasRecording && !isRecording) {
                    TextButton(onClick = { recorder.delete(item.id) }) {
                        Text("🗑 " + stringResource(R.string.delete_recording), color = Garden.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton("🔊 " + stringResource(R.string.listen), Garden.buttonBrush, Modifier.weight(1f)) {
                speaker.speak(item, times)
            }
            if (spellParts.isNotEmpty()) {
                GradientButton("🔤 " + stringResource(R.string.spell), Garden.traceBrush, Modifier.weight(1f)) {
                    speaker.speak(item, times, spellParts.map { it.speak })
                }
            }
            GradientButton("🎤 " + stringResource(R.string.say_it), Garden.quizBrush, Modifier.weight(1f)) {
                if (listening || isRecording) return@GradientButton
                when {
                    !checker.isAvailable() -> feedback = context.getString(R.string.speech_unavailable)
                    micGranted() -> startListening()
                    else -> { pendingRecord = false; permission.launch(Manifest.permission.RECORD_AUDIO) }
                }
            }
        }
        feedback?.let {
            Text(it, modifier = Modifier.padding(top = 10.dp), color = Garden.Ink, fontWeight = FontWeight.Bold)
        }
    }
}
