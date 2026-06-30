package cl.vigia.app.data

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
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

/* ============================================================
   Repositorio de la demostración.

   Cuatro zonas mineras ficticias, cada una con su territorio,
   estaciones, alertas y conjuntos de datos. Las series son
   deterministas (semilla fija) para una demo estable, pero cada
   zona tiene un "perfil ambiental" propio: `intent` fija, por
   dominio, el nivel de severidad típico (1.0 = justo en el límite),
   de modo que una zona resalte por el agua, otra por el aire, etc.
   ============================================================ */

object Repo {

    // -------------------- Dominios (normativos, iguales en todas las zonas) --------------------
    val domains = listOf(
        Domain(
            tipo = "agua", nombre = "Agua", titulo = "Calidad del agua",
            color = Agua, soft = AguaSoft, sub = "Vertientes y canales del sector",
            descripcion = "Seguimiento de la calidad del agua superficial aguas abajo de la faena: metales, acidez y turbidez que pueden afectar el riego y el consumo.",
            principal = "arsenico",
            metrics = listOf(
                Metric("arsenico", "Arsénico", "mg/L", 0.01, 0.006, 0.004, 101, 3),
                Metric("cobre", "Cobre", "mg/L", 1.0, 0.42, 0.22, 102, 2),
                Metric("ph", "pH", "", 6.5, 8.2, 0.4, 103, 1, lowerIsWorse = true),
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
                Metric("phSuelo", "pH del suelo", "", 5.5, 7.0, 0.5, 304, 1, lowerIsWorse = true),
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

    // -------------------- Zonas --------------------
    val zones = listOf(
        Zone(
            id = "cerro-bayo", nombre = "Mina Cerro Bayo", faena = "Cobre · tajo abierto",
            comuna = "Valle El Tambo", seed = 11,
            descripcion = "Faena de cobre en torno a la quebrada. El foco está en el agua: el drenaje aguas abajo arrastra metales y acidez que llegan a los canales de riego del sector.",
            stations = listOf(
                Station("cb-norte", "Estación Vega Norte", "Quebrada El Tambo", 28f, 30f),
                Station("cb-rio", "Estación Río Bajo", "Vega del Estero", 22f, 64f),
                Station("cb-canal", "Estación Canal Riego", "Los Maitenes", 52f, 56f),
                Station("cb-faena", "Estación Salida Faena", "Portezuelo", 74f, 28f),
            ),
            intent = mapOf("agua" to 1.18, "aire" to 0.50, "tierra" to 0.50, "ruido" to 0.50),
        ),
        Zone(
            id = "el-algarrobo", nombre = "Faena El Algarrobo", faena = "Hierro · tajo abierto",
            comuna = "Pampa Larga", seed = 23,
            descripcion = "Faena de hierro a tajo abierto. El polvo en suspensión del acopio y el transporte es el principal desafío para los poblados cercanos.",
            stations = listOf(
                Station("al-acopio", "Estación Acopio", "Pampa Seca", 70f, 26f),
                Station("al-pueblo", "Estación El Algarrobo", "Villa El Algarrobo", 34f, 42f),
                Station("al-camino", "Estación Camino Minero", "Portezuelo", 58f, 68f),
                Station("al-escuela", "Estación Escuela", "Bajo Hondo", 24f, 72f),
            ),
            intent = mapOf("aire" to 1.16, "agua" to 0.52, "tierra" to 0.50, "ruido" to 0.50),
        ),
        Zone(
            id = "quebrada-honda", nombre = "Mina Quebrada Honda", faena = "Oro y plata · subterránea",
            comuna = "Quebrada Honda", seed = 37,
            descripcion = "Mina de oro y plata en una quebrada estrecha. Las tronaduras y el tránsito nocturno de camiones marcan el ruido que afecta el descanso de la comunidad.",
            stations = listOf(
                Station("qh-rajo", "Estación Borde Rajo", "Quebrada Honda", 72f, 24f),
                Station("qh-poblado", "Estación El Poblado", "Los Almendros", 30f, 62f),
                Station("qh-mirador", "Estación Mirador", "Cuesta Larga", 56f, 38f),
            ),
            intent = mapOf("ruido" to 1.17, "agua" to 0.50, "aire" to 0.48, "tierra" to 0.48),
        ),
        Zone(
            id = "llano-verde", nombre = "Planta Llano Verde", faena = "Litio y sales · planta",
            comuna = "Llano Verde", seed = 53,
            descripcion = "Planta de procesamiento de sales y litio en el llano. En general dentro de norma; se vigila la salud del suelo agrícola colindante.",
            stations = listOf(
                Station("lv-pozas", "Estación Pozas", "Salar Chico", 64f, 30f),
                Station("lv-norte", "Estación Llano Norte", "Llano Verde", 30f, 34f),
                Station("lv-canal", "Estación Canal Sur", "Bajo el Llano", 40f, 66f),
                Station("lv-acceso", "Estación Acceso", "El Cruce", 76f, 60f),
            ),
            intent = mapOf("tierra" to 0.55, "agua" to 0.48, "aire" to 0.45, "ruido" to 0.45),
        ),
    )

    val periodos = listOf("Últimas 24 horas", "Últimos 7 días", "Últimos 30 días", "Trimestre actual")
    val formatos = listOf("CSV", "JSON")

    // -------------------- Lookups --------------------
    fun domain(tipo: String): Domain? = domains.find { it.tipo == tipo }
    fun metric(tipo: String, key: String): Metric = domain(tipo)!!.metrics.first { it.key == key }

    val defaultZone: String get() = zones.first().id
    fun zone(id: String): Zone = zones.find { it.id == id } ?: zones.first()

    private val allStations: List<Station> by lazy { zones.flatMap { it.stations } }
    fun station(id: String): Station? = allStations.find { it.id == id }
    fun stationName(id: String): String = station(id)?.nombre ?: id

    // -------------------- Series por zona (en vivo) --------------------
    // Las lecturas son una función continua del tiempo: cambian cada 5 minutos
    // (cuantizadas por LiveSim), como si cada sensor reportara en ese intervalo.

    fun series(zoneId: String, tipo: String, metricKey: String, rango: String = "24h"): List<Point> {
        val end = LiveSim.readingTime
        val (n, stepMs) = when (rango) {
            "7d" -> 7 to DAY
            "30d" -> 30 to DAY
            else -> 24 to HOUR
        }
        return (n - 1 downTo 0).map { k ->
            val t = end - k * stepMs
            Point(t, valueAt(zoneId, tipo, metricKey, t))
        }
    }

    /** Valor de una métrica en un instante dado (cuantizado a pasos de 5 min). */
    fun valueAt(zoneId: String, tipo: String, key: String, t: Long): Double {
        val z = zone(zoneId)
        val m = metric(tipo, key)
        val factor = domainFactor(zoneId, tipo)
        val step = t / LiveSim.FIVE_MIN
        val dayFrac = (t % DAY).toDouble() / DAY
        val diurnal = sin(dayFrac * 2 * PI + m.seed)
        val drift = sin(t / (DAY * 2.0) * PI + z.seed + m.seed)
        val noise = hashNoise(m.seed + z.seed * 7, step) - 0.5
        var v = m.baseline + m.amp * (0.30 * diurnal + 0.22 * drift + 0.30 * noise)
        if (hashNoise(m.seed * 3 + z.seed, step) > 0.96) v += m.amp * (if (m.lowerIsWorse) -0.7 else 0.9)
        return applyFactor(v, m, factor)
    }

    /**
     * Factor de la zona para un dominio: escala las series para que la métrica
     * principal de ese dominio quede, en promedio, en el nivel de severidad
     * fijado por `intent`. Se deriva de la línea base (estable en el tiempo).
     */
    private fun domainFactor(zoneId: String, tipo: String): Double {
        val z = zone(zoneId)
        val d = domain(tipo)!!
        val principal = d.metrics.first { it.key == d.principal }
        val naturalRatio = (principal.baseline / principal.limite).coerceAtLeast(1e-6)
        val target = z.intent[tipo] ?: 0.6
        return (target / naturalRatio).coerceIn(0.05, 12.0)
    }

    private fun applyFactor(v: Double, m: Metric, factor: Double): Double {
        // Para métricas donde "menos es peor", reduce el margen sobre el mínimo;
        // para el resto, escala el valor. Así el sesgo siempre empeora/mejora de forma coherente.
        val w = if (m.lowerIsWorse) m.limite + (v - m.limite) / factor else v * factor
        val clamped = if (m.key == "ph" || m.key == "phSuelo") w.coerceIn(4.0, 8.8) else maxOf(0.0, w)
        return round(clamped, m.dec)
    }

    fun current(zoneId: String, tipo: String, key: String): Double =
        valueAt(zoneId, tipo, key, LiveSim.readingTime)

    // -------------------- Estados --------------------
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

    fun domainStatus(zoneId: String, tipo: String): Status {
        val d = domain(tipo)!!
        val estados = d.metrics.map { statusOf(current(zoneId, tipo, it.key), it) }
        return worst(estados)
    }

    fun zoneStatus(zoneId: String): Status = worst(domains.map { domainStatus(zoneId, it.tipo) })

    fun trendOf(zoneId: String, tipo: String, key: String): Trend {
        val s = series(zoneId, tipo, key, "24h")
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

    private fun worst(estados: List<Status>): Status = when {
        estados.contains(Status.CRIT) -> Status.CRIT
        estados.contains(Status.WARN) -> Status.WARN
        else -> Status.OK
    }

    // -------------------- Conjuntos de datos por zona --------------------
    val datasets: List<Dataset> by lazy { zones.flatMap { genDatasets(it) } }

    fun datasetsOf(zoneId: String): List<Dataset> = datasets.filter { it.zona == zoneId }

    private fun genDatasets(z: Zone): List<Dataset> {
        val doms = z.intent.entries.sortedByDescending { it.value }.map { domain(it.key)!! }
        val plan = listOf(
            Triple(doms[0], "Últimos 7 días", "CSV"),
            Triple(doms[0], "Últimos 30 días", "JSON"),
            Triple(doms[1], "Últimos 7 días", "CSV"),
            Triple(doms.getOrElse(2) { doms[1] }, "Trimestre actual", "CSV"),
            Triple(doms.getOrElse(3) { doms[0] }, "Últimos 30 días", "CSV"),
        )
        return plan.mapIndexed { i, (d, per, fmto) ->
            val st = z.stations[i % z.stations.size]
            val filas = when (per) {
                "Últimos 7 días" -> 168
                "Últimos 30 días" -> 720
                "Trimestre actual" -> 2160
                else -> 24
            }
            val peso = "${maxOf(8, filas / 7 + d.metrics.size * 4)} KB"
            Dataset(
                "${z.id}-d$i", z.id, d.tipo,
                "${d.titulo} — ${st.nombre.removePrefix("Estación ")}",
                st.id, per, fmto, filas, peso, NOW - (i + 1) * 8 * HOUR,
            )
        }
    }

    // -------------------- Informes --------------------
    fun buildCSV(zoneId: String, tipo: String): String {
        val d = domain(tipo)!!
        val z = zone(zoneId)
        val headers = listOf("fecha_hora") + d.metrics.map { "${it.key}_${if (it.unit.isEmpty()) "idx" else it.unit}" }
        val cols = d.metrics.map { series(zoneId, tipo, it.key, "30d") }
        val sb = StringBuilder()
        sb.append("# Vigía — ${d.titulo} — ${z.nombre}\n")
        sb.append(headers.joinToString(",")).append("\n")
        for (i in cols[0].indices) {
            val fecha = Instant.ofEpochMilli(cols[0][i].t).toString()
            val fila = listOf(fecha) + cols.map { it[i].v.toString() }
            sb.append(fila.joinToString(",")).append("\n")
        }
        return sb.toString()
    }

    fun buildJSON(zoneId: String, tipo: String): String {
        val d = domain(tipo)!!
        val z = zone(zoneId)
        val root = JSONObject()
        root.put("fuente", "Vigía — datos abiertos")
        root.put("zona", z.nombre)
        root.put("dominio", d.titulo)
        root.put("generado", Instant.ofEpochMilli(NOW).toString())
        val seriesObj = JSONObject()
        d.metrics.forEach { m ->
            val arr = JSONArray()
            series(zoneId, tipo, m.key, "30d").forEach { p ->
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

/** Valor pseudoaleatorio estable en [0,1) para un par (semilla, paso). */
internal fun hashNoise(seed: Int, step: Long): Double =
    rng(seed * 374761393 + step.toInt() * 668265263)()

private fun round(v: Double, dec: Int): Double {
    var f = 1.0
    repeat(dec) { f *= 10 }
    return (v * f).roundToLong() / f
}
