package com.nooraniqaida.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Lesson
import com.nooraniqaida.app.data.Progress
import com.nooraniqaida.app.data.Qaida
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    speaker: Speaker,
    progress: Progress,
    onLesson: (Int) -> Unit,
    onQuiz: (Int) -> Unit,
    onTrace: () -> Unit,
) {
    val lang = appLang()
    val context = LocalContext.current
    val lessons = Qaida.lessons
    val mastered = progress.mastered
    var filter by rememberSaveable { mutableIntStateOf(0) } // 0 all, 1 remaining, 2 mastered
    var confirmReset by remember { mutableStateOf(false) }
    var showVoice by remember { mutableStateOf(false) }

    val shown = when (filter) {
        1 -> lessons.filter { it.number !in mastered }
        2 -> lessons.filter { it.number in mastered }
        else -> lessons
    }
    val next = lessons.firstOrNull { it.number !in mastered } ?: lessons.first()
    val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

    GardenBackground {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(330.dp),
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 34.dp, bottom = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = full) { HeaderCard(progress, onVoice = { showVoice = true }) { confirmReset = true } }

            when (speaker.status) {
                Speaker.Status.MISSING_ARABIC -> item(span = full) {
                    NoticeCard(
                        stringResource(R.string.tts_missing_title),
                        stringResource(R.string.tts_missing_desc),
                        stringResource(R.string.install_voice),
                    ) { runCatching { context.startActivity(speaker.installVoiceIntent()) } }
                }
                Speaker.Status.UNAVAILABLE -> item(span = full) {
                    NoticeCard(stringResource(R.string.tts_unavailable), null, null) {}
                }
                else -> {}
            }

            item(span = full) {
                ModeCards(
                    onListen = { onLesson(next.number) },
                    onQuiz = { onQuiz(next.number) },
                    onTrace = onTrace,
                )
            }

            item(span = full) {
                LessonsHeader(filter, lessons.size, mastered.size) { filter = it }
            }

            if (shown.isEmpty()) {
                item(span = full) {
                    Text(
                        "🌱 " + stringResource(R.string.empty_filter),
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = Garden.Muted,
                    )
                }
            }

            items(shown, key = { it.number }) { lesson ->
                LessonCard(
                    lesson = lesson,
                    mastered = lesson.number in mastered,
                    best = progress.best(lesson.number),
                    lang = lang,
                    onLesson = onLesson,
                    onQuiz = onQuiz,
                    onTrace = onTrace,
                )
            }
        }
    }

    if (showVoice) VoiceDialog(speaker) { showVoice = false }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_progress)) },
            text = { Text(stringResource(R.string.reset_confirm)) },
            confirmButton = {
                TextButton(onClick = { progress.reset(); confirmReset = false }) {
                    Text(stringResource(R.string.reset), color = Garden.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderCard(progress: Progress, onVoice: () -> Unit, onReset: () -> Unit) {
    val cheers = stringArrayResource(R.array.cheers)
    var cheer by remember { mutableStateOf<String?>(null) }
    var bounce by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (bounce) 1.3f else 1f, label = "sun")
    LaunchedEffect(bounce) { if (bounce) { delay(220); bounce = false } }

    GardenCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "☀️",
                fontSize = 50.sp,
                modifier = Modifier
                    .scale(scale)
                    .clip(CircleShape)
                    .clickable { cheer = cheers.random(); bounce = true }
                    .padding(4.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    (if (progress.name.isBlank()) stringResource(R.string.welcome)
                    else stringResource(R.string.welcome_name, progress.name)) + " 🌸",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Garden.GreenDark,
                )
                Text(
                    cheer ?: (stringResource(R.string.tap_sun) + " 🌟"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Garden.Green,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NameField(progress)
            val done = progress.mastered.size
            val total = Qaida.lessons.size
            Pill(
                "🏆 " + stringResource(R.string.progress, done, total, done * 100 / total),
                Garden.PurpleSoft, Garden.Purple, Garden.PurpleLine,
                Modifier.align(Alignment.CenterVertically),
            )
            Pill(
                "⭐ " + stringResource(R.string.stars, progress.stars),
                Garden.YellowSoft, Garden.Brown, Garden.Yellow,
                Modifier.align(Alignment.CenterVertically),
            )
            Pill(
                "🎙️ " + stringResource(R.string.voice_settings),
                Garden.MintSoft, Garden.GreenDark, Garden.Mint,
                Modifier.align(Alignment.CenterVertically).clip(RoundedCornerShape(50)).clickable(onClick = onVoice),
            )
            IconButton(onClick = onReset, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(Icons.Filled.Refresh, stringResource(R.string.reset_progress), tint = Garden.Orange)
            }
        }
        Spacer(Modifier.height(10.dp))
        LanguageSwitch()
    }
}

@Composable
private fun NameField(progress: Progress) {
    var text by remember { mutableStateOf(progress.name) }
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(Garden.YellowSoft)
            .border(1.5.dp, Garden.Yellow, shape)
            .padding(start = 12.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "🏷️ " + stringResource(R.string.your_name) + ":",
            fontWeight = FontWeight.ExtraBold,
            color = Garden.Brown,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .width(160.dp)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Garden.Yellow, shape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            if (text.isEmpty()) {
                Text(stringResource(R.string.name_hint), color = Garden.Muted, style = MaterialTheme.typography.bodyMedium)
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it.take(30); progress.updateName(text) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Garden.Ink, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LanguageSwitch() {
    val lang = appLang()
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("ar" to "العربية", "en" to "English", "bn" to "বাংলা").forEach { (tag, label) ->
            val selected = tag == lang
            Text(
                label,
                color = if (selected) Color.White else Garden.GreenDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) Garden.Green else Garden.MintSoft)
                    .clickable(enabled = !selected) {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun NoticeCard(title: String, body: String?, action: String?, onAction: () -> Unit) {
    GardenCard(borderColor = Garden.Red) {
        Text("🔇 $title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Garden.Ink)
        body?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Garden.Muted) }
        action?.let { TextButton(onClick = onAction) { Text(it, fontWeight = FontWeight.Bold) } }
    }
}

@Composable
private fun ModeCards(onListen: () -> Unit, onQuiz: () -> Unit, onTrace: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cards: @Composable (Modifier) -> Unit = { m ->
            ModeCard(1, "🎧", stringResource(R.string.mode_listen_title), stringResource(R.string.mode_listen_desc),
                stringResource(R.string.mode_listen_tag), Garden.listenBrush, Garden.MintSoft, Garden.GreenDark, m, onListen)
            ModeCard(2, "🧠", stringResource(R.string.mode_quiz_title), stringResource(R.string.mode_quiz_desc),
                stringResource(R.string.mode_quiz_tag), Garden.quizBrush, Color.White, Garden.Brown, m, onQuiz)
            ModeCard(3, "✍️", stringResource(R.string.mode_trace_title), stringResource(R.string.mode_trace_desc),
                stringResource(R.string.mode_trace_tag), Garden.traceBrush, Garden.Yellow, Garden.Brown, m, onTrace)
        }
        if (maxWidth > 760.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) { cards(Modifier.weight(1f)) }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { cards(Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun ModeCard(
    n: Int, icon: String, title: String, desc: String, tag: String,
    brush: Brush, tagBg: Color, tagFg: Color, modifier: Modifier, onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier
            .shadow(6.dp, shape)
            .clip(shape)
            .background(brush)
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) { Text(icon, fontSize = 28.sp) }
                Spacer(Modifier.width(12.dp))
                Text(
                    "$n. $title",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Pill(tag, tagBg, tagFg)
            }
            Spacer(Modifier.height(10.dp))
            Text(desc, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LessonsHeader(filter: Int, total: Int, mastered: Int, onFilter: (Int) -> Unit) {
    GardenCard {
        Text(
            "📖 " + stringResource(R.string.lessons_title, total),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Garden.GreenDark,
        )
        Text(stringResource(R.string.lessons_sub), style = MaterialTheme.typography.bodyMedium, color = Garden.Muted)
        Spacer(Modifier.height(12.dp))
        val labels = listOf(
            stringResource(R.string.filter_all, total),
            stringResource(R.string.filter_remaining, total - mastered),
            "⭐ " + stringResource(R.string.filter_mastered, mastered),
        )
        val shape = RoundedCornerShape(50)
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .clip(shape)
                .background(Color(0xFFF2FBF5))
                .border(1.5.dp, Garden.Mint, shape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            labels.forEachIndexed { i, label ->
                val selected = i == filter
                Text(
                    label,
                    color = if (selected) Color.White else Garden.GreenDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(shape)
                        .background(if (selected) Garden.Green else Color.Transparent)
                        .clickable { onFilter(i) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonCard(
    lesson: Lesson,
    mastered: Boolean,
    best: Int,
    lang: String,
    onLesson: (Int) -> Unit,
    onQuiz: (Int) -> Unit,
    onTrace: () -> Unit,
) {
    GardenCard(borderColor = if (mastered) Garden.Yellow else Garden.Mint) {
        Row(verticalAlignment = Alignment.Top) {
            FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Pill(stringResource(R.string.lesson_n, lesson.number), Garden.MintSoft, Garden.GreenDark, Garden.Mint)
                Pill("🎧 " + stringResource(R.string.interactive), Garden.YellowSoft, Garden.Brown, Garden.Yellow)
                if (mastered) Pill("⭐ " + stringResource(R.string.mastered_badge), Color(0xFFFFE9A8), Garden.Brown)
            }
            val iconShape = RoundedCornerShape(16.dp)
            Box(
                Modifier.size(52.dp).clip(iconShape).background(Color(0xFFF6F8F7)).border(1.dp, Color(0xFFE3EAE6), iconShape),
                contentAlignment = Alignment.Center,
            ) { Text(lesson.icon, fontSize = 24.sp) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            lesson.titleAr,
            style = quranStyle(24.sp, if (lang == "ar") TextAlign.Right else TextAlign.Left),
            fontWeight = FontWeight.Bold,
            color = Garden.Ink,
            modifier = Modifier.fillMaxWidth(),
        )
        if (lang != "ar") {
            Text(lesson.title.pick(lang), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold, color = Garden.Green)
        }
        Text(lesson.desc.pick(lang), style = MaterialTheme.typography.bodyMedium, color = Garden.Muted)
        if (best > 0) {
            Text(stringResource(R.string.best_score, best), style = MaterialTheme.typography.labelMedium,
                color = Garden.Brown, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF3EF))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton("🔊 " + stringResource(R.string.start_lesson), Garden.buttonBrush, Modifier.weight(1f)) {
                onLesson(lesson.number)
            }
            if (lesson.number == 1) {
                EmojiButton("✍️", Color(0xFFDDF4F1), Color(0xFF7CCFC4), onTrace)
            }
            EmojiButton("🧠") { onQuiz(lesson.number) }
        }
    }
}


@Composable
private fun VoiceDialog(speaker: Speaker, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎙️ " + stringResource(R.string.voice_title), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.voice_hint), style = MaterialTheme.typography.bodySmall, color = Garden.Muted)
                Spacer(Modifier.height(8.dp))
                if (speaker.voices.isEmpty()) {
                    Text(stringResource(R.string.no_voices), color = Garden.Red)
                }
                speaker.voices.forEachIndexed { i, name ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { speaker.selectVoice(name) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = name == speaker.voiceName, onClick = { speaker.selectVoice(name) })
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.voice_n, i + 1), fontWeight = FontWeight.Bold)
                            Text(
                                name + if (name.contains("network")) " · " + stringResource(R.string.voice_online) else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Garden.Muted,
                            )
                        }
                        TextButton(onClick = { speaker.selectVoice(name); speaker.say("أَلِفْ، بَا، تَا") }) {
                            Text("▶ " + stringResource(R.string.voice_test))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.pitch_label), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.deeper), style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = speaker.pitch,
                        onValueChange = { speaker.updatePitch(it) },
                        onValueChangeFinished = { speaker.say("بِسْمِ اللَّهِ") },
                        valueRange = 0.6f..1.2f,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                    Text(stringResource(R.string.higher), style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) } },
    )
}
