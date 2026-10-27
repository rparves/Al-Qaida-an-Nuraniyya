package com.nooraniqaida.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nooraniqaida.app.data.Makhraj

/**
 * Side view of the head (facing left): lips, teeth, tongue, palate, nasal cavity and
 * throat, with a pulsing marker on the letter's makhraj. Drawn in code, so it adds
 * no image files to the app.
 */
@Composable
fun MakhrajDiagram(makhraj: Makhraj, modifier: Modifier = Modifier, secondary: Makhraj? = null) {
    val pulse by rememberInfiniteTransition(label = "makhraj").animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse",
    )
    Canvas(modifier) {
        val sx = size.width / 100f
        val sy = size.height / 90f
        fun o(x: Float, y: Float) = Offset(x * sx, y * sy)
        fun Path.m(x: Float, y: Float) = moveTo(x * sx, y * sy)
        fun Path.l(x: Float, y: Float) = lineTo(x * sx, y * sy)
        fun Path.c(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
            cubicTo(x1 * sx, y1 * sy, x2 * sx, y2 * sy, x3 * sx, y3 * sy)

        val skin = Color(0xFFFFDCC2)
        val skinLine = Color(0xFFE0A980)
        val cavity = Color(0xFFB0485C)
        val tongue = Color(0xFFE57373)

        // Head and neck.
        val head = Path().apply {
            m(62f, 2f); c(34f, 2f, 16f, 12f, 12f, 24f); l(4f, 34f); l(11f, 38f)
            c(9f, 40f, 9f, 43f, 10f, 44f); l(13f, 48f); l(10f, 52f)
            c(10f, 56f, 12f, 60f, 16f, 64f); c(26f, 68f, 36f, 70f, 44f, 70f)
            l(48f, 90f); l(92f, 90f); c(98f, 60f, 96f, 20f, 62f, 2f); close()
        }
        drawPath(head, skin)
        drawPath(head, skinLine, style = Stroke(1.2f * sx))

        // Nasal cavity (khayshum).
        drawOval(Color(0xFFF4B9B9), topLeft = o(16f, 28f), size = Size(34f * sx, 7f * sy))

        // Mouth cavity and throat airway.
        val mouth = Path().apply {
            m(14f, 46.5f); c(22f, 37f, 42f, 33f, 54f, 39f); l(58f, 44f)
            l(58f, 90f); l(65f, 90f); l(65f, 44f); c(64f, 36f, 56f, 33f, 50f, 33f)
            c(40f, 33f, 22f, 36f, 14f, 46.5f); close()
        }
        drawPath(mouth, cavity.copy(alpha = 0.45f))
        drawRoundRect(cavity.copy(alpha = 0.55f), topLeft = o(57f, 44f), size = Size(8f * sx, 46f * sy),
            cornerRadius = CornerRadius(3f * sx))

        // Hard and soft palate with the uvula.
        val palate = Path().apply { m(15f, 45f); c(22f, 37f, 42f, 33f, 54f, 40f) }
        drawPath(palate, Color(0xFFD9777F), style = Stroke(1.6f * sx))
        drawCircle(Color(0xFFD9777F), 1.6f * sx, o(56.5f, 46f))

        // Tongue.
        val t = Path().apply {
            m(15f, 51f); c(16f, 48f, 22f, 46f, 30f, 45f); c(42f, 43f, 52f, 46f, 56f, 56f)
            c(57f, 62f, 56f, 66f, 54f, 70f); c(46f, 66f, 36f, 60f, 30f, 58f)
            c(24f, 57f, 18f, 56f, 15f, 54f); close()
        }
        drawPath(t, tongue)
        drawPath(Path().apply { m(22f, 49f); c(32f, 47f, 44f, 48f, 52f, 56f) }, Color(0xFFC85A5A), style = Stroke(0.8f * sx))

        // Teeth and lips.
        drawRoundRect(Color.White, topLeft = o(13f, 43.2f), size = Size(3.2f * sx, 3.6f * sy), cornerRadius = CornerRadius(0.8f * sx))
        drawRoundRect(Color.White, topLeft = o(13f, 50f), size = Size(3.2f * sx, 3.6f * sy), cornerRadius = CornerRadius(0.8f * sx))
        drawOval(Color(0xFFE08A8A), topLeft = o(8f, 43.5f), size = Size(5f * sx, 3.5f * sy))
        drawOval(Color(0xFFE08A8A), topLeft = o(8f, 50f), size = Size(5f * sx, 3.5f * sy))

        // Voice box.
        drawRoundRect(Color(0xFFC97B7B), topLeft = o(56.5f, 77f), size = Size(9f * sx, 4f * sy), cornerRadius = CornerRadius(1f * sx))

        // Highlight.
        if (makhraj == Makhraj.JAWF) {
            drawPath(mouth, Garden.Orange.copy(alpha = 0.25f * pulse))
        }
        // Ghunnah letters also sound from the khayshum (nose).
        secondary?.let { marker(o(it.x, it.y), sx, 1.65f - pulse) }
        marker(o(makhraj.x, makhraj.y), sx, pulse)
    }
}

private fun DrawScope.marker(c: Offset, sx: Float, pulse: Float) {
    drawCircle(Garden.Orange.copy(alpha = 0.35f), 5.5f * sx * pulse, c)
    drawCircle(Color.White, 2.4f * sx, c)
    drawCircle(Garden.Orange, 1.7f * sx, c)
}
