package com.nooraniqaida.app.ui

import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nooraniqaida.app.R
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.ArabicText
import com.nooraniqaida.app.data.Qaida
import com.nooraniqaida.app.ml.InkPoint
import com.nooraniqaida.app.ml.InkRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max

private enum class ModelState { CHECKING, DOWNLOADING, READY, FAILED }

/** The target letter as an outline path, its filled area, and sample points inside it. */
private class Glyph(val path: android.graphics.Path, val region: Region, val samples: List<Offset>)

private fun buildGlyph(ch: String, size: IntSize): Glyph? {
    if (size.width == 0 || size.height == 0) return null
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size.height * 0.62f
        typeface = Typeface.DEFAULT_BOLD
    }
    val p = android.graphics.Path()
    paint.getTextPath(ch, 0, ch.length, 0f, 0f, p)
    val b = RectF()
    p.computeBounds(b, true)
    if (b.isEmpty) return null
    p.transform(Matrix().apply { setTranslate(size.width / 2f - b.centerX(), size.height / 2f - b.centerY()) })
    val region = Region().apply { setPath(p, Region(0, 0, size.width, size.height)) }
    val step = max(8, size.width / 60)
    val samples = ArrayList<Offset>()
    var y = 0
    while (y < size.height) {
        var x = 0
        while (x < size.width) {
            if (region.contains(x, y)) samples.add(Offset(x.toFloat(), y.toFloat()))
            x += step
        }
        y += step
    }
    return Glyph(p, region, samples)
}

/** Returns (coverage %, accuracy %). */
private fun scoreTrace(glyph: Glyph, points: List<InkPoint>, tolerance: Float): Pair<Int, Int> {
    if (points.isEmpty() || glyph.samples.isEmpty()) return 0 to 0
    val t = tolerance.toInt()
    val onLine = points.count { p ->
        val x = p.x.toInt(); val y = p.y.toInt()
        glyph.region.contains(x, y) || glyph.region.contains(x + t, y) || glyph.region.contains(x - t, y) ||
            glyph.region.contains(x, y + t) || glyph.region.contains(x, y - t)
    }
    val reach = tolerance * 2.2f
    val covered = glyph.samples.count { s -> points.any { hypot(it.x - s.x, it.y - s.y) <= reach } }
    return (covered * 100 / glyph.samples.size) to (onLine * 100 / points.size)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceScreen(speaker: Speaker, onBack: () -> Unit) {
    val lang = appLang()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recognizer = remember { InkRecognizer() }
    DisposableEffect(Unit) { onDispose { recognizer.close() } }

    val targets = remember { Qaida.letters.filter { it.id != "hamza" } }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val target = targets[index]
    var freeMode by rememberSaveable { mutableStateOf(false) }

    var modelState by remember { mutableStateOf(ModelState.CHECKING) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt, freeMode) {
        if (!freeMode || modelState == ModelState.READY) return@LaunchedEffect
        modelState = ModelState.CHECKING
        val ok = recognizer.ensureModel { modelState = ModelState.DOWNLOADING }
        modelState = if (ok) ModelState.READY else ModelState.FAILED
    }

    val strokes = remember { mutableStateListOf<SnapshotStateList<InkPoint>>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var result by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    val glyph = remember(target.char, canvasSize) { buildGlyph(target.char, canvasSize) }

    fun reset() {
        strokes.clear()
        result = null
    }

    val ink = Garden.Green
    val dotPaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            color = Garden.Orange.toArgb()
        }
    }
    val fillPaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = Garden.Yellow.copy(alpha = 0.28f).toArgb()
        }
    }

    GardenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text("✍️ " + stringResource(R.string.mode_trace_title), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, color = Garden.GreenDark, maxLines = 1)
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
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Letter picker, right to left like the alphabet.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        itemsIndexed(targets) { i, h ->
                            val sel = i == index
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(shape)
                                    .background(if (sel) Garden.Green else Color.White)
                                    .border(2.dp, if (sel) Garden.Green else Garden.Mint, shape)
                                    .clickable { index = i; reset() },
                                contentAlignment = Alignment.Center,
                            ) { Text(h.char, style = quranStyle(24.sp), color = if (sel) Color.White else Garden.Ink) }
                        }
                    }
                }

                // Mode switch.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(false to stringResource(R.string.trace_mode), true to stringResource(R.string.free_mode)).forEach { (m, label) ->
                        val sel = m == freeMode
                        Text(
                            label,
                            color = if (sel) Color.White else Garden.GreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (sel) Garden.Green else Garden.MintSoft)
                                .clickable { freeMode = m; reset() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(
                            R.string.write_x,
                            when (lang) { "bn" -> target.nameBn; "ar" -> target.nameAr; else -> target.nameEn },
                        ) + "  ·  " + stringResource(if (freeMode) R.string.free_hint else R.string.trace_hint),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Garden.GreenDark,
                        modifier = Modifier.weight(1f),
                    )
                    EmojiButton("🔊") { speaker.speak(Qaida.letterItem(target)) }
                }

                val boardShape = RoundedCornerShape(26.dp)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .aspectRatio(1f)
                        .clip(boardShape)
                        .background(Color.White)
                        .border(2.dp, Garden.Mint, boardShape)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            // Taps count as one-point strokes: the dots are what tell ب ت ث apart.
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                down.consume()
                                val stroke = mutableStateListOf(InkPoint(down.position.x, down.position.y, System.currentTimeMillis()))
                                strokes.add(stroke)
                                result = null
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val last = stroke.last()
                                    val nx = change.position.x
                                    val ny = change.position.y
                                    // Fill in gaps from fast strokes so scoring sees a continuous line.
                                    val gap = hypot(nx - last.x, ny - last.y)
                                    val steps = (gap / 6f).toInt()
                                    for (k in 1..steps) {
                                        val f = k / (steps + 1f)
                                        stroke.add(InkPoint(last.x + (nx - last.x) * f, last.y + (ny - last.y) * f, System.currentTimeMillis()))
                                    }
                                    stroke.add(InkPoint(nx, ny, System.currentTimeMillis()))
                                    change.consume()
                                }
                            }
                        },
                ) {
                    if (!freeMode) {
                        glyph?.let { g ->
                            dotPaint.strokeWidth = size.minDimension * 0.014f
                            dotPaint.pathEffect = DashPathEffect(floatArrayOf(0.1f, size.minDimension * 0.03f), 0f)
                            drawIntoCanvas { c ->
                                c.nativeCanvas.drawPath(g.path, fillPaint)
                                c.nativeCanvas.drawPath(g.path, dotPaint)
                            }
                        }
                    }
                    strokes.forEach { s ->
                        if (s.size == 1) {
                            drawCircle(ink, radius = size.minDimension * 0.016f, center = Offset(s[0].x, s[0].y))
                        } else {
                            val path = Path().apply {
                                moveTo(s[0].x, s[0].y)
                                for (i in 1 until s.size) lineTo(s[i].x, s[i].y)
                            }
                            drawPath(path, ink, style = Stroke(width = size.minDimension * 0.032f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }

                if (freeMode) {
                    when (modelState) {
                        ModelState.CHECKING -> Text(stringResource(R.string.model_checking), color = Garden.Muted)
                        ModelState.DOWNLOADING -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Garden.Green)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.model_downloading), color = Garden.Muted)
                        }
                        ModelState.FAILED -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.model_failed), textAlign = TextAlign.Center, color = Garden.Red)
                            TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.retry)) }
                        }
                        ModelState.READY -> {}
                    }
                }

                result?.let {
                    Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
                        color = Garden.GreenDark, textAlign = TextAlign.Center)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.widthIn(max = 520.dp)) {
                    GradientButton("🧽 " + stringResource(R.string.clear), Garden.traceBrush, Modifier.weight(1f)) { reset() }
                    GradientButton("✅ " + stringResource(R.string.check), Garden.quizBrush, Modifier.weight(1f)) {
                        if (checking || strokes.isEmpty()) return@GradientButton
                        val points = strokes.flatMap { it.toList() }
                        if (!freeMode) {
                            val g = glyph ?: return@GradientButton
                            scope.launch {
                                checking = true
                                val (cov, acc) = withContext(Dispatchers.Default) {
                                    scoreTrace(g, points, canvasSize.width * 0.022f)
                                }
                                val stars = when {
                                    cov >= 70 && acc >= 70 -> 3
                                    cov >= 50 && acc >= 55 -> 2
                                    cov >= 30 -> 1
                                    else -> 0
                                }
                                result = "⭐".repeat(stars) + "☆".repeat(3 - stars) + "\n" +
                                    context.getString(R.string.trace_result, cov, acc)
                                checking = false
                            }
                        } else {
                            if (modelState != ModelState.READY) return@GradientButton
                            scope.launch {
                                checking = true
                                val candidates = recognizer.recognize(
                                    strokes.map { it.toList() }, canvasSize.width.toFloat(), canvasSize.height.toFloat(),
                                )
                                val expected = ArabicText.normalize(target.char)
                                result = when {
                                    candidates.take(2).any { ArabicText.normalize(it).contains(expected) } ->
                                        "⭐⭐⭐\n" + context.getString(R.string.write_correct, target.char)
                                    candidates.isEmpty() -> context.getString(R.string.write_unknown)
                                    else -> "🔁 " + context.getString(R.string.write_wrong, candidates.first())
                                }
                                checking = false
                            }
                        }
                    }
                }

                Row {
                    TextButton(onClick = { index = (index - 1 + targets.size) % targets.size; reset() }) {
                        Text(stringResource(R.string.previous), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { index = (index + 1) % targets.size; reset() }) {
                        Text(stringResource(R.string.next), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
