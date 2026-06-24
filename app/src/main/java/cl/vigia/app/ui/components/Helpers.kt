package cl.vigia.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cl.vigia.app.data.Status
import cl.vigia.app.ui.theme.Crit
import cl.vigia.app.ui.theme.CritSoft
import cl.vigia.app.ui.theme.Ok
import cl.vigia.app.ui.theme.OkSoft
import cl.vigia.app.ui.theme.Warn
import cl.vigia.app.ui.theme.WarnSoft

fun domainIcon(tipo: String): ImageVector = when (tipo) {
    "agua" -> Icons.Outlined.WaterDrop
    "aire" -> Icons.Outlined.Air
    "tierra" -> Icons.Outlined.Terrain
    else -> Icons.Outlined.GraphicEq
}

fun statusColor(s: Status): Color = when (s) {
    Status.OK -> Ok
    Status.WARN -> Warn
    Status.CRIT -> Crit
}

fun statusSoft(s: Status): Color = when (s) {
    Status.OK -> OkSoft
    Status.WARN -> WarnSoft
    Status.CRIT -> CritSoft
}

fun statusInk(s: Status): Color = when (s) {
    Status.OK -> Color(0xFF3C6038)
    Status.WARN -> Color(0xFF8C5A14)
    Status.CRIT -> Color(0xFF8A332A)
}
