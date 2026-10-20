package com.nooraniqaida.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Lesson
import com.nooraniqaida.app.data.Progress
import com.nooraniqaida.app.data.QItem
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ROUNDS = 10

private class Question(val answer: QItem, val options: List<QItem>)

private fun buildQuiz(lesson: Lesson, seed: Int): List<Question> {
    val rnd = Random(seed)
    val pool = lesson.items.distinctBy { it.display }
    val answers = generateSequence { pool.shuffled(rnd) }.flatten().take(ROUNDS).toList()
    return answers.map { a ->
        val others = pool.filter { it.display != a.display }.shuffled(rnd).take(3)
        Question(a, (others + a).shuffled(rnd))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(lesson: Lesson, speaker: Speaker, progress: Progress, onBack: () -> Unit) {
    val lang = appLang()
    var seed by rememberSaveable { mutableIntStateOf(Random.nextInt()) }
    val quiz = remember(seed) { buildQuiz(lesson, seed) }
    var round by rememberSaveable(seed) { mutableIntStateOf(0) }
    var score by rememberSaveable(seed) { mutableIntStateOf(0) }
    var picked by remember(seed, round) { mutableStateOf<QItem?>(null) }
    var passed by rememberSaveable(seed) { mutableStateOf<Boolean?>(null) }
    val finished = round >= ROUNDS

    // Play each new question automatically.
    LaunchedEffect(seed, round) {
        if (!finished) {
            delay(350)
            speaker.speak(quiz[round].answer)
        }
    }
    // After an answer, pause so the child sees the result, then move on.
    LaunchedEffect(picked) {
        if (picked != null) {
            delay(if (picked == quiz[round].answer) 900L else 1800L)
            round++
        }
    }
    LaunchedEffect(finished) {
        if (finished && passed == null) passed = progress.recordQuiz(lesson.number, score, ROUNDS)
    }
    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    GardenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            "🧠 " + stringResource(R.string.quiz) + " · " +
                                (if (lang == "ar") lesson.title.ar else lesson.title.pick(lang)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Garden.GreenDark,
                            maxLines = 1,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Garden.GreenDark)
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp),
                )
            },
        ) { pad ->
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (finished) {
                    Results(score, passed == true, onRetry = { seed = Random.nextInt() }, onBack = onBack)
                } else {
                    val q = quiz[round]
                    QuestionHeader(round, score)
                    BigSpeakerButton { speaker.speak(q.answer) }
                    Text(
                        stringResource(R.string.quiz_question),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Garden.GreenDark,
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            q.options.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { opt ->
                                        OptionCard(opt, q.answer, picked, Modifier.weight(1f)) {
                                            if (picked == null) {
                                                picked = opt
                                                if (opt == q.answer) score++ else speaker.speak(q.answer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    picked?.let { p ->
                        Text(
                            if (p == q.answer) "🎉 " + stringResource(R.string.correct)
                            else "💡 " + stringResource(R.string.quiz_wrong, q.answer.display.replace("\n", " ")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (p == q.answer) Garden.Green else Garden.Orange,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionHeader(round: Int, score: Int) {
    GardenCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.quiz_progress, round + 1, ROUNDS),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Garden.GreenDark,
                modifier = Modifier.weight(1f),
            )
            Pill("⭐ $score", Garden.YellowSoft, Garden.Brown, Garden.Yellow)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { round / ROUNDS.toFloat() },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
            color = Garden.Orange,
            trackColor = Garden.MintSoft,
        )
    }
}

@Composable
private fun BigSpeakerButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "speaker")
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(110.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Garden.quizBrush)
                .clickable { pressed = true; onClick() },
            contentAlignment = Alignment.Center,
        ) { Text("🔊", fontSize = 46.sp) }
        Text(stringResource(R.string.play_again), color = Garden.Muted, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun OptionCard(opt: QItem, answer: QItem, picked: QItem?, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val (bg, line) = when {
        picked == null -> Color.White to Garden.Mint
        opt == answer -> Color(0xFFD9F7E5) to Garden.Green
        opt == picked -> Garden.RedSoft to Garden.Red
        else -> Color(0xFFF7F7F7) to Color(0xFFE5E5E5)
    }
    Box(
        modifier
            .heightIn(min = 110.dp)
            .clip(shape)
            .background(bg)
            .border(2.5.dp, line, shape)
            .clickable(enabled = picked == null, onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        val len = opt.display.length
        Text(opt.display, style = quranStyle(if (len > 8) 24.sp else if (len > 3) 32.sp else 44.sp), color = Garden.Ink)
        if (picked != null && (opt == answer || opt == picked)) {
            Text(if (opt == answer) "✅" else "❌", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun Results(score: Int, passed: Boolean, onRetry: () -> Unit, onBack: () -> Unit) {
    val stars = when {
        score >= 9 -> 3
        score >= 7 -> 2
        score >= 5 -> 1
        else -> 0
    }
    GardenCard(borderColor = if (passed) Garden.Yellow else Garden.Mint) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (passed) "🏆" else if (stars > 0) "🌟" else "💪", fontSize = 64.sp)
            Text(
                stringResource(R.string.quiz_done, score, ROUNDS),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Garden.GreenDark,
                textAlign = TextAlign.Center,
            )
            Text("⭐".repeat(stars) + "☆".repeat(3 - stars), fontSize = 34.sp, color = Garden.Yellow)
            Text(
                if (passed) stringResource(R.string.quiz_mastered) else stringResource(R.string.quiz_keep),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (passed) Garden.Green else Garden.Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton("🔁 " + stringResource(R.string.try_quiz_again), Garden.quizBrush, Modifier.weight(1f), onRetry)
                GradientButton(stringResource(R.string.back), Garden.buttonBrush, Modifier.weight(1f), onBack)
            }
        }
    }
}
