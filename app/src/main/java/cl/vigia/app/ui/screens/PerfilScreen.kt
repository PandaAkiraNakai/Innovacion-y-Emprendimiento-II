@file:OptIn(ExperimentalLayoutApi::class)

package cl.vigia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Repo
import cl.vigia.app.ui.components.ChipToggle
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.IconBadge
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.StatusPill
import cl.vigia.app.ui.components.domainIcon
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.LineStrong
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.Paper2
import cl.vigia.app.ui.theme.SurfaceCard
import cl.vigia.app.ui.theme.Tierra

private val SENSIBILIDAD = listOf(
    Triple("criticas", "Solo críticas", "Avisos cuando se supera el límite normativo."),
    Triple("ambas", "Críticas y vigilancia", "También cuando una medición se acerca al límite."),
    Triple("todas", "Todas", "Incluye avisos informativos y resúmenes diarios."),
)

@Composable
fun PerfilScreen(zoneId: String, toast: (String) -> Unit) {
    val zone = Repo.zone(zoneId)
    val domToggles = remember { mutableStateMapOf("agua" to true, "aire" to true, "tierra" to true, "ruido" to false) }
    val canales = remember { mutableStateMapOf("correo" to true, "push" to true, "sms" to false) }
    var sensibilidad by remember { mutableStateOf("ambas") }

    var asunto by remember { mutableStateOf("") }
    var temaReporte by remember { mutableStateOf("aire") }
    var estacionReporte by remember(zoneId) { mutableStateOf(zone.stations.first().id) }
    var detalle by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf(true) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Tu cuenta",
            title = "Perfil",
            subtitle = "Decide qué alertas quieres recibir y envía un reporte cuando notes algo en tu sector.",
        )
        Spacer(Modifier.height(20.dp))

        // Cuenta
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(Moss), contentAlignment = Alignment.Center) {
                    Text("M", style = MaterialTheme.typography.headlineSmall, color = SurfaceCard)
                }
                Column(Modifier.weight(1f)) {
                    Text("María Soto", style = MaterialTheme.typography.titleLarge)
                    Text("Junta de Vecinos Los Almendros", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                StatusPill(cl.vigia.app.data.Status.OK)
            }
            Spacer(Modifier.height(16.dp))
            ProfileField("Correo", "maria.soto@vecinos.example")
            Spacer(Modifier.height(12.dp))
            ProfileField("Teléfono", "+56 9 1234 5678")
            Spacer(Modifier.height(12.dp))
            ProfileField("Sector", "Los Almendros")
        }
        Spacer(Modifier.height(18.dp))

        // Alertas por sensor
        SectionCard {
            CardTitle(Icons.Outlined.NotificationsActive, "Avisos", "Alertas por sensor")
            Spacer(Modifier.height(6.dp))
            Repo.domains.forEachIndexed { i, d ->
                if (i > 0) androidx.compose.material3.HorizontalDivider(color = LineCol)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    IconBadge(domainIcon(d.tipo), d.soft, d.color, boxSize = 38.dp, corner = 11.dp, iconSize = 20.dp)
                    Column(Modifier.weight(1f)) {
                        Text(d.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(d.sub, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    }
                    VigiaSwitch(domToggles[d.tipo] == true) { domToggles[d.tipo] = it }
                }
            }

            Spacer(Modifier.height(18.dp))
            CardTitle(Icons.Outlined.Send, null, "¿Cómo quieres recibirlas?")
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("correo" to "Correo", "push" to "Notificación", "sms" to "SMS").forEach { (k, label) ->
                    ChipToggle(label, canales[k] == true, { canales[k] = !(canales[k] ?: false) }, selectedBg = Moss)
                }
            }

            Spacer(Modifier.height(18.dp))
            CardTitle(Icons.Outlined.Tune, null, "Sensibilidad")
            Spacer(Modifier.height(10.dp))
            SENSIBILIDAD.forEach { (value, title, desc) ->
                val on = sensibilidad == value
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (on) MossSoft else SurfaceCard)
                        .border(1.dp, if (on) Moss else LineCol, RoundedCornerShape(16.dp))
                        .clickable { sensibilidad = value }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(20.dp).clip(CircleShape).border(2.dp, if (on) Moss else LineStrong, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) Box(Modifier.size(10.dp).clip(CircleShape).background(Moss))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(desc, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val activos = domToggles.values.count { it }
                    toast("Preferencias guardadas · $activos ${if (activos == 1) "sensor" else "sensores"} con alertas")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Moss, contentColor = SurfaceCard),
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Guardar preferencias")
            }
        }
        Spacer(Modifier.height(18.dp))

        // Enviar reporte
        SectionCard {
            CardTitle(Icons.Outlined.Send, "Participación", "Enviar un reporte", tint = Tierra)
            Spacer(Modifier.height(6.dp))
            Text(
                "Cuéntale a la Junta de Vecinos lo que observas: polvo, mal olor, agua turbia o ruido fuera de horario.",
                style = MaterialTheme.typography.bodyMedium, color = InkSoft,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = asunto,
                onValueChange = { asunto = it },
                label = { Text("Asunto") },
                placeholder = { Text("Ej. Polvo intenso en la mañana") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = perfilFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            FieldLabel("Tema")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Repo.domains.forEach { d ->
                    ChipToggle(d.nombre, temaReporte == d.tipo, { temaReporte = d.tipo }, leadingDot = d.color, selectedBg = d.color)
                }
            }
            Spacer(Modifier.height(14.dp))
            FieldLabel("Estación cercana · ${zone.nombre}")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                zone.stations.forEach { s ->
                    ChipToggle(s.nombre.removePrefix("Estación "), estacionReporte == s.id, { estacionReporte = s.id }, selectedBg = Moss)
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = detalle,
                onValueChange = { detalle = it },
                label = { Text("Descripción") },
                placeholder = { Text("Describe qué notaste, a qué hora y dónde…") },
                shape = RoundedCornerShape(10.dp),
                colors = perfilFieldColors(),
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IconBadge(Icons.Outlined.Place, Paper2, InkSoft, boxSize = 38.dp, corner = 11.dp, iconSize = 19.dp)
                Column(Modifier.weight(1f)) {
                    Text("Adjuntar mi ubicación", style = MaterialTheme.typography.titleMedium)
                    Text("Ayuda a ubicar el origen del problema.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                VigiaSwitch(ubicacion) { ubicacion = it }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (asunto.isBlank() || detalle.isBlank()) {
                        toast("Completa el asunto y la descripción del reporte")
                    } else {
                        toast("Reporte enviado a la Junta de Vecinos")
                        asunto = ""; detalle = ""; temaReporte = "aire"; estacionReporte = zone.stations.first().id; ubicacion = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Moss, contentColor = SurfaceCard),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Enviar reporte")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = perfilFieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun CardTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, eyebrow: String?, title: String, tint: Color = Moss) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Column {
            if (eyebrow != null) Eyebrow(eyebrow)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun VigiaSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SurfaceCard,
            checkedTrackColor = Moss,
            checkedBorderColor = Moss,
            uncheckedThumbColor = SurfaceCard,
            uncheckedTrackColor = LineStrong,
            uncheckedBorderColor = LineStrong,
        ),
    )
}

@Composable
private fun perfilFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Moss,
    unfocusedBorderColor = LineStrong,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
)
