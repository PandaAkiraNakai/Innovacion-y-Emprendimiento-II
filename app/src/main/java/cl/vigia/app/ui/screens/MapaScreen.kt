package cl.vigia.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Station
import cl.vigia.app.data.Status
import cl.vigia.app.data.desde
import cl.vigia.app.ui.components.AlertRow
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.StationMap
import cl.vigia.app.ui.components.StatusPill
import cl.vigia.app.ui.components.ZoneSelector
import cl.vigia.app.ui.components.statusColor
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.Ok
import cl.vigia.app.ui.theme.SurfaceCard

@Composable
fun MapaScreen(
    zoneId: String,
    onSelectZone: (String) -> Unit,
    onOpenSensor: (String) -> Unit,
    onGoAlertas: () -> Unit,
) {
    val zone = Repo.zone(zoneId)
    var selected by remember(zoneId) { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Territorio · ${zone.comuna}",
            title = "Mapa del sector",
            subtitle = "Las estaciones de monitoreo sobre el territorio de la faena. Toca una estación para ver su estado y sus alertas.",
        )
        Spacer(Modifier.height(20.dp))

        ZoneSelector(zoneId, onSelectZone)
        Spacer(Modifier.height(20.dp))

        // Mapa
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Place, null, tint = Moss, modifier = Modifier.size(20.dp))
                Column {
                    Eyebrow(zone.faena)
                    Text(zone.nombre, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(14.dp))
            StationMap(
                zone,
                Modifier.fillMaxWidth().height(280.dp).clip(MaterialTheme.shapes.medium),
                selectedStationId = selected,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot("Normal", Ok)
                LegendDot("Vigilancia", statusColor(Status.WARN))
                LegendDot("Crítico", Crit)
            }
        }
        Spacer(Modifier.height(18.dp))

        Text("Estaciones", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        zone.stations.forEach { st ->
            StationRow(
                zoneId = zoneId,
                station = st,
                expanded = selected == st.id,
                onClick = { selected = if (selected == st.id) null else st.id },
                onOpenSensor = onOpenSensor,
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onGoAlertas) { Text("Ver todas las alertas de la zona", color = Moss) }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StationRow(
    zoneId: String,
    station: Station,
    expanded: Boolean,
    onClick: () -> Unit,
    onOpenSensor: (String) -> Unit,
) {
    val status = Repo.stationStatus(zoneId, station.id)
    val alertas = Repo.alertsOf(zoneId).filter { it.estacion == station.id }.sortedByDescending { it.ts }
    val activas = alertas.count { it.estado != "resuelta" }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (expanded) MossSoft.copy(alpha = 0.4f) else SurfaceCard)
            .border(1.dp, if (expanded) Moss else LineCol, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(statusColor(status)))
            Column(Modifier.weight(1f)) {
                Text(station.nombre, style = MaterialTheme.typography.titleMedium)
                Text(station.sector, style = MaterialTheme.typography.bodySmall, color = InkFaint)
            }
            StatusPill(status)
        }
        Row(Modifier.padding(start = 24.dp, top = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                if (activas > 0) "$activas ${if (activas == 1) "alerta activa" else "alertas activas"}" else "Sin alertas activas",
                style = MaterialTheme.typography.bodySmall,
                color = if (activas > 0) Crit else InkSoft,
            )
            if (alertas.isNotEmpty()) {
                Text("· última ${desde(alertas.first().ts)}", style = MaterialTheme.typography.bodySmall, color = InkFaint)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(12.dp))
                if (alertas.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = Ok, modifier = Modifier.size(18.dp))
                        Text("Esta estación está dentro de norma.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    }
                } else {
                    alertas.forEachIndexed { i, a ->
                        AlertRow(a, onOpen = { onOpenSensor(a.tipo) })
                        if (i < alertas.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkFaint)
    }
}
