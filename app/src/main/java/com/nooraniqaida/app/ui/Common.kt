package com.nooraniqaida.app.ui

import android.content.Context
import android.speech.SpeechRecognizer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nooraniqaida.app.R

@Composable
fun appLang(): String = when (LocalConfiguration.current.locales[0].language) {
    "bn" -> "bn"
    "ar" -> "ar"
    else -> "en"
}

fun quranStyle(size: TextUnit, align: TextAlign = TextAlign.Center) = TextStyle(
    fontFamily = QuranFont,
    fontSize = size,
    lineHeight = size * 1.75f,
    textAlign = align,
    textDirection = TextDirection.Rtl,
)

@Composable
fun Pill(text: String, bg: Color, fg: Color, border: Color? = null, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        color = fg,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier
            .clip(shape)
            .background(bg)
            .then(if (border != null) Modifier.border(1.5.dp, border, shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

@Composable
fun GardenCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Garden.Mint,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(5.dp, shape, ambientColor = Garden.Mint, spotColor = Garden.Mint)
            .clip(shape)
            .background(Color.White)
            .border(2.dp, borderColor, shape)
            .padding(18.dp),
        content = content,
    )
}

@Composable
fun GradientButton(text: String, brush: Brush, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun EmojiButton(emoji: String, bg: Color = Garden.YellowSoft, border: Color = Garden.Yellow, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier.size(52.dp).clip(shape).background(bg).border(1.5.dp, border, shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = 22.sp) }
}

/** Cream-to-mint background with vines along the top and bottom, like the design. */
@Composable
fun GardenBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFBEA), Color(0xFFEFFAF0)))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val vine = Color(0xFF63C06F)
            val leaf = Color(0xFF86D892)
            val leafDark = Color(0xFF45A957)
            val berry = Color(0xFFF0506E)
            drawVine(14.dp.toPx(), vine, leaf, berry)
            drawVine(size.height - 14.dp.toPx(), vine, leaf, berry)
            // Leaves along both sides.
            var y = 120.dp.toPx()
            while (y < size.height - 120.dp.toPx()) {
                leafAt(Offset(6.dp.toPx(), y), 0f, leaf.copy(alpha = 0.8f))
                leafAt(Offset(size.width - 6.dp.toPx(), y + 40.dp.toPx()), 0f, leaf.copy(alpha = 0.8f))
                y += 110.dp.toPx()
            }
            // Big corner leaves.
            leafAt(Offset(18.dp.toPx(), 52.dp.toPx()), 55f, leafDark, 64.dp.toPx(), 24.dp.toPx())
            leafAt(Offset(34.dp.toPx(), 40.dp.toPx()), 25f, leaf, 54.dp.toPx(), 20.dp.toPx())
            leafAt(Offset(size.width - 20.dp.toPx(), 48.dp.toPx()), -50f, leaf, 60.dp.toPx(), 22.dp.toPx())
            leafAt(Offset(22.dp.toPx(), size.height - 50.dp.toPx()), -55f, leaf, 66.dp.toPx(), 24.dp.toPx())
            leafAt(Offset(size.width - 26.dp.toPx(), size.height - 46.dp.toPx()), 50f, leafDark, 60.dp.toPx(), 22.dp.toPx())
            // Ladybug.
            val bug = Offset(40.dp.toPx(), 66.dp.toPx())
            drawCircle(Color(0xFFD63031), 7.dp.toPx(), bug)
            drawCircle(Color.Black, 2.dp.toPx(), bug + Offset(-2.5f.dp.toPx(), -1.dp.toPx()))
            drawCircle(Color.Black, 2.dp.toPx(), bug + Offset(2.5f.dp.toPx(), 2.dp.toPx()))
        }
        content()
    }
}

private fun DrawScope.drawVine(y: Float, vine: Color, leaf: Color, berry: Color) {
    val amp = 6.dp.toPx()
    val step = 72.dp.toPx()
    val path = Path().apply {
        moveTo(0f, y)
        var x = 0f
        while (x < size.width) {
            quadraticBezierTo(x + step / 4, y - amp, x + step / 2, y)
            quadraticBezierTo(x + 3 * step / 4, y + amp, x + step, y)
            x += step
        }
    }
    drawPath(path, vine, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    var x = step / 2
    var i = 0
    while (x < size.width) {
        leafAt(Offset(x, y), if (i % 2 == 0) -35f else 35f, leaf)
        if (i % 3 == 0) drawCircle(berry, 3.dp.toPx(), Offset(x + 9.dp.toPx(), y + 6.dp.toPx()))
        x += step
        i++
    }
}

private fun DrawScope.leafAt(
    c: Offset,
    angle: Float,
    color: Color,
    w: Float = 22.dp.toPx(),
    h: Float = 10.dp.toPx(),
) {
    rotate(angle, pivot = c) {
        drawOval(color, topLeft = Offset(c.x - w / 2, c.y - h / 2), size = Size(w, h))
    }
}

fun speechErrorMessage(context: Context, code: Int): String = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
        context.getString(R.string.speech_no_match)
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
        context.getString(R.string.speech_network)
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.mic_permission_needed)
    12, 13 -> context.getString(R.string.speech_lang_missing) // LANGUAGE_NOT_SUPPORTED / LANGUAGE_UNAVAILABLE
    else -> context.getString(R.string.speech_error, code)
}
