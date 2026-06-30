package cl.vigia.app.data

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ============================================================
   Modelo de datos de la demostración (todo en el dispositivo).
   Lugares, faenas y personas ficticios.
   ============================================================ */

enum class Status { OK, WARN, CRIT }

data class Point(val t: Long, val v: Double)

/** Una variable medida dentro de un dominio (p. ej. Arsénico dentro de Agua). */
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

/** Un dominio ambiental: agua, aire, tierra o ruido. Es normativo: igual en todas las zonas. */
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

/** Estación de monitoreo dentro de una zona. x/y son porcentajes (0–100) sobre el mapa. */
data class Station(val id: String, val nombre: String, val sector: String, val x: Float, val y: Float)

/**
 * Una zona minera con su propio territorio, estaciones, alertas y datos.
 * `intent` fija, por dominio, el nivel de severidad típico (1.0 = en el límite),
 * de modo que cada zona tenga un "perfil ambiental" propio y reconocible.
 */
data class Zone(
    val id: String,
    val nombre: String,
    val faena: String,
    val comuna: String,
    val descripcion: String,
    val seed: Int,
    val stations: List<Station>,
    val intent: Map<String, Double>,
)

data class Alert(
    val id: String,
    val zona: String,
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
    val zona: String,
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

const val HOUR = 3600_000L
const val DAY = 24 * HOUR

// Ancla temporal fija (UTC) para etiquetas estables en la demostración.
val NOW: Long = Instant.parse("2026-06-23T14:00:00Z").toEpochMilli()

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

// -------------------- Etiquetas --------------------
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
