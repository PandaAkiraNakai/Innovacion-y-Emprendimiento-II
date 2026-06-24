package cl.vigia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.NOW
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.data.fmtHora
import cl.vigia.app.ui.components.AlertRow
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.KpiCard
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.SensorCard
import cl.vigia.app.ui.components.StationMap
import cl.vigia.app.ui.components.StatusPill
import cl.vigia.app.ui.components.statusColor
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.MonoValue
import cl.vigia.app.ui.theme.Ok

@Composable
fun DashboardScreen(
    onOpenSensor: (String) -> Unit,
    onGoAlertas: () -> Unit,
    onGoDatos: () -> Unit,
) {
    val estados = Repo.domains.map { Repo.domainStatus(it.tipo) }
    val general = when {
        estados.contains(Status.CRIT) -> Status.CRIT
        estados.contains(Status.WARN) -> Status.WARN
        else -> Status.OK
    }
    val activas = Repo.alerts.filter { it.estado == "activa" }
    val enNorma = estados.count { it == Status.OK }
    val recientes = Repo.alerts.sortedByDescending { it.ts }.take(3)

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Resumen del sector · Quebrada Verde",
            title = "Hola, María",
            subtitle = "Así está hoy la calidad ambiental cerca de la faena. Toca un sensor para ver el detalle.",
            action = {
                IconBadge(Icons.Outlined.FileDownload, MossSoft, Moss, boxSize = 48.dp, corner = 16.dp, iconSize = 20.dp, contentDescription = "Descargar datos", modifier = Modifier.clip(CircleShape).clickable { onGoDatos() })
            },
        )
        Spacer(Modifier.height(22.dp))

        // Indicadores 2x2
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Estado general", Modifier.weight(1f), icon = Icons.Outlined.Shield) {
                StatusPill(general, full = true)
            }
            KpiCard("Alertas activas", Modifier.weight(1f), icon = Icons.Outlined.Notifications) {
                Text("${activas.size}", style = MonoValue, color = if (activas.isNotEmpty()) Crit else Ok)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Dominios en norma", Modifier.weight(1f), icon = Icons.Outlined.Layers) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$enNorma", style = MonoValue)
                    Text(" / 4", style = MaterialTheme.typography.bodyMedium, color = InkFaint)
                }
            }
            KpiCard("Última medición", Modifier.weight(1f), icon = Icons.Outlined.Schedule) {
                Text(fmtHora(NOW), style = MonoValue)
            }
        }
        Spacer(Modifier.height(26.dp))

        Text("Sensores del sector", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(14.dp))
        Repo.domains.forEach { d ->
            SensorCard(d.tipo, onClick = { onOpenSensor(d.tipo) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
        }

        Spacer(Modifier.height(12.dp))

        // Mapa
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Place, null, tint = Moss, modifier = Modifier.size(20.dp))
                Column {
                    Eyebrow("Territorio")
                    Text("Mapa del sector", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(14.dp))
            StationMap(Modifier.fillMaxWidth().height(210.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot("Normal", Ok)
                LegendDot("Vigilancia", statusColor(Status.WARN))
                LegendDot("Crítico", Crit)
            }
        }
        Spacer(Modifier.height(18.dp))

        // Alertas recientes
        SectionCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Eyebrow("Lo último")
                    Text("Alertas recientes", style = MaterialTheme.typography.titleLarge)
                }
                TextButton(onClick = onGoAlertas) { Text("Ver todas", color = Moss) }
            }
            Spacer(Modifier.height(12.dp))
            recientes.forEachIndexed { i, a ->
                AlertRow(a, onOpen = { onOpenSensor(a.tipo) })
                if (i < recientes.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkFaint)
    }
}
