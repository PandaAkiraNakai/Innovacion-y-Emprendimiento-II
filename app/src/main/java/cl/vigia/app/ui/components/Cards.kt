@file:OptIn(ExperimentalLayoutApi::class)

package cl.vigia.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.vigia.app.data.Alert
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Trend
import cl.vigia.app.data.desde
import cl.vigia.app.data.estadoLabel
import cl.vigia.app.data.fmt
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.MonoBig
import cl.vigia.app.ui.theme.MonoSmall
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.Ok

@Composable
fun TrendChip(trend: Trend) {
    val (icon, color, txt) = when (trend.dir) {
        "up" -> Triple(Icons.Filled.ArrowUpward, Crit, "${kotlin.math.abs(trend.pct)}%")
        "down" -> Triple(Icons.Filled.ArrowDownward, Ok, "${kotlin.math.abs(trend.pct)}%")
        else -> Triple(Icons.Filled.Remove, InkFaint, "estable")
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Text(txt, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun SensorCard(tipo: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val d = Repo.domain(tipo)!!
    val metric = Repo.metric(tipo, d.principal)
    val value = Repo.current(tipo, d.principal)
    val status = Repo.domainStatus(tipo)
    val trend = Repo.trendOf(tipo, d.principal)
    val serie = Repo.series(tipo, d.principal, "24h")

    Column(
        modifier = modifier.cardSurface().clickable { onClick() }.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(domainIcon(tipo), d.soft, d.color)
            Column {
                Text(d.nombre, style = MaterialTheme.typography.titleLarge)
                Text(d.sub, style = MaterialTheme.typography.bodySmall, color = InkFaint)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(fmt(value, metric.dec), style = MonoBig.copy(fontSize = 28.sp))
                    Spacer(Modifier.width(4.dp))
                    Text(metric.unit, style = MonoSmall, color = InkFaint)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrendChip(trend)
                    Text("${metric.label} · 24 h", style = MaterialTheme.typography.bodySmall, color = InkFaint)
                }
            }
            Sparkline(serie, d.color, width = 100.dp, height = 40.dp)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(status, full = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ver detalle", style = MaterialTheme.typography.labelLarge, color = Moss)
                Icon(Icons.Filled.KeyboardArrowRight, null, tint = Moss, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = InkFaint, modifier = Modifier.size(14.dp))
        Text(text, style = MonoSmall, color = InkFaint)
    }
}

@Composable
fun AlertRow(alert: Alert, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val d = Repo.domain(alert.tipo)!!
    val resuelta = alert.estado == "resuelta"
    Row(
        modifier = modifier
            .alpha(if (resuelta) 0.72f else 1f)
            .cardSurface(RoundedCornerShape(16.dp))
            .clickable { onOpen() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        IconBadge(
            Icons.Outlined.Warning,
            statusSoft(alert.nivel),
            statusColor(alert.nivel),
            boxSize = 38.dp,
            corner = 11.dp,
            iconSize = 20.dp,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Tag(d.nombre, d.soft, d.color)
                Tag(estadoLabel[alert.estado]!!)
            }
            Text(alert.titulo, style = MaterialTheme.typography.titleMedium)
            Text(alert.detalle, style = MaterialTheme.typography.bodyMedium, color = InkFaint)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaItem(Icons.Outlined.Place, Repo.stationName(alert.estacion))
                MetaItem(Icons.Outlined.Tune, "${alert.metrica}: ${alert.valor}")
                MetaItem(Icons.Outlined.Schedule, desde(alert.ts))
            }
        }
    }
}
