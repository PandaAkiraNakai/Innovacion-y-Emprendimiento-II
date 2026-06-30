package cl.vigia.app.data

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/* ============================================================
   Motor "en vivo" de la demostración.

   - El tiempo avanza con el reloj real. Las lecturas se actualizan
     cada 5 minutos (como si cada sensor reportara en ese intervalo).
   - Las alertas son una FUNCIÓN PURA del reloj: cada hora puede
     generar un aviso, con nivel (alta/media/baja) y un estado que
     evoluciona con su antigüedad (activa → reconocida → resuelta).
     Así el feed se mantiene vivo sin estado mutable que mantener.
   ============================================================ */

object LiveSim {

    const val FIVE_MIN = 5 * 60_000L
    private const val WINDOW_HOURS = 72L // horas de historial visible de alertas

    fun quantize(t: Long): Long = (t / FIVE_MIN) * FIVE_MIN

    // Marca de la última lectura (alineada a 5 min). La leen las pantallas;
    // al cambiar (cada 5 min) recomponen y muestran datos nuevos.
    private var _readingTime by mutableLongStateOf(quantize(System.currentTimeMillis()))
    val readingTime: Long get() = _readingTime

    /** Mantiene la marca de lectura al día. Se lanza una vez desde VigiaApp. */
    suspend fun run() {
        while (true) {
            _readingTime = quantize(System.currentTimeMillis())
            delay(10_000)
        }
    }

    /** Segundos hasta la próxima lectura (para una cuenta regresiva en la UI). */
    fun secondsToNextReading(sysNow: Long): Long {
        val next = quantize(sysNow) + FIVE_MIN
        return ((next - sysNow) / 1000).coerceAtLeast(0)
    }

    // -------------------- Alertas en vivo --------------------
    fun alertsOf(zoneId: String): List<Alert> = liveAlerts(readingTime).filter { it.zona == zoneId }

    fun stationStatus(zoneId: String, stationId: String): Status {
        val activas = alertsOf(zoneId).filter { it.estacion == stationId && it.estado != "resuelta" }
        return when {
            activas.any { it.nivel == Status.CRIT } -> Status.CRIT
            activas.any { it.nivel == Status.WARN } -> Status.WARN
            else -> Status.OK
        }
    }

    private fun liveAlerts(now: Long): List<Alert> {
        val out = ArrayList<Alert>()
        // 1) Alertas vigentes derivadas de las lecturas actuales de cada zona.
        Repo.zones.forEach { z -> out += standingAlerts(z, now) }
        // 2) Feed horario: un posible aviso por hora, con su ciclo de vida.
        val curHour = now / HOUR
        var h = curHour - WINDOW_HOURS
        while (h <= curHour) {
            alertForHour(h, curHour)?.let { out.add(it) }
            h++
        }
        return out.sortedByDescending { it.ts }
    }

    /** Alertas que reflejan la condición actual: por cada dominio fuera de norma, un aviso activo. */
    private fun standingAlerts(z: Zone, now: Long): List<Alert> {
        val out = ArrayList<Alert>()
        Repo.domains.forEachIndexed { idx, d ->
            val worst = d.metrics.maxByOrNull {
                Repo.severityRatio(Repo.current(z.id, d.tipo, it.key), it)
            } ?: return@forEachIndexed
            val v = Repo.current(z.id, d.tipo, worst.key)
            val st = Repo.statusOf(v, worst)
            if (st == Status.OK) return@forEachIndexed
            val station = z.stations[idx % z.stations.size]
            val valTxt = fmtVal(v, worst)
            val titulo = when {
                st == Status.CRIT && worst.lowerIsWorse -> "${worst.label} bajo el mínimo en ${d.nombre.lowercase()}"
                st == Status.CRIT -> "${worst.label} sobre el límite normativo"
                worst.lowerIsWorse -> "${worst.label} acercándose al mínimo"
                else -> "${worst.label} cerca del límite permitido"
            }
            val limTxt = "${if (worst.lowerIsWorse) "mínimo" else "límite"} ${fmt(worst.limite, worst.dec)} ${worst.unit}".trim()
            val detalle = "Lectura en vivo: la ${station.nombre} registra $valTxt ($limTxt)."
            val ts = now - (4 + idx * 6) * 60_000L
            out.add(Alert("${z.id}-now-${d.tipo}", z.id, d.tipo, st, "activa", titulo, detalle, worst.label, valTxt, station.id, ts))
        }
        return out
    }

    /** Decide, de forma determinista, si la hora `h` genera un aviso y cuál. */
    private fun alertForHour(h: Long, curHour: Long): Alert? {
        if (hashNoise(909, h) > 0.62) return null // ~38% de las horas traen aviso

        val zones = Repo.zones
        val z = zones[(hashNoise(131, h) * zones.size).toInt().coerceIn(0, zones.size - 1)]
        // 55% el dominio sensible de la zona; el resto, cualquiera.
        val tipo = if (hashNoise(257, h) < 0.55) {
            z.intent.maxByOrNull { it.value }!!.key
        } else {
            Repo.domains[(hashNoise(373, h) * Repo.domains.size).toInt().coerceIn(0, Repo.domains.size - 1)].tipo
        }
        val d = Repo.domain(tipo)!!
        val m = d.metrics[(hashNoise(421, h) * d.metrics.size).toInt().coerceIn(0, d.metrics.size - 1)]
        val station = z.stations[(hashNoise(547, h) * z.stations.size).toInt().coerceIn(0, z.stations.size - 1)]

        val rl = hashNoise(631, h)
        val nivel = when {
            rl < 0.30 -> Status.CRIT
            rl < 0.70 -> Status.WARN
            else -> Status.OK
        }
        val ratio = when (nivel) {
            Status.CRIT -> 1.02 + hashNoise(11, h) * 0.28
            Status.WARN -> 0.82 + hashNoise(13, h) * 0.15
            else -> 0.60 + hashNoise(17, h) * 0.15
        }
        val v = if (m.lowerIsWorse) m.limite / ratio else m.limite * ratio
        val valTxt = fmtVal(v, m)

        val age = curHour - h
        val estado = when {
            nivel == Status.OK -> if (age >= 2) "resuelta" else "activa"
            age >= 18 -> "resuelta"
            age >= 6 -> "reconocida"
            else -> "activa"
        }

        val titulo = when {
            nivel == Status.CRIT && m.lowerIsWorse -> "${m.label} bajo el mínimo en ${d.nombre.lowercase()}"
            nivel == Status.CRIT -> "${m.label} sobre el límite normativo"
            nivel == Status.WARN && m.lowerIsWorse -> "${m.label} acercándose al mínimo"
            nivel == Status.WARN -> "${m.label} cerca del límite permitido"
            else -> "Lectura de ${d.nombre.lowercase()} dentro de rango"
        }
        val limTxt = "${if (m.lowerIsWorse) "mínimo" else "límite"} ${fmt(m.limite, m.dec)} ${m.unit}".trim()
        val detalle = if (nivel == Status.OK) {
            "Reporte informativo: ${m.label.lowercase()} en $valTxt en la ${station.nombre}."
        } else {
            "La ${station.nombre} registró $valTxt ($limTxt)."
        }
        val ts = h * HOUR + (hashNoise(701, h) * 55).toLong() * 60_000L
        return Alert("h$h", z.id, tipo, nivel, estado, titulo, detalle, m.label, valTxt, station.id, ts)
    }
}
