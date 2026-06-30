package cl.vigia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.LiveSim
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.data.fmtHora
import cl.vigia.app.ui.components.AlertRow
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.KpiCard
import cl.vigia.app.ui.components.LiveBadge
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.SensorCard
import cl.vigia.app.ui.components.StatusPill
import cl.vigia.app.ui.components.ZoneSelector
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.MonoValue
import cl.vigia.app.ui.theme.Ok

@Composable
fun DashboardScreen(
    zoneId: String,
    onSelectZone: (String) -> Unit,
    onOpenSensor: (String) -> Unit,
    onGoAlertas: () -> Unit,
    onGoDatos: () -> Unit,
    onGoMapa: () -> Unit,
) {
    val zone = Repo.zone(zoneId)
    val estados = Repo.domains.map { Repo.domainStatus(zoneId, it.tipo) }
    val general = Repo.zoneStatus(zoneId)
    val activas = LiveSim.alertsOf(zoneId).filter { it.estado == "activa" }
    val enNorma = estados.count { it == Status.OK }
    val recientes = LiveSim.alertsOf(zoneId).sortedByDescending { it.ts }.take(3)

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Monitoreo comunitario · ${zone.comuna}",
            title = "Hola, María",
            subtitle = "Así está hoy la calidad ambiental cerca de la faena. Elige una zona y toca un sensor para ver el detalle.",
            action = {
                IconBadge(Icons.Outlined.FileDownload, MossSoft, Moss, boxSize = 48.dp, corner = 16.dp, iconSize = 20.dp, contentDescription = "Descargar datos", modifier = Modifier.clip(CircleShape).clickable { onGoDatos() })
            },
        )
        Spacer(Modifier.height(20.dp))

        ZoneSelector(zoneId, onSelectZone)
        Spacer(Modifier.height(16.dp))

        LiveBadge()
        Spacer(Modifier.height(16.dp))

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
                Text(fmtHora(LiveSim.readingTime), style = MonoValue)
            }
        }
        Spacer(Modifier.height(26.dp))

        Text("Sensores de ${zone.nombre}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(14.dp))
        Repo.domains.forEach { d ->
            SensorCard(zoneId, d.tipo, onClick = { onOpenSensor(d.tipo) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
        }

        Spacer(Modifier.height(12.dp))

        // Acceso al mapa (ahora es una página propia)
        SectionCard(modifier = Modifier.clickable { onGoMapa() }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconBadge(Icons.Outlined.Map, MossSoft, Moss, boxSize = 46.dp, corner = 14.dp, iconSize = 22.dp)
                Column(Modifier.weight(1f)) {
                    Eyebrow("Territorio")
                    Text("Ver el mapa del sector", style = MaterialTheme.typography.titleLarge)
                    Text("${zone.stations.size} estaciones sobre el territorio de ${zone.nombre}", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                Icon(Icons.Filled.KeyboardArrowRight, null, tint = Moss, modifier = Modifier.size(22.dp))
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
