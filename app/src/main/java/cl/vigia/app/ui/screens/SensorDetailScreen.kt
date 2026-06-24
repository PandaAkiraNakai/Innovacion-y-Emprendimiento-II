@file:OptIn(ExperimentalLayoutApi::class)

package cl.vigia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Repo
import cl.vigia.app.data.fmt
import cl.vigia.app.ui.components.AlertRow
import cl.vigia.app.ui.components.ChipToggle
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.EmptyState
import cl.vigia.app.ui.components.Gauge
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.MetricTile
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.StatusPill
import cl.vigia.app.ui.components.AreaChart
import cl.vigia.app.ui.components.TrendChip
import cl.vigia.app.ui.components.domainIcon
import cl.vigia.app.ui.theme.Ink
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.MonoBig
import cl.vigia.app.ui.theme.MonoSmall
import cl.vigia.app.ui.theme.MossDeep
import cl.vigia.app.ui.theme.MossSoft

private val RANGOS = listOf("24h" to "24 h", "7d" to "7 días", "30d" to "30 días")

@Composable
fun SensorDetailScreen(tipo: String, onBack: () -> Unit, onGoPerfil: () -> Unit) {
    val d = Repo.domain(tipo) ?: return
    var metricKey by remember(tipo) { mutableStateOf(d.principal) }
    var rango by remember(tipo) { mutableStateOf("24h") }

    val metric = Repo.metric(tipo, metricKey)
    val serie = Repo.series(tipo, metricKey, rango)
    // El valor y el estado se derivan de la MISMA serie que se grafica,
    // para que el número, el estado y el punto final coincidan con el rango.
    val valActual = serie.last().v
    val estado = Repo.statusOf(valActual, metric)
    val general = Repo.domainStatus(tipo)

    val principal = Repo.metric(tipo, d.principal)
    val valPrincipal = Repo.current(tipo, d.principal)
    val estadoPrincipal = Repo.statusOf(valPrincipal, principal)
    val ratioPrincipal = Repo.severityRatio(valPrincipal, principal)
    val trendPrincipal = Repo.trendOf(tipo, d.principal)

    val alertasDominio = Repo.alerts.filter { it.tipo == tipo }.sortedByDescending { it.ts }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        // Barra superior
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ink)
            }
            Eyebrow("Resumen · Sensor")
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(d.titulo, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            StatusPill(general, full = true)
        }
        Spacer(Modifier.height(8.dp))
        Text(d.descripcion, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Spacer(Modifier.height(20.dp))

        // Medidor principal
        SectionCard {
            Eyebrow("Medición principal")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                IconBadge(domainIcon(tipo), d.soft, d.color, boxSize = 34.dp, corner = 10.dp, iconSize = 19.dp)
                Text(principal.label, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Gauge(
                    ratio = ratioPrincipal,
                    status = estadoPrincipal,
                    valueText = fmt(valPrincipal, principal.dec),
                    unit = principal.unit,
                )
                Spacer(Modifier.height(10.dp))
                StatusPill(estadoPrincipal, full = true)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tendencia 24 h", style = MonoSmall, color = InkFaint)
                        Spacer(Modifier.height(2.dp))
                        TrendChip(trendPrincipal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Límite normativo", style = MonoSmall, color = InkFaint)
                        Spacer(Modifier.height(2.dp))
                        Text("${fmt(principal.limite, principal.dec)} ${principal.unit}".trim(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Evolución
        SectionCard {
            Eyebrow("Evolución")
            Spacer(Modifier.height(4.dp))
            Text(metric.label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                d.metrics.forEach { m ->
                    ChipToggle(
                        text = m.label,
                        selected = m.key == metricKey,
                        onClick = { metricKey = m.key },
                        selectedBg = d.color,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(fmt(valActual, metric.dec), style = MonoBig, color = d.color)
                    if (metric.unit.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text(metric.unit, style = MonoSmall, color = InkSoft)
                    }
                }
                StatusPill(estado, full = true)
            }
            Spacer(Modifier.height(10.dp))
            AreaChart(serie, d.color, metric.unit, metric.limite, rango, height = 230.dp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RANGOS.forEach { (value, label) ->
                    val on = value == rango
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) d.color else InkFaint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                            .background(if (on) d.soft else androidx.compose.ui.graphics.Color.Transparent)
                            .clickableRange { rango = value }
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // Todas las mediciones
        Text("Todas las mediciones", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        d.metrics.chunked(2).forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { m ->
                    MetricTile(tipo, m, Repo.current(tipo, m.key), Modifier.weight(1f))
                }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(10.dp))

        // Contexto
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Info, null, tint = d.color, modifier = Modifier.size(20.dp))
                Text("¿Qué significa y por qué importa?", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            Text(d.descripcion, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            Spacer(Modifier.height(12.dp))
            d.metrics.forEach { m ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(m.label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${if (m.lowerIsWorse) "mínimo" else "máximo"} ${fmt(m.limite, m.dec)} ${m.unit}".trim(),
                        style = MonoSmall, color = InkFaint,
                    )
                }
                androidx.compose.material3.HorizontalDivider(color = LineCol)
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MossSoft).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Outlined.Eco, null, tint = MossDeep, modifier = Modifier.size(18.dp))
                    Text("¿Notas algo raro?", style = MaterialTheme.typography.titleMedium, color = MossDeep)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Si percibes mal olor, polvo o ruido inusual, envía un reporte desde tu perfil para que quede registrado.",
                    style = MaterialTheme.typography.bodyMedium, color = InkSoft,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onGoPerfil, shape = RoundedCornerShape(50)) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar un reporte")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Alertas del dominio
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Warning, null, tint = cl.vigia.app.ui.theme.Crit, modifier = Modifier.size(20.dp))
                Text("Alertas de ${d.nombre.lowercase()}", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))
            if (alertasDominio.isEmpty()) {
                EmptyState(Icons.Outlined.Eco, "Sin alertas registradas para este sensor.")
            } else {
                alertasDominio.take(4).forEachIndexed { i, a ->
                    AlertRow(a, onOpen = {})
                    if (i < alertasDominio.take(4).lastIndex) Spacer(Modifier.height(12.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun Modifier.clickableRange(onClick: () -> Unit): Modifier =
    this.clickable { onClick() }
