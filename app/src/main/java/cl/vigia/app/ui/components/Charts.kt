package cl.vigia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.vigia.app.data.Point
import cl.vigia.app.data.Status
import cl.vigia.app.data.fmt
import cl.vigia.app.data.fmtFecha
import cl.vigia.app.data.fmtHora
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.Mono
import cl.vigia.app.ui.theme.MonoBig
import cl.vigia.app.ui.theme.Paper2
import cl.vigia.app.ui.theme.SurfaceCard
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/* ----------------------- Gráfico de área ----------------------- */
@Composable
fun AreaChart(
    data: List<Point>,
    color: Color,
    unit: String,
    limite: Double?,
    rango: String,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
) {
    if (data.size < 2) return
    val measurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontFamily = Mono, fontSize = 9.5.sp, color = InkFaint)
    val limitStyle = TextStyle(fontFamily = Mono, fontSize = 9.5.sp, color = Crit)

    val values = data.map { it.v }
    var minV = values.min()
    var maxV = values.max()
    if (limite != null) {
        minV = min(minV, limite)
        maxV = max(maxV, limite)
    }
    val span0 = maxV - minV
    val pad = if (span0 == 0.0) 1.0 else span0 * 0.14
    minV -= pad
    maxV += pad
    val span = (maxV - minV).coerceAtLeast(1e-6)

    Canvas(modifier.fillMaxWidth().height(height)) {
        val leftPad = 42.dp.toPx()
        val rightPad = 10.dp.toPx()
        val topPad = 10.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val w = size.width - leftPad - rightPad
        val h = size.height - topPad - bottomPad
        val n = data.size

        fun px(i: Int) = leftPad + if (n <= 1) 0f else w * i / (n - 1)
        fun py(v: Double) = topPad + (h * (1.0 - (v - minV) / span)).toFloat()

        val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 6.dp.toPx()))

        // Gridlines + etiquetas Y
        for (g in 0..3) {
            val gv = maxV - span * g / 3
            val gy = py(gv)
            drawLine(LineCol, Offset(leftPad, gy), Offset(size.width - rightPad, gy), 1.dp.toPx(), pathEffect = dash)
            val label = fmt(gv, if (gv < 1) 2 else if (gv < 10) 1 else 0)
            val measured = measurer.measure(label, axisStyle)
            drawText(measured, topLeft = Offset(leftPad - measured.size.width - 6.dp.toPx(), gy - measured.size.height / 2f))
        }

        // Línea + área
        val line = Path()
        data.forEachIndexed { i, p ->
            if (i == 0) line.moveTo(px(i), py(p.v)) else line.lineTo(px(i), py(p.v))
        }
        val area = Path()
        area.addPath(line)
        area.lineTo(px(n - 1), topPad + h)
        area.lineTo(px(0), topPad + h)
        area.close()
        drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.02f)), startY = topPad, endY = topPad + h))
        drawPath(line, color, style = Stroke(width = 2.5.dp.toPx()))

        // Línea de límite normativo
        if (limite != null) {
            val ly = py(limite)
            drawLine(Crit, Offset(leftPad, ly), Offset(size.width - rightPad, ly), 1.4.dp.toPx(), pathEffect = dash)
            val lbl = measurer.measure("límite ${fmt(limite, if (limite < 1) 2 else if (limite < 10) 1 else 0)}", limitStyle)
            drawText(lbl, topLeft = Offset(size.width - rightPad - lbl.size.width, ly - lbl.size.height - 2.dp.toPx()))
        }

        // Punto final
        val lastX = px(n - 1)
        val lastY = py(data.last().v)
        drawCircle(SurfaceCard, 5.dp.toPx(), Offset(lastX, lastY))
        drawCircle(color, 3.5.dp.toPx(), Offset(lastX, lastY))

        // Etiquetas X (inicio, medio, fin)
        val idxs = listOf(0, n / 2, n - 1)
        idxs.forEach { i ->
            val t = data[i].t
            val txt = if (rango == "24h") fmtHora(t) else fmtFecha(t)
            val m = measurer.measure(txt, axisStyle)
            var tx = px(i) - m.size.width / 2f
            tx = tx.coerceIn(leftPad, size.width - rightPad - m.size.width)
            drawText(m, topLeft = Offset(tx, size.height - bottomPad + 6.dp.toPx()))
        }
    }
}

/* ----------------------- Medidor radial ----------------------- */
@Composable
fun Gauge(
    ratio: Double,
    status: Status,
    valueText: String,
    unit: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 184.dp,
) {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        val color = statusColor(status)
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 13.dp.toPx()
            val inset = stroke / 2f + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val startAngle = 135f
            val sweepFull = 270f

            // Pista
            drawArc(Paper2, startAngle, sweepFull, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            // Valor
            val frac = (ratio / 1.25).coerceIn(0.0, 1.0).toFloat()
            drawArc(color, startAngle, sweepFull * frac, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))

            // Marca del límite (ratio = 1.0 -> 0.8 del arco)
            val limitDeg = startAngle + sweepFull * (1.0 / 1.25)
            val rad = Math.toRadians(limitDeg)
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rOuter = arcSize.width / 2f + stroke / 2f
            val rInner = arcSize.width / 2f - stroke / 2f
            drawLine(
                Color(0xFF2A2620).copy(alpha = 0.7f),
                Offset(cx + (rInner * cos(rad)).toFloat(), cy + (rInner * sin(rad)).toFloat()),
                Offset(cx + (rOuter * cos(rad)).toFloat(), cy + (rOuter * sin(rad)).toFloat()),
                2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Text(valueText, style = MonoBig)
            androidx.compose.material3.Text(unit.ifEmpty { "índice" }, style = TextStyle(fontFamily = Mono, fontSize = 12.sp, color = InkFaint))
        }
    }
}

/* ----------------------- Sparkline ----------------------- */
@Composable
fun Sparkline(
    data: List<Point>,
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = 104.dp,
    height: Dp = 40.dp,
) {
    Canvas(modifier.size(width, height)) {
        if (data.isEmpty()) return@Canvas
        val vals = data.map { it.v }
        val minV = vals.min()
        val maxV = vals.max()
        val span = (maxV - minV).coerceAtLeast(1e-6)
        val n = data.size
        val pad = 3.dp.toPx()
        fun px(i: Int) = if (n <= 1) 0f else size.width * i / (n - 1)
        fun py(v: Double) = (size.height - pad - ((v - minV) / span) * (size.height - 2 * pad)).toFloat()

        val line = Path()
        data.forEachIndexed { i, p -> if (i == 0) line.moveTo(px(i), py(p.v)) else line.lineTo(px(i), py(p.v)) }
        val area = Path()
        area.addPath(line)
        area.lineTo(px(n - 1), size.height)
        area.lineTo(px(0), size.height)
        area.close()
        drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0f))))
        drawPath(line, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}
