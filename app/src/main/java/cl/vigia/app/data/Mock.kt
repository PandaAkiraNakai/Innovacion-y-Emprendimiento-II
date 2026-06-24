package cl.vigia.app.data

import androidx.compose.ui.graphics.Color
import cl.vigia.app.ui.theme.Agua
import cl.vigia.app.ui.theme.AguaSoft
import cl.vigia.app.ui.theme.Aire
import cl.vigia.app.ui.theme.AireSoft
import cl.vigia.app.ui.theme.Ruido
import cl.vigia.app.ui.theme.RuidoSoft
import cl.vigia.app.ui.theme.Tierra
import cl.vigia.app.ui.theme.TierraSoft
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

/* ============================================================
   Datos de demostración (todo en el dispositivo).
   Series deterministas (semilla fija) para una demo estable.
   Lugares y personas ficticios.
   ============================================================ */

enum class Status { OK, WARN, CRIT }

data class Point(val t: Long, val v: Double)

data class Metric(
    val key: String,
    val label: String,
    val unit: String,
    val limite: Double,
    val baseline: Double,
    val amp: Double,
    val seed: Int,
    val dec: Int,
    val lowerIsWorse: Boolean = false,
)

data class Domain(
    val tipo: String,
    val nombre: String,
    val titulo: String,
    val color: Color,
    val soft: Color,
    val sub: String,
    val descripcion: String,
    val principal: String,
    val metrics: List<Metric>,
)

data class Station(val id: String, val nombre: String, val sector: String, val x: Float, val y: Float)

data class Alert(
    val id: String,
    val tipo: String,
    val nivel: Status,
    val estado: String, // activa | reconocida | resuelta
    val titulo: String,
    val detalle: String,
    val metrica: String,
    val valor: String,
    val estacion: String,
    val ts: Long,
)

data class Dataset(
    val id: String,
    val tipo: String,
    val titulo: String,
    val estacion: String,
    val periodo: String,
    val formato: String,
    val filas: Int?,
    val peso: String,
    val actualizado: Long,
)

data class Trend(val pct: Int, val dir: String) // up | down | flat

private const val HOUR = 3600_000L
private const val DAY = 24 * HOUR

// Ancla temporal fija (UTC) para etiquetas estables.
val NOW: Long = Instant.parse("2026-06-23T14:00:00Z").toEpochMilli()

object Repo {

    val stations = listOf(
        Station("norte", "Estación Norte", "Quebrada Verde", 30f, 32f),
        Station("sur", "Estación Sur", "Los Almendros", 64f, 70f),
        Station("rio", "Estación Río Bajo", "Vega del Estero", 22f, 66f),
        Station("camino", "Estación Camino Minero", "Portezuelo", 75f, 30f),
    )

    val domains = listOf(
        Domain(
            tipo = "agua", nombre = "Agua", titulo = "Calidad del agua",
            color = Agua, soft = AguaSoft, sub = "Vertientes y canales del sector",
            descripcion = "Seguimiento de la calidad del agua superficial aguas abajo de la faena: metales, acidez y turbidez que pueden afectar el riego y el consumo.",
            principal = "arsenico",
            metrics = listOf(
                Metric("arsenico", "Arsénico", "mg/L", 0.01, 0.006, 0.004, 101, 3),
                Metric("cobre", "Cobre", "mg/L", 1.0, 0.42, 0.22, 102, 2),
                Metric("ph", "pH", "", 6.5, 7.4, 0.5, 103, 1, lowerIsWorse = true),
                Metric("turbidez", "Turbidez", "NTU", 5.0, 2.4, 1.6, 104, 1),
                Metric("sulfatos", "Sulfatos", "mg/L", 250.0, 138.0, 60.0, 105, 0),
            ),
        ),
        Domain(
            tipo = "aire", nombre = "Aire", titulo = "Calidad del aire",
            color = Aire, soft = AireSoft, sub = "Material particulado y gases",
            descripcion = "Monitoreo del polvo en suspensión y gases generados por tronaduras, transporte y acopio de material. Clave para la salud respiratoria del sector.",
            principal = "pm10",
            metrics = listOf(
                Metric("pm25", "PM2.5", "µg/m³", 25.0, 14.0, 9.0, 201, 0),
                Metric("pm10", "PM10", "µg/m³", 50.0, 32.0, 18.0, 202, 0),
                Metric("so2", "SO₂", "ppb", 60.0, 22.0, 14.0, 203, 0),
                Metric("no2", "NO₂", "ppb", 100.0, 31.0, 16.0, 204, 0),
                Metric("sedimentable", "Polvo sedimentable", "mg/m²/d", 150.0, 78.0, 42.0, 205, 0),
            ),
        ),
        Domain(
            tipo = "tierra", nombre = "Tierra", titulo = "Calidad del suelo",
            color = Tierra, soft = TierraSoft, sub = "Metales y salud del suelo",
            descripcion = "Análisis del suelo agrícola y de áreas habitadas: acumulación de metales pesados, acidez y humedad que condicionan los cultivos del sector.",
            principal = "plomo",
            metrics = listOf(
                Metric("plomo", "Plomo", "mg/kg", 300.0, 142.0, 70.0, 301, 0),
                Metric("arsenicoSuelo", "Arsénico", "mg/kg", 20.0, 11.0, 6.0, 302, 1),
                Metric("cobreSuelo", "Cobre", "mg/kg", 100.0, 58.0, 26.0, 303, 0),
                Metric("phSuelo", "pH del suelo", "", 5.5, 6.6, 0.6, 304, 1, lowerIsWorse = true),
                Metric("humedad", "Humedad", "%", 12.0, 19.0, 5.0, 305, 0, lowerIsWorse = true),
            ),
        ),
        Domain(
            tipo = "ruido", nombre = "Ruido", titulo = "Niveles de ruido",
            color = Ruido, soft = RuidoSoft, sub = "Faena, transporte y tronaduras",
            descripcion = "Nivel de presión sonora en zonas habitadas. Las tronaduras y el tránsito de camiones marcan los picos que afectan el descanso de la comunidad.",
            principal = "diurno",
            metrics = listOf(
                Metric("diurno", "Ruido diurno", "dB(A)", 55.0, 47.0, 9.0, 401, 0),
                Metric("nocturno", "Ruido nocturno", "dB(A)", 45.0, 41.0, 7.0, 402, 0),
                Metric("pico", "Pico máximo", "dB(A)", 80.0, 63.0, 18.0, 403, 0),
                Metric("tronaduras", "Tronaduras / día", "", 4.0, 2.0, 2.0, 404, 0),
            ),
        ),
    )

    val periodos = listOf("Últimas 24 horas", "Últimos 7 días", "Últimos 30 días", "Trimestre actual")
    val formatos = listOf("CSV", "JSON")

    val datasets = listOf(
        Dataset("d1", "agua", "Calidad del agua — registro horario", "rio", "Últimos 7 días", "CSV", 168, "24 KB", NOW - 2 * HOUR),
        Dataset("d2", "aire", "Material particulado PM2.5 y PM10", "norte", "Últimos 30 días", "CSV", 720, "96 KB", NOW - 3 * HOUR),
        Dataset("d3", "ruido", "Niveles de ruido y tronaduras", "camino", "Últimos 7 días", "CSV", 168, "22 KB", NOW - 6 * HOUR),
        Dataset("d4", "tierra", "Metales en suelo — muestreo", "sur", "Trimestre actual", "CSV", 90, "14 KB", NOW - 1 * DAY),
        Dataset("d5", "agua", "Cumplimiento normativo — registro completo", "rio", "Últimos 30 días", "JSON", 720, "120 KB", NOW - 1 * DAY),
        Dataset("d6", "aire", "Gases (SO₂ y NO₂) — registro horario", "norte", "Últimos 7 días", "JSON", 168, "41 KB", NOW - 8 * HOUR),
        Dataset("d7", "ruido", "Resumen mensual de ruido por estación", "sur", "Últimos 30 días", "CSV", 720, "88 KB", NOW - 2 * DAY),
        Dataset("d8", "tierra", "pH y humedad del suelo", "norte", "Últimos 30 días", "CSV", 720, "88 KB", NOW - 2 * DAY),
        Dataset("d9", "agua", "Sulfatos y conductividad", "rio", "Trimestre actual", "JSON", 2160, "210 KB", NOW - 3 * DAY),
        Dataset("d10", "aire", "Polvo sedimentable — campaña", "camino", "Trimestre actual", "CSV", 90, "12 KB", NOW - 4 * DAY),
    )

    val alerts = listOf(
        Alert("a1", "agua", Status.CRIT, "activa", "Arsénico en agua sobre el límite normativo",
            "La Estación Río Bajo registró 0,013 mg/L (límite 0,010 mg/L) en la última medición.",
            "Arsénico", "0,013 mg/L", "rio", NOW - (2 * HOUR + 20 * 60_000L)),
        Alert("a2", "ruido", Status.CRIT, "activa", "Pico de ruido por tronadura sobre el límite nocturno",
            "Se detectó un pico de 84 dB(A) durante una tronadura fuera del horario autorizado.",
            "Pico máximo", "84 dB(A)", "camino", NOW - 5 * HOUR),
        Alert("a3", "aire", Status.WARN, "activa", "PM10 acercándose al límite diario",
            "El polvo en suspensión subió a 47 µg/m³ por viento y tránsito de camiones.",
            "PM10", "47 µg/m³", "norte", NOW - 9 * HOUR),
        Alert("a4", "tierra", Status.WARN, "activa", "Acidez del suelo en descenso",
            "El pH del suelo bajó a 5,8 en la Estación Sur; conviene seguir su evolución.",
            "pH del suelo", "5,8", "sur", NOW - 14 * HOUR),
        Alert("a5", "agua", Status.WARN, "reconocida", "Turbidez elevada tras lluvia",
            "La turbidez llegó a 6,1 NTU en el canal de riego; la Junta de Vecinos fue notificada.",
            "Turbidez", "6,1 NTU", "rio", NOW - (1 * DAY + 3 * HOUR)),
        Alert("a6", "aire", Status.CRIT, "resuelta", "Episodio de polvo controlado",
            "El PM10 superó el límite por 3 horas durante el acopio. Se aplicó riego de caminos.",
            "PM10", "58 µg/m³", "camino", NOW - (2 * DAY + 6 * HOUR)),
        Alert("a7", "ruido", Status.WARN, "resuelta", "Ruido diurno sostenido sobre lo habitual",
            "Tránsito intenso elevó el ruido diurno a 56 dB(A) durante la mañana.",
            "Ruido diurno", "56 dB(A)", "sur", NOW - 3 * DAY),
        Alert("a8", "tierra", Status.CRIT, "resuelta", "Plomo en suelo sobre referencia en un punto",
            "Un muestreo puntual marcó 312 mg/kg (límite 300). Se reforzó el monitoreo del sector.",
            "Plomo", "312 mg/kg", "norte", NOW - 5 * DAY),
    )

    // -------------------- Lookups --------------------
    fun domain(tipo: String): Domain? = domains.find { it.tipo == tipo }
    fun metric(tipo: String, key: String): Metric = domain(tipo)!!.metrics.first { it.key == key }
    fun station(id: String): Station? = stations.find { it.id == id }
    fun stationName(id: String): String = station(id)?.nombre ?: id

    // -------------------- Series --------------------
    private val serieCache = HashMap<String, List<Point>>()

    fun series(tipo: String, metricKey: String, rango: String = "24h"): List<Point> {
        val cacheKey = "$tipo:$metricKey:$rango"
        serieCache[cacheKey]?.let { return it }
        val m = metric(tipo, metricKey)
        val (points, step) = when (rango) {
            "7d" -> 7 to 24
            "30d" -> 30 to 24
            else -> 24 to 1
        }
        val s = buildSerie(m, points, step)
        serieCache[cacheKey] = s
        return s
    }

    private fun buildSerie(m: Metric, points: Int, stepHours: Int): List<Point> {
        val rand = rng(m.seed + points)
        val out = ArrayList<Point>(points)
        for (i in points - 1 downTo 0) {
            val t = NOW - i.toLong() * stepHours * HOUR
            val phase = (points - i).toDouble() / points
            val diurnal = sin(phase * PI * 2 * (if (stepHours <= 1) 2 else 4))
            val drift = sin(phase * PI * 1.3 + m.seed)
            val noise = rand() - 0.5
            var v = m.baseline + m.amp * (0.45 * diurnal + 0.3 * drift + 0.5 * noise)
            if (rand() > 0.93) v += m.amp * (if (m.lowerIsWorse) -0.7 else 1.1)
            v = if (m.key == "ph" || m.key == "phSuelo") v.coerceIn(4.5, 8.6) else maxOf(0.0, v)
            out.add(Point(t, round(v, m.dec)))
        }
        return out
    }

    fun current(tipo: String, key: String): Double = series(tipo, key, "24h").last().v

    fun severityRatio(value: Double, m: Metric): Double =
        if (m.lowerIsWorse) m.limite / maxOf(value, 0.0001) else value / m.limite

    fun statusOf(value: Double, m: Metric): Status {
        val r = severityRatio(value, m)
        return when {
            r >= 1.0 -> Status.CRIT
            r >= 0.8 -> Status.WARN
            else -> Status.OK
        }
    }

    fun domainStatus(tipo: String): Status {
        val d = domain(tipo)!!
        val estados = d.metrics.map { statusOf(current(tipo, it.key), it) }
        return when {
            estados.contains(Status.CRIT) -> Status.CRIT
            estados.contains(Status.WARN) -> Status.WARN
            else -> Status.OK
        }
    }

    fun trendOf(tipo: String, key: String): Trend {
        val s = series(tipo, key, "24h")
        val half = s.size / 2
        val prev = s.take(half).map { it.v }.average()
        val now = s.drop(half).map { it.v }.average()
        val diff = if (prev == 0.0) 0.0 else (now - prev) / prev * 100
        val dir = when {
            diff > 2 -> "up"
            diff < -2 -> "down"
            else -> "flat"
        }
        return Trend(diff.roundToInt(), dir)
    }

    fun stationStatus(id: String): Status {
        val activas = alerts.filter { it.estacion == id && it.estado != "resuelta" }
        return when {
            activas.any { it.nivel == Status.CRIT } -> Status.CRIT
            activas.any { it.nivel == Status.WARN } -> Status.WARN
            else -> Status.OK
        }
    }

    // -------------------- Informes --------------------
    fun buildCSV(tipo: String): String {
        val d = domain(tipo)!!
        val headers = listOf("fecha_hora") + d.metrics.map { "${it.key}_${if (it.unit.isEmpty()) "idx" else it.unit}" }
        val cols = d.metrics.map { series(tipo, it.key, "30d") }
        val sb = StringBuilder()
        sb.append("# Vigía — ${d.titulo}\n")
        sb.append(headers.joinToString(",")).append("\n")
        for (i in cols[0].indices) {
            val fecha = Instant.ofEpochMilli(cols[0][i].t).toString()
            val fila = listOf(fecha) + cols.map { it[i].v.toString() }
            sb.append(fila.joinToString(",")).append("\n")
        }
        return sb.toString()
    }

    fun buildJSON(tipo: String): String {
        val d = domain(tipo)!!
        val root = JSONObject()
        root.put("fuente", "Vigía — datos abiertos")
        root.put("dominio", d.titulo)
        root.put("generado", Instant.ofEpochMilli(NOW).toString())
        val seriesObj = JSONObject()
        d.metrics.forEach { m ->
            val arr = JSONArray()
            series(tipo, m.key, "30d").forEach { p ->
                val o = JSONObject()
                o.put("fecha", Instant.ofEpochMilli(p.t).toString())
                o.put("valor", p.v)
                o.put("unidad", m.unit)
                o.put("limite", m.limite)
                arr.put(o)
            }
            seriesObj.put(m.key, arr)
        }
        root.put("series", seriesObj)
        return root.toString(2)
    }
}

// -------------------- Utilidades --------------------
private fun rng(seed: Int): () -> Double {
    var a = seed
    return {
        a += 0x6D2B79F5
        var t = (a xor (a ushr 15)) * (a or 1)
        t = (t + ((t xor (t ushr 7)) * (t or 61))) xor t
        ((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL).toDouble() / 4294967296.0
    }
}

private fun round(v: Double, dec: Int): Double {
    var f = 1.0
    repeat(dec) { f *= 10 }
    return (v * f).roundToLong() / f
}

private fun Double.roundToInt(): Int = Math.round(this).toInt()

// -------------------- Formato --------------------
private val esCL = Locale("es", "CL")
private val horaFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.UTC)
private val fechaFmt = DateTimeFormatter.ofPattern("dd MMM", esCL).withZone(ZoneOffset.UTC)

fun fmt(value: Double, dec: Int = 2): String = String.format(esCL, "%,.${dec}f", value)

fun fmtHora(ts: Long): String = horaFmt.format(Instant.ofEpochMilli(ts))

fun fmtFecha(ts: Long): String = fechaFmt.format(Instant.ofEpochMilli(ts))

fun desde(ts: Long): String {
    val diff = NOW - ts
    val min = (diff / 60_000L).toInt()
    if (min < 60) return "hace $min min"
    val h = min / 60
    if (h < 24) return "hace $h h"
    val d = h / 24
    return "hace $d d"
}

val statusLabelFull = mapOf(
    Status.OK to "Dentro de norma",
    Status.WARN to "En vigilancia",
    Status.CRIT to "Sobre el límite",
)
val statusLabelShort = mapOf(
    Status.OK to "Normal",
    Status.WARN to "Vigilancia",
    Status.CRIT to "Crítico",
)
val nivelLabel = mapOf(Status.OK to "Informativa", Status.WARN to "Precaución", Status.CRIT to "Crítica")
val estadoLabel = mapOf("activa" to "Activa", "reconocida" to "Reconocida", "resuelta" to "Resuelta")
