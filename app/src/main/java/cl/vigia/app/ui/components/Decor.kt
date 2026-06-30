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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.vigia.app.data.LiveSim
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.data.Zone
import cl.vigia.app.ui.theme.Agua
import cl.vigia.app.ui.theme.Aire
import cl.vigia.app.ui.theme.Body
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.Mono
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

/* ----------------------- Mapa topográfico de la zona -----------------------
   Mapa simulado con aspecto realista: relieve sombreado, curvas de nivel,
   vegetación, cauce y embalse, caminos, el rajo de la faena, grilla,
   barra de escala, norte y pines de estación con sombra. Todo varía por zona.  */
@Composable
fun StationMap(
    zone: Zone,
    modifier: Modifier = Modifier,
    selectedStationId: String? = null,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Body, fontWeight = FontWeight(700), fontSize = 9.5.sp, color = Color(0xFF40382B))
    val microStyle = TextStyle(fontFamily = Mono, fontWeight = FontWeight(500), fontSize = 8.sp, color = Color(0xFF6F6857))
    // Semilla normalizada de la zona, para variar el terreno.
    val s = (zone.seed % 7) / 7f
    val s2 = ((zone.seed * 3) % 11) / 11f

    val transition = rememberInfiniteTransition(label = "map")
    val pulse by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "mapPulse",
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun px(p: Float) = w * p / 100f
        fun py(p: Float) = h * p / 100f

        // --- Base de terreno (verde tierras bajas -> tostado tierras altas) ---
        drawRect(
            Brush.linearGradient(
                listOf(Color(0xFFDBE0C4), Color(0xFFE4DCC2), Color(0xFFE9DDC1)),
                start = Offset(0f, 0f), end = Offset(w, h),
            ),
        )

        // --- Relieve sombreado: cerros suaves (claro arriba-izq, sombra abajo-der) ---
        val hills = listOf(
            Triple(0.28f + s * 0.1f, 0.30f, 0.46f),
            Triple(0.74f - s2 * 0.12f, 0.22f, 0.40f),
            Triple(0.58f, 0.74f, 0.50f),
        )
        hills.forEach { (cx, cy, rad) ->
            val center = Offset(px(cx * 100), py(cy * 100))
            val r = rad * minOf(w, h)
            drawCircle(Brush.radialGradient(listOf(Color(0x33FFFFFF), Color(0x00FFFFFF)), Offset(center.x - r * 0.3f, center.y - r * 0.3f), r), r, center)
            drawCircle(Brush.radialGradient(listOf(Color(0x00000000), Color(0x1A6B5A3A)), center, r), r, center)
        }

        // --- Vegetación (manchas verdes translúcidas en tierras bajas) ---
        val veg = listOf(0.16f to 0.62f, 0.30f to 0.78f, 0.46f to 0.66f, 0.12f to 0.40f)
        veg.forEachIndexed { i, (vx, vy) ->
            val c = Offset(px(vx * 100 + s * 6), py(vy * 100))
            drawCircle(Color(0xFF8FA86A).copy(alpha = 0.22f), (0.10f + (i % 2) * 0.03f) * minOf(w, h), c)
        }

        // --- Curvas de nivel topográficas ---
        val contour = Color(0xFFB7A57E)
        for (k in 0..5) {
            val off = h * (0.10f + s * 0.05f + k * 0.15f)
            val index = k % 2 == 0
            val path = Path().apply {
                moveTo(-12f, off)
                cubicTo(w * 0.22f, off - h * (0.07f + s2 * 0.05f), w * 0.5f, off + h * 0.06f, w * 0.74f, off - h * 0.05f)
                cubicTo(w * 0.88f, off - h * 0.09f, w * 0.96f, off + h * 0.02f, w + 12f, off - h * 0.04f)
            }
            drawPath(path, contour.copy(alpha = if (index) 0.55f else 0.30f), style = Stroke((if (index) 1.3f else 0.9f).dp.toPx()))
        }

        // --- Cauce + embalse / tranque ---
        val river = Path().apply {
            moveTo(px(4f + s * 6f), py(2f))
            cubicTo(px(16f), py(24f), px(8f + s * 8f), py(44f), px(24f), py(56f))
            cubicTo(px(34f), py(64f), px(48f + s * 6f), py(70f), px(66f), py(76f))
            cubicTo(px(78f), py(80f), px(86f), py(86f), px(96f), py(92f))
        }
        drawPath(river, Color(0xFF5C9FB4).copy(alpha = 0.85f), style = Stroke(3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(river, Color(0xFFAFD6E0).copy(alpha = 0.6f), style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
        // tranque de relaves cerca de la faena
        val pondC = Offset(px(40f + s * 8f), py(48f))
        drawOval(Color(0xFF5C9FB4).copy(alpha = 0.55f), topLeft = Offset(pondC.x - px(11f), pondC.y - py(7f)), size = Size(px(22f), py(14f)))
        drawOval(Color(0xFF3E7E92), topLeft = Offset(pondC.x - px(11f), pondC.y - py(7f)), size = Size(px(22f), py(14f)), style = Stroke(1.2.dp.toPx()))

        // --- Caminos (acceso a la faena) ---
        val faenaX = 64f + s * 12f
        val faenaC = Offset(px(faenaX + 7f), py(19f))
        val road = Path().apply {
            moveTo(px(-2f), py(86f))
            cubicTo(px(28f), py(80f), px(40f), py(54f), px(faenaX), py(40f))
            cubicTo(px(faenaX + 4f), py(32f), faenaC.x, py(24f), faenaC.x, faenaC.y)
        }
        drawPath(road, Color(0xFFB59E78), style = Stroke(5.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(road, Color(0xFFF1E7D2), style = Stroke(3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(road, Color(0xFFB59E78).copy(alpha = 0.7f), style = Stroke(0.9.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))))
        // camino secundario
        val road2 = Path().apply {
            moveTo(px(102f), py(58f))
            cubicTo(px(74f), py(60f), px(54f), py(64f), px(30f), py(70f))
        }
        drawPath(road2, Color(0xFFC9B591), style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))))

        // --- Rajo de la faena (terrazas concéntricas) ---
        val pitR = px(13f)
        listOf(1f to Color(0xFFC2AE8C), 0.74f to Color(0xFFA8916C), 0.5f to Color(0xFF8C7551), 0.28f to Color(0xFF6E5A3C)).forEach { (f, col) ->
            drawOval(
                col.copy(alpha = 0.9f),
                topLeft = Offset(faenaC.x - pitR * f, faenaC.y - pitR * f * 0.66f),
                size = Size(pitR * 2 * f, pitR * 2 * f * 0.66f),
            )
            drawOval(
                Color(0xFF5A4A30).copy(alpha = 0.5f),
                topLeft = Offset(faenaC.x - pitR * f, faenaC.y - pitR * f * 0.66f),
                size = Size(pitR * 2 * f, pitR * 2 * f * 0.66f),
                style = Stroke(0.8.dp.toPx()),
            )
        }
        val faenaLbl = measurer.measure("FAENA", microStyle.copy(color = Color(0xFF5A4A30)))
        drawText(faenaLbl, topLeft = Offset(faenaC.x - faenaLbl.size.width / 2f, faenaC.y - faenaLbl.size.height / 2f))

        // --- Grilla de coordenadas (sutil) ---
        val grid = Color(0xFF6F6857).copy(alpha = 0.10f)
        for (gx in 1..3) drawLine(grid, Offset(px(gx * 25f), 0f), Offset(px(gx * 25f), h), 1f)
        for (gy in 1..3) drawLine(grid, Offset(0f, py(gy * 25f)), Offset(w, py(gy * 25f)), 1f)

        // --- Estaciones (pines realistas) ---
        zone.stations.forEach { st ->
            val status = LiveSim.stationStatus(zone.id, st.id)
            val color = statusColor(status)
            val c = Offset(px(st.x), py(st.y))
            val sel = st.id == selectedStationId
            // halo pulsante en críticas o seleccionada
            if (status == Status.CRIT || sel) {
                drawCircle(color.copy(alpha = 0.22f * (1f - pulse)), (10f + pulse * 12f).dp.toPx(), c)
            }
            drawStationPin(c, color, if (sel) 1.18f else 1f)
            // etiqueta con fondo legible
            val name = st.nombre.removePrefix("Estación ")
            val m = measurer.measure(name, labelStyle)
            val pad = 3.dp.toPx()
            val boxW = m.size.width + pad * 2
            val boxX = (c.x - boxW / 2f).coerceIn(2f, w - boxW - 2f)
            val boxY = c.y - 30.dp.toPx() - m.size.height
            drawRoundRect(
                Color(0xFFFBF8F1).copy(alpha = 0.85f),
                topLeft = Offset(boxX, boxY - pad),
                size = Size(boxW, m.size.height + pad * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
            drawText(m, topLeft = Offset(boxX + pad, boxY))
        }

        // --- Norte (brújula) ---
        val nx = w - 16.dp.toPx()
        val ny = 18.dp.toPx()
        val arrow = Path().apply {
            moveTo(nx, ny - 9.dp.toPx()); lineTo(nx - 5.dp.toPx(), ny + 5.dp.toPx())
            lineTo(nx, ny + 1.dp.toPx()); lineTo(nx + 5.dp.toPx(), ny + 5.dp.toPx()); close()
        }
        drawPath(arrow, Color(0xFF40382B), style = Fill)
        val nLbl = measurer.measure("N", microStyle.copy(color = Color(0xFF40382B), fontWeight = FontWeight(700)))
        drawText(nLbl, topLeft = Offset(nx - nLbl.size.width / 2f, ny + 6.dp.toPx()))

        // --- Barra de escala ---
        val sbX = 14.dp.toPx()
        val sbY = h - 16.dp.toPx()
        val sbW = px(26f)
        drawLine(Color(0xFF40382B), Offset(sbX, sbY), Offset(sbX + sbW, sbY), 2.dp.toPx())
        listOf(0f, 0.5f, 1f).forEach { f ->
            drawLine(Color(0xFF40382B), Offset(sbX + sbW * f, sbY - 3.dp.toPx()), Offset(sbX + sbW * f, sbY + 3.dp.toPx()), 1.6.dp.toPx())
        }
        val scLbl = measurer.measure("0       1       2 km", microStyle)
        drawText(scLbl, topLeft = Offset(sbX, sbY - scLbl.size.height - 3.dp.toPx()))
    }
}

/** Pin de mapa (lágrima) con sombra, relleno por estado y punto interior. */
private fun DrawScope.drawStationPin(tip: Offset, color: Color, scale: Float) {
    val r = 7.dp.toPx() * scale
    val headC = Offset(tip.x, tip.y - r * 2.0f)
    // sombra en el suelo
    drawOval(
        Color(0x33000000),
        topLeft = Offset(tip.x - r * 0.7f, tip.y - r * 0.25f),
        size = Size(r * 1.4f, r * 0.5f),
    )
    // cuerpo (triángulo hacia la punta)
    val body = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(headC.x - r * 0.74f, headC.y + r * 0.32f)
        lineTo(headC.x + r * 0.74f, headC.y + r * 0.32f)
        close()
    }
    drawPath(body, color)
    // cabeza
    drawCircle(color, r, headC)
    drawCircle(Color(0xFFFBF8F1), r, headC, style = Stroke(1.8.dp.toPx()))
    // punto interior
    drawCircle(Color(0xFFFBF8F1), r * 0.42f, headC)
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
