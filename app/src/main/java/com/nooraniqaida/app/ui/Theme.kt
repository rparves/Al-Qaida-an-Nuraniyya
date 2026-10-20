package com.nooraniqaida.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.nooraniqaida.app.R

/** Garden palette, taken from the kids' design. */
object Garden {
    val Mint = Color(0xFF9BE7C0)
    val MintSoft = Color(0xFFDDF7E8)
    val Green = Color(0xFF2E8B57)
    val GreenDark = Color(0xFF1F6B45)
    val Ink = Color(0xFF24433A)
    val Muted = Color(0xFF5D7A70)
    val Yellow = Color(0xFFFCD34D)
    val YellowSoft = Color(0xFFFFF4C7)
    val Brown = Color(0xFF7A4B00)
    val Purple = Color(0xFF6D28D9)
    val PurpleSoft = Color(0xFFF1EBFF)
    val PurpleLine = Color(0xFFC4B5FD)
    val Orange = Color(0xFFEA8A1A)
    val Red = Color(0xFFE5484D)
    val RedSoft = Color(0xFFFFE4E1)
    val Cream = Color(0xFFFFFBEA)

    val listenBrush = Brush.linearGradient(listOf(Color(0xFF38A872), Color(0xFF2E7D4F)))
    val quizBrush = Brush.linearGradient(listOf(Color(0xFFF7A21B), Color(0xFFEA6A1A)))
    val traceBrush = Brush.linearGradient(listOf(Color(0xFF1E9C7E), Color(0xFF2F7FB8)))
    val buttonBrush = Brush.horizontalGradient(listOf(Color(0xFF2E8B57), Color(0xFF2E9A8E)))
}

// Google Fonts are downloaded on first launch by Play services and cached: 0 bytes in the APK.
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun family(name: String) = FontFamily(
    Font(GoogleFont(name), provider, FontWeight.Normal),
    Font(GoogleFont(name), provider, FontWeight.Medium),
    Font(GoogleFont(name), provider, FontWeight.Bold),
    Font(GoogleFont(name), provider, FontWeight.ExtraBold),
)

private val UiArabicLatin = family("Baloo Bhaijaan 2")
private val UiBangla = family("Baloo Da 2")

val QuranFont = FontFamily(
    Font(GoogleFont("Amiri"), provider, FontWeight.Normal),
    Font(GoogleFont("Amiri"), provider, FontWeight.Bold),
)

private fun TextStyle.f(font: FontFamily) = copy(fontFamily = font)

@Composable
fun QaidaTheme(content: @Composable () -> Unit) {
    val font = if (appLang() == "bn") UiBangla else UiArabicLatin
    val b = Typography()
    val typography = Typography(
        displayLarge = b.displayLarge.f(font), displayMedium = b.displayMedium.f(font), displaySmall = b.displaySmall.f(font),
        headlineLarge = b.headlineLarge.f(font), headlineMedium = b.headlineMedium.f(font), headlineSmall = b.headlineSmall.f(font),
        titleLarge = b.titleLarge.f(font), titleMedium = b.titleMedium.f(font), titleSmall = b.titleSmall.f(font),
        bodyLarge = b.bodyLarge.f(font), bodyMedium = b.bodyMedium.f(font), bodySmall = b.bodySmall.f(font),
        labelLarge = b.labelLarge.f(font), labelMedium = b.labelMedium.f(font), labelSmall = b.labelSmall.f(font),
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Garden.Green,
            onPrimary = Color.White,
            primaryContainer = Garden.MintSoft,
            onPrimaryContainer = Garden.GreenDark,
            secondary = Garden.Orange,
            background = Garden.Cream,
            surface = Color.White,
            onSurface = Garden.Ink,
            surfaceVariant = Color(0xFFF2FBF5),
            onSurfaceVariant = Garden.Muted,
            errorContainer = Garden.RedSoft,
        ),
        typography = typography,
        content = content,
    )
}
