package cl.vigia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Zone
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.SurfaceCard

/** Carrusel horizontal para elegir la zona minera activa. */
@Composable
fun ZoneSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Eyebrow("Zona minera", Modifier.padding(start = 2.dp, bottom = 10.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Repo.zones.forEach { z ->
                ZoneChip(z, selected == z.id) { onSelect(z.id) }
            }
        }
    }
}

@Composable
private fun ZoneChip(zone: Zone, selected: Boolean, onClick: () -> Unit) {
    val status = Repo.zoneStatus(zone.id)
    Column(
        Modifier
            .width(176.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MossSoft else SurfaceCard)
            .border(1.5.dp, if (selected) Moss else LineCol, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Moss else MossSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Landscape, null,
                    tint = if (selected) SurfaceCard else Moss,
                    modifier = Modifier.size(19.dp),
                )
            }
            Box(Modifier.size(10.dp).clip(CircleShape).background(statusColor(status)))
        }
        Column {
            Text(
                zone.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                zone.faena,
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        StatusPill(status, full = true)
    }
}
