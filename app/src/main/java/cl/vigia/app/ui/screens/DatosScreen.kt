@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package cl.vigia.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Dataset
import cl.vigia.app.data.Repo
import cl.vigia.app.data.fmt
import cl.vigia.app.data.shareReport
import cl.vigia.app.ui.components.ChipToggle
import cl.vigia.app.ui.components.EmptyState
import cl.vigia.app.ui.components.SectionCard
import cl.vigia.app.ui.components.ScreenHeader
import cl.vigia.app.ui.components.SegmentedControl
import cl.vigia.app.ui.components.Tag
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.LineStrong
import cl.vigia.app.ui.theme.MonoSmall
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.SurfaceCard

private val FORMATO_OPC = listOf("todos" to "Todos", "CSV" to "CSV", "JSON" to "JSON")

private fun slug(s: String): String =
    s.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

@Composable
fun DatosScreen(toast: (String) -> Unit) {
    val ctx = LocalContext.current
    var q by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("todos") }
    var estacion by remember { mutableStateOf("todas") }
    var periodo by remember { mutableStateOf("todos") }
    var formato by remember { mutableStateOf("todos") }

    val lista = Repo.datasets.filter { d ->
        (tipo == "todos" || d.tipo == tipo) &&
            (estacion == "todas" || d.estacion == estacion) &&
            (periodo == "todos" || d.periodo == periodo) &&
            (formato == "todos" || d.formato == formato) &&
            (q.isBlank() || d.titulo.contains(q, true) || Repo.domain(d.tipo)!!.nombre.contains(q, true))
    }

    fun descargar(d: Dataset) {
        val esJSON = d.formato == "JSON"
        val text = if (esJSON) Repo.buildJSON(d.tipo) else Repo.buildCSV(d.tipo)
        val ext = if (esJSON) "json" else "csv"
        val mime = if (esJSON) "application/json" else "text/csv"
        shareReport(ctx, "vigia_${d.tipo}_${slug(d.periodo)}.$ext", text, mime)
        toast("Informe generado: ${d.titulo}")
    }

    val estacionOpc = listOf("todas" to "Todas") + Repo.stations.map { it.id to it.nombre }
    val periodoOpc = listOf("todos" to "Cualquiera") + Repo.periodos.map { it to it }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        ScreenHeader(
            eyebrow = "Transparencia ambiental",
            title = "Datos abiertos",
            subtitle = "Descarga los registros de monitoreo en formato abierto (CSV o JSON) para analizarlos, compartirlos o respaldar un reclamo.",
        )
        Spacer(Modifier.height(20.dp))

        // Filtros
        SectionCard {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("Buscar conjunto") },
                placeholder = { Text("Ej. agua, ruido, PM10…") },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = InkFaint) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipToggle("Todos", tipo == "todos", { tipo = "todos" })
                Repo.domains.forEach { d ->
                    ChipToggle(d.nombre, tipo == d.tipo, { tipo = d.tipo }, leadingDot = d.color, selectedBg = d.color)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField("Estación", estacionOpc, estacion, { estacion = it }, Modifier.weight(1f))
                DropdownField("Periodo", periodoOpc, periodo, { periodo = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            SegmentedControl(FORMATO_OPC, formato, { formato = it }, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "${lista.size} ${if (lista.size == 1) "conjunto disponible" else "conjuntos disponibles"}",
            style = MaterialTheme.typography.bodySmall, color = InkFaint,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 6.dp),
        )
        Spacer(Modifier.height(10.dp))

        if (lista.isEmpty()) {
            EmptyState(Icons.Outlined.Search, "No encontramos conjuntos con esos filtros.")
        } else {
            lista.forEach { d ->
                DatasetCard(d, onDownload = { descargar(d) })
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Los archivos se generan en tu dispositivo a partir de los datos de la demostración.",
            style = MaterialTheme.typography.bodySmall, color = InkFaint,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DatasetCard(d: Dataset, onDownload: () -> Unit) {
    val dom = Repo.domain(d.tipo)!!
    SectionCard(padding = 16.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(d.titulo, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Tag(dom.nombre, dom.soft, dom.color)
            }
            Tag(d.formato)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetaCol("Estación", Repo.stationName(d.estacion))
            MetaCol("Periodo", d.periodo)
            MetaCol("Registros", d.filas?.let { fmt(it.toDouble(), 0) } ?: "—")
        }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(
            onClick = onDownload,
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Icon(Icons.Outlined.FileDownload, null, tint = Moss, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Descargar ${d.formato}", color = Moss)
        }
    }
}

@Composable
private fun MetaCol(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = cl.vigia.app.ui.theme.MonoLabel, color = InkFaint)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    }
}

@Composable
private fun DropdownField(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (v, l) ->
                DropdownMenuItem(text = { Text(l) }, onClick = { onSelect(v); expanded = false })
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Moss,
    unfocusedBorderColor = LineStrong,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
)
