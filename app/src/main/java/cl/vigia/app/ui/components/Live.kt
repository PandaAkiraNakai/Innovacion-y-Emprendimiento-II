package cl.vigia.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.LiveSim
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.MonoLabel
import cl.vigia.app.ui.theme.Ok
import cl.vigia.app.ui.theme.OkSoft
import kotlinx.coroutines.delay

/**
 * Indicador "EN VIVO" con punto pulsante y cuenta regresiva a la próxima lectura.
 * Tiene su propio tick de 1 s, aislado, para no recomponer la pantalla entera.
 */
@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val secs = LiveSim.secondsToNextReading(now)
    val mm = secs / 60
    val ss = (secs % 60).toString().padStart(2, '0')

    val transition = rememberInfiniteTransition(label = "live")
    val pulse by transition.animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )

    Row(
        modifier
            .clip(CircleShape)
            .background(OkSoft)
            .padding(start = 9.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).alpha(pulse).background(Ok))
        Text("EN VIVO", style = MonoLabel, color = Ok)
        Text("· próxima lectura en $mm:$ss", style = MaterialTheme.typography.bodySmall, color = InkFaint)
    }
}
