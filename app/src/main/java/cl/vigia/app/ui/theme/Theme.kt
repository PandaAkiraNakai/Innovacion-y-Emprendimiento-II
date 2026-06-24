package cl.vigia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VigiaColors = lightColorScheme(
    primary = Moss,
    onPrimary = SurfaceCard,
    primaryContainer = MossSoft,
    onPrimaryContainer = MossDeep,
    secondary = Tierra,
    onSecondary = SurfaceCard,
    background = Paper,
    onBackground = Ink,
    surface = SurfaceCard,
    onSurface = Ink,
    surfaceVariant = Paper2,
    onSurfaceVariant = InkSoft,
    surfaceContainer = SurfaceCard,
    surfaceContainerHigh = SurfaceCard2,
    outline = LineStrong,
    outlineVariant = LineCol,
    error = Crit,
    onError = SurfaceCard,
    errorContainer = CritSoft,
    onErrorContainer = Crit,
    scrim = Loam,
)

val VigiaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun VigiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VigiaColors,
        typography = VigiaTypography,
        shapes = VigiaShapes,
        content = content,
    )
}
