package com.nooraniqaida.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.ArabicText
import com.nooraniqaida.app.data.Qaida
import com.nooraniqaida.app.ml.InkPoint
import com.nooraniqaida.app.ml.InkRecognizer
import kotlinx.coroutines.launch

private enum class ModelState { CHECKING, DOWNLOADING, READY, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(speaker: Speaker, onBack: () -> Unit) {
    val bn = isBangla()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recognizer = remember { InkRecognizer() }
    DisposableEffect(Unit) { onDispose { recognizer.close() } }

    val targets = remember { Qaida.letters.filter { it.id != "hamza" } }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val target = targets[index]

    var modelState by remember { mutableStateOf(ModelState.CHECKING) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        modelState = ModelState.CHECKING
        val ok = recognizer.ensureModel { modelState = ModelState.DOWNLOADING }
        modelState = if (ok) ModelState.READY else ModelState.FAILED
    }

    val strokes = remember { mutableStateListOf<SnapshotStateList<InkPoint>>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var result by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    var showGuide by rememberSaveable { mutableStateOf(true) }

    fun reset() {
        strokes.clear()
        result = null
    }

    val inkColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val measurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.writing_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.write_x, if (bn) target.nameBn else target.nameEn),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(target.char, style = arabicStyle(36.sp))
                IconButton(onClick = { speaker.speak(Qaida.letterItem(target)) }) { Text("🔊", fontSize = 22.sp) }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        // Taps are kept as one-point strokes: the dots are what tell ب ت ث apart.
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()
                            val stroke = mutableStateListOf(
                                InkPoint(down.position.x, down.position.y, System.currentTimeMillis())
                            )
                            strokes.add(stroke)
                            result = null
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                stroke.add(InkPoint(change.position.x, change.position.y, System.currentTimeMillis()))
                                change.consume()
                            }
                        }
                    },
            ) {
                if (showGuide) {
                    val layout = measurer.measure(
                        target.char,
                        TextStyle(fontSize = (size.minDimension * 0.5f).toSp(), color = guideColor),
                    )
                    drawText(
                        layout,
                        topLeft = Offset(
                            (size.width - layout.size.width) / 2f,
                            (size.height - layout.size.height) / 2f,
                        ),
                    )
                }
                strokes.forEach { s ->
                    if (s.size == 1) {
                        drawCircle(inkColor, radius = 10f, center = Offset(s[0].x, s[0].y))
                    } else {
                        val path = Path().apply {
                            moveTo(s[0].x, s[0].y)
                            for (i in 1 until s.size) lineTo(s[i].x, s[i].y)
                        }
                        drawPath(path, inkColor, style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showGuide, onCheckedChange = { showGuide = it })
                Text(stringResource(R.string.show_guide))
            }

            when (modelState) {
                ModelState.CHECKING -> Text(stringResource(R.string.model_checking))
                ModelState.DOWNLOADING -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.model_downloading))
                }
                ModelState.FAILED -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.model_failed), textAlign = TextAlign.Center)
                    TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.retry)) }
                }
                ModelState.READY -> {}
            }

            result?.let { Text(it, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { reset() }) { Text(stringResource(R.string.clear)) }
                Button(
                    enabled = modelState == ModelState.READY && strokes.isNotEmpty() && !checking,
                    onClick = {
                        scope.launch {
                            checking = true
                            val candidates = recognizer.recognize(
                                strokes.map { it.toList() },
                                canvasSize.width.toFloat(),
                                canvasSize.height.toFloat(),
                            )
                            val expected = ArabicText.normalize(target.char)
                            result = when {
                                candidates.take(2).any { ArabicText.normalize(it).contains(expected) } ->
                                    "✅ " + context.getString(R.string.write_correct, target.char)
                                candidates.isEmpty() -> context.getString(R.string.write_unknown)
                                else -> "🔁 " + context.getString(R.string.write_wrong, candidates.first())
                            }
                            checking = false
                        }
                    },
                ) { Text(stringResource(R.string.check)) }
            }

            Row {
                TextButton(onClick = { index = (index - 1 + targets.size) % targets.size; reset() }) {
                    Text(stringResource(R.string.previous))
                }
                TextButton(onClick = { index = (index + 1) % targets.size; reset() }) {
                    Text(stringResource(R.string.next))
                }
            }
        }
    }
}
