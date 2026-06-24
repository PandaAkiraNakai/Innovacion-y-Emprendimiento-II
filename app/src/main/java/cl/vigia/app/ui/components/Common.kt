package cl.vigia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.vigia.app.data.Metric
import cl.vigia.app.data.Repo
import cl.vigia.app.data.Status
import cl.vigia.app.data.fmt
import cl.vigia.app.data.statusLabelFull
import cl.vigia.app.data.statusLabelShort
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.InkSoft
import cl.vigia.app.ui.theme.LineCol
import cl.vigia.app.ui.theme.LineStrong
import cl.vigia.app.ui.theme.MonoLabel
import cl.vigia.app.ui.theme.MonoSmall
import cl.vigia.app.ui.theme.MonoValue
import cl.vigia.app.ui.theme.Paper
import cl.vigia.app.ui.theme.Paper2
import cl.vigia.app.ui.theme.SurfaceCard
import cl.vigia.app.ui.theme.SurfaceCard2

private val CardShape = RoundedCornerShape(22.dp)

fun Modifier.cardSurface(shape: RoundedCornerShape = CardShape): Modifier =
    this.clip(shape).background(SurfaceCard).border(1.dp, LineCol, shape)

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.cardSurface().padding(padding), content = content)
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MonoLabel, color = InkFaint, modifier = modifier)
}

@Composable
fun StatusPill(status: Status, full: Boolean = false, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(statusSoft(status))
            .padding(start = 9.dp, end = 11.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(statusColor(status)))
        Text(
            text = if (full) statusLabelFull[status]!! else statusLabelShort[status]!!,
            style = MaterialTheme.typography.labelMedium,
            color = statusInk(status),
        )
    }
}

@Composable
fun Tag(text: String, bg: Color = Paper2, fg: Color = InkSoft, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MonoLabel.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified),
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun IconBadge(
    icon: ImageVector,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    boxSize: Dp = 42.dp,
    corner: Dp = 12.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier.size(boxSize).clip(RoundedCornerShape(corner)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = fg, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            if (eyebrow != null) {
                Eyebrow(eyebrow)
                Spacer(Modifier.height(4.dp))
            }
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            }
        }
        if (action != null) {
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}

@Composable
fun KpiCard(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.cardSurface(RoundedCornerShape(16.dp)).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) Icon(icon, null, tint = InkSoft, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun MetricTile(tipo: String, metric: Metric, value: Double, modifier: Modifier = Modifier) {
    val status = Repo.statusOf(value, metric)
    val ratio = Repo.severityRatio(value, metric)
    val fill = (ratio * 0.8).coerceIn(0.06, 1.0).toFloat()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard2)
            .border(1.dp, LineCol, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(metric.label, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            StatusPill(status)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(fmt(value, metric.dec), style = MonoValue)
            if (metric.unit.isNotEmpty()) {
                Spacer(Modifier.width(3.dp))
                Text(metric.unit, style = MonoSmall, color = InkFaint)
            }
        }
        Box(
            Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Paper2),
        ) {
            Box(Modifier.fillMaxWidth(fill).height(7.dp).clip(CircleShape).background(statusColor(status)))
        }
        Text(
            text = "${if (metric.lowerIsWorse) "mín." else "límite"} ${fmt(metric.limite, metric.dec)} ${metric.unit}".trim(),
            style = MonoSmall,
            color = InkFaint,
        )
    }
}

@Composable
fun SegmentedControl(
    options: List<Pair<String, String>>, // value to label
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clip(CircleShape).background(Paper2).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (on) SurfaceCard2 else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (on) cl.vigia.app.ui.theme.Ink else InkSoft)
            }
        }
    }
}

@Composable
fun ChipToggle(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDot: Color? = null,
    selectedBg: Color = cl.vigia.app.ui.theme.Ink,
    selectedFg: Color = Paper,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) selectedBg else SurfaceCard)
            .border(1.dp, if (selected) selectedBg else LineStrong, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (leadingDot != null) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (selected) Paper else leadingDot))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) selectedFg else InkSoft,
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceCard)
            .border(1.dp, LineStrong, CardShape)
            .padding(vertical = 44.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = InkFaint, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSoft, textAlign = TextAlign.Center)
    }
}
