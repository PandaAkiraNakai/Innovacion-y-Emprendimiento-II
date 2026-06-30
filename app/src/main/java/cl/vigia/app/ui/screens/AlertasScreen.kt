@file:OptIn(ExperimentalLayoutApi::class)

package cl.vigia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.ui.components.AlertRow
import cl.vigia.app.ui.components.ChipToggle
import cl.vigia.app.ui.components.EmptyState
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.KpiCard
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.SegmentedControl
import cl.vigia.app.ui.components.ZoneSelector
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.MonoValue
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.Ok
import cl.vigia.app.ui.theme.Warn

private val ESTADOS = listOf(
    "todas" to "Todas", "activa" to "Activas", "reconocida" to "Reconocidas", "resuelta" to "Resueltas",
)

@Composable
fun AlertasScreen(
    zoneId: String,
    onSelectZone: (String) -> Unit,
    onOpenSensor: (String) -> Unit,
    onGoPerfil: () -> Unit,
) {
    var tipo by remember { mutableStateOf("todos") }
    var estado by remember { mutableStateOf("todas") }

    val zonaAlerts = Repo.alertsOf(zoneId)
    val lista = zonaAlerts
        .filter { tipo == "todos" || it.tipo == tipo }
        .filter { estado == "todas" || it.estado == estado }
        .sortedByDescending { it.ts }

    val activas = zonaAlerts.filter { it.estado == "activa" }
    val criticas = activas.count { it.nivel == Status.CRIT }
    val vigilancia = activas.count { it.nivel == Status.WARN }
    val resueltas = zonaAlerts.count { it.estado == "resuelta" }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Centro de alertas",
            title = "Alertas de la zona",
            subtitle = "Avisos cuando una medición se acerca o supera el límite normativo. Las críticas exigen atención inmediata.",
            action = {
                IconBadge(Icons.Outlined.NotificationsActive, MossSoft, Moss, boxSize = 48.dp, corner = 16.dp, iconSize = 20.dp, contentDescription = "Configurar avisos", modifier = Modifier.clip(CircleShape).clickable { onGoPerfil() })
            },
        )
        Spacer(Modifier.height(20.dp))

        ZoneSelector(zoneId, onSelectZone)
        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Activas", Modifier.weight(1f), icon = Icons.Outlined.Warning) {
                Text("${activas.size}", style = MonoValue, color = if (activas.isNotEmpty()) Crit else Ok)
            }
            KpiCard("Resueltas", Modifier.weight(1f), icon = Icons.Outlined.CheckCircle) {
                Text("$resueltas", style = MonoValue)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiDot("Críticas", criticas, Crit, Modifier.weight(1f))
            KpiDot("En vigilancia", vigilancia, Warn, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))

        // Filtros
        SectionCard {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipToggle("Todos", tipo == "todos", { tipo = "todos" })
                Repo.domains.forEach { d ->
                    ChipToggle(d.nombre, tipo == d.tipo, { tipo = d.tipo }, leadingDot = d.color, selectedBg = d.color)
                }
            }
            Spacer(Modifier.height(12.dp))
            SegmentedControl(ESTADOS, estado, { estado = it }, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(16.dp))

        if (lista.isEmpty()) {
            EmptyState(Icons.Outlined.CheckCircle, "No hay alertas que coincidan con el filtro.")
        } else {
            lista.forEachIndexed { i, a ->
                AlertRow(a, onOpen = { onOpenSensor(a.tipo) })
                if (i < lista.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun KpiDot(label: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    KpiCard(label, modifier, icon = null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(color))
            Text("$value", style = MonoValue)
        }
    }
}
