@file:OptIn(ExperimentalTextApi::class)

package cl.vigia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cl.vigia.app.R

/* Tipografías variables empaquetadas en res/font.
   Cada peso se declara con su FontVariation para que el motor escoja bien. */

private fun bricolage(weight: Int) = Font(
    R.font.bricolage_grotesque,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun hanken(weight: Int) = Font(
    R.font.hanken_grotesk,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun spline(weight: Int) = Font(
    R.font.spline_sans_mono,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Display = FontFamily(bricolage(500), bricolage(600), bricolage(700), bricolage(800))
val Body = FontFamily(hanken(400), hanken(500), hanken(600), hanken(700))
val Mono = FontFamily(spline(400), spline(500), spline(600))

val VigiaTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight(800), fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight(700), fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight(700), fontSize = 23.sp, lineHeight = 27.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight(700), fontSize = 19.sp, lineHeight = 23.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight(600), fontSize = 17.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight(600), fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight(400), fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight(400), fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = Body, fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight(600), fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight(600), fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight(500), fontSize = 11.sp, lineHeight = 14.sp),
)

// Estilos mono reutilizables (lecturas / unidades / etiquetas técnicas)
val MonoBig = TextStyle(fontFamily = Mono, fontWeight = FontWeight(600), fontSize = 30.sp, letterSpacing = (-0.5).sp)
val MonoValue = TextStyle(fontFamily = Mono, fontWeight = FontWeight(600), fontSize = 20.sp, letterSpacing = (-0.3).sp)
val MonoLabel = TextStyle(fontFamily = Mono, fontWeight = FontWeight(500), fontSize = 11.sp, letterSpacing = 1.6.sp)
val MonoSmall = TextStyle(fontFamily = Mono, fontWeight = FontWeight(400), fontSize = 12.sp)
