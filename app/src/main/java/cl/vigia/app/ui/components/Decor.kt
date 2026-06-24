package cl.vigia.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.ui.theme.Agua
import cl.vigia.app.ui.theme.Aire
import cl.vigia.app.ui.theme.Body
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.Paper
import cl.vigia.app.ui.theme.Tierra

/* ----------------------- Corte de terreno (login) ----------------------- */
@Composable
fun TerrainHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hero")
    val drift by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )
    val pulse by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val surfaceY = h * 0.46f

        // Partículas (aire)
        val motas = listOf(
            0.12f to 0.12f, 0.24f to 0.22f, 0.36f to 0.10f, 0.52f to 0.18f,
            0.64f to 0.11f, 0.78f to 0.24f, 0.88f to 0.15f, 0.18f to 0.32f,
            0.46f to 0.30f, 0.72f to 0.34f, 0.9f to 0.32f,
        )
        motas.forEachIndexed { i, (fx, fy) ->
            val dy = (if (i % 2 == 0) -1 else 1) * drift * 10.dp.toPx()
            drawCircle(Aire.copy(alpha = 0.45f), if (i % 3 == 0) 3.dp.toPx() else 2.dp.toPx(), Offset(w * fx, h * fy + dy))
        }

        // Onda de ruido sobre la superficie
        val wave = Path()
        val waveY = surfaceY - 14.dp.toPx()
        val steps = 28
        for (s in 0..steps) {
            val x = w * s / steps
            val y = waveY + kotlin.math.sin(s * 0.9f) * 6.dp.toPx()
            if (s == 0) wave.moveTo(x, y) else wave.lineTo(x, y)
        }
        drawPath(wave, Color(0xFFC49AB4).copy(alpha = 0.7f), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

        // Estrato superior de suelo (tierra)
        val soil1 = Path().apply {
            moveTo(0f, surfaceY)
            cubicTo(w * 0.3f, surfaceY - 14f, w * 0.65f, surfaceY + 12f, w, surfaceY - 6f)
            lineTo(w, h * 0.64f)
            lineTo(0f, h * 0.66f)
            close()
        }
        drawPath(soil1, Brush.verticalGradient(listOf(Tierra.copy(alpha = 0.42f), Tierra.copy(alpha = 0.3f)), startY = surfaceY, endY = h * 0.65f))

        // Napa de agua
        val waterTop = h * 0.62f
        val water = Path().apply {
            moveTo(0f, waterTop)
            cubicTo(w * 0.3f, waterTop - 12f, w * 0.6f, waterTop + 16f, w, waterTop - 6f)
            lineTo(w, h * 0.76f)
            lineTo(0f, h * 0.78f)
            close()
        }
        drawPath(water, Brush.verticalGradient(listOf(Agua.copy(alpha = 0.55f), Agua.copy(alpha = 0.12f)), startY = waterTop, endY = h * 0.77f))
        val waterline = Path().apply {
            moveTo(0f, waterTop)
            cubicTo(w * 0.3f, waterTop - 12f, w * 0.6f, waterTop + 16f, w, waterTop - 6f)
        }
        drawPath(waterline, Color(0xFF5FC7D0).copy(alpha = 0.85f), style = Stroke(2.dp.toPx()))

        // Estrato profundo
        val deep = Path().apply {
            moveTo(0f, h * 0.76f)
            cubicTo(w * 0.3f, h * 0.74f, w * 0.6f, h * 0.80f, w, h * 0.75f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(deep, Color(0xFF5A3C24).copy(alpha = 0.6f))

        // Vetas minerales
        val vein = Path().apply {
            moveTo(w * 0.05f, h * 0.88f)
            cubicTo(w * 0.3f, h * 0.85f, w * 0.6f, h * 0.91f, w * 0.98f, h * 0.87f)
        }
        drawPath(vein, Color(0xFFCAA15F).copy(alpha = 0.3f), style = Stroke(1.5.dp.toPx()))

        // Pins de estaciones
        val pinXs = listOf(0.22f, 0.5f, 0.78f)
        pinXs.forEachIndexed { i, fx ->
            val cx = w * fx
            val top = surfaceY - 18.dp.toPx()
            // tallo punteado
            drawLine(
                Paper.copy(alpha = 0.4f),
                Offset(cx, top), Offset(cx, h * 0.72f),
                1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx())),
            )
            // anillo pulsante
            val ringAlpha = (0.5f - pulse * 0.5f).coerceAtLeast(0f)
            drawCircle(Color(0xFF7BD389).copy(alpha = ringAlpha), 11.dp.toPx() + pulse * 7.dp.toPx(), Offset(cx, top), style = Stroke(1.5.dp.toPx()))
            drawCircle(Color(0xFF2A2B23), 11.dp.toPx(), Offset(cx, top))
            drawCircle(Paper, 11.dp.toPx(), Offset(cx, top), style = Stroke(2.dp.toPx()))
            drawCircle(Color(0xFF7BD389), 4.dp.toPx(), Offset(cx, top))
        }
    }
}

/* ----------------------- Mapa esquemático de estaciones ----------------------- */
@Composable
fun StationMap(modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Body, fontWeight = FontWeight(600), fontSize = 10.sp, color = InkSoft)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun px(p: Float) = w * p / 100f
        fun py(p: Float) = h * p / 100f

        // Fondo
        drawRect(Brush.verticalGradient(listOf(Color(0xFFECE5D6), Color(0xFFE3DCCB))))

        // Curvas de nivel
        val contourColor = Color(0xFFCDBF9F)
        for (k in 0..3) {
            val off = h * (0.18f + k * 0.16f)
            val path = Path().apply {
                moveTo(-10f, off)
                cubicTo(w * 0.25f, off - h * 0.08f, w * 0.55f, off + h * 0.05f, w + 10f, off - h * 0.06f)
            }
            drawPath(path, contourColor.copy(alpha = 0.8f), style = Stroke(1.dp.toPx()))
        }

        // Faena (esquema)
        val faena = Path().apply {
            moveTo(px(70f), py(14f)); lineTo(px(86f), py(14f)); lineTo(px(83f), py(26f)); lineTo(px(73f), py(26f)); close()
        }
        drawPath(faena, Color(0xFFC9B58E).copy(alpha = 0.55f))

        // Cauce (agua)
        val river = Path().apply {
            moveTo(px(8f), py(6f))
            cubicTo(px(18f), py(28f), px(10f), py(46f), px(26f), py(58f))
            cubicTo(px(36f), py(66f), px(46f), py(70f), px(60f), py(72f))
        }
        drawPath(river, Color(0xFF7FB6BE).copy(alpha = 0.7f), style = Stroke(2.6.dp.toPx(), cap = StrokeCap.Round))

        // Estaciones
        Repo.stations.forEach { st ->
            val status = Repo.stationStatus(st.id)
            val color = statusColor(status)
            val c = Offset(px(st.x), py(st.y))
            if (status != Status.OK) {
                drawCircle(color.copy(alpha = 0.4f), 9.dp.toPx(), c, style = Stroke(1.dp.toPx()))
            }
            drawCircle(Paper, 6.dp.toPx(), c)
            drawCircle(color, 6.dp.toPx(), c, style = Stroke(1.6.dp.toPx()))
            drawCircle(color, 2.6.dp.toPx(), c)
            val name = st.nombre.removePrefix("Estación ")
            val m = measurer.measure(name, labelStyle)
            drawText(m, topLeft = Offset(c.x - m.size.width / 2f, c.y - 11.dp.toPx() - m.size.height))
        }
    }
}

/* ----------------------- Curvas de nivel (textura ambiente) ----------------------- */
@Composable
fun Contours(modifier: Modifier = Modifier, color: Color = Color(0xFF2A2620), alpha: Float = 0.05f) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        for (k in 0..4) {
            val off = h * (0.3f + k * 0.16f)
            val path = Path().apply {
                moveTo(-10f, off)
                cubicTo(w * 0.25f, off - h * 0.18f, w * 0.6f, off + h * 0.12f, w + 10f, off - h * 0.14f)
            }
            drawPath(path, color.copy(alpha = alpha), style = Stroke(1.2f))
        }
    }
}
