package cl.vigia.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.vigia.app.R
import cl.vigia.app.data.Repo
import cl.vigia.app.ui.components.Eyebrow
import cl.vigia.app.ui.components.TerrainHero
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.Loam
import cl.vigia.app.ui.theme.Loam2
import cl.vigia.app.ui.theme.LineStrong
import cl.vigia.app.ui.theme.MonoLabel
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.OnLoamSoft
import cl.vigia.app.ui.theme.SurfaceCard
import cl.vigia.app.ui.theme.MonoValue

@Composable
fun LoginScreen(onEnter: () -> Unit) {
    var correo by remember { mutableStateOf("maria.soto@vecinos.example") }
    var clave by remember { mutableStateOf("demo1234") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hero: corte de terreno
        Box(Modifier.fillMaxWidth().height(310.dp).background(Loam)) {
            TerrainHero(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Loam2)) {
                        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
                    }
                    Column {
                        Text("Vigía", style = MaterialTheme.typography.headlineMedium, color = SurfaceCard)
                        Text("MONITOREO AMBIENTAL COMUNITARIO", style = cl.vigia.app.ui.theme.MonoLabel, color = OnLoamSoft)
                    }
                }
                Column {
                    Text(
                        "El monitoreo ambiental, en manos de la comunidad.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SurfaceCard,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                        Kpi("${Repo.zones.size}", "zonas")
                        Kpi("${Repo.zones.sumOf { it.stations.size }}", "estaciones")
                        Kpi("24/7", "mediciones")
                    }
                }
            }
        }

        // Formulario
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp)) {
            Eyebrow("Acceso a la comunidad")
            Spacer(Modifier.height(8.dp))
            Text("Bienvenido de vuelta", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                "Revisa la calidad del agua, el aire, la tierra y el ruido cerca de la faena minera de tu sector.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            Spacer(Modifier.height(24.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Moss,
                unfocusedBorderColor = LineStrong,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Outlined.MailOutline, null, tint = InkFaint) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = InkFaint) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onEnter,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Moss, contentColor = SurfaceCard),
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.Outlined.Info, null, tint = InkFaint, modifier = Modifier.size(15.dp))
                Text(
                    "Demostración solo de frontend: entra sin necesidad de escribir datos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                )
            }
        }
    }
}

@Composable
private fun Kpi(value: String, label: String) {
    Column {
        Text(value, style = MonoValue, color = SurfaceCard)
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnLoamSoft)
    }
}
