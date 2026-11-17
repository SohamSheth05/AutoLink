package com.autolink.feature.vehicle.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autolink.core.designsystem.AutoColors
import com.autolink.core.designsystem.Dimens
import com.autolink.core.domain.vehicle.SignalQuality
import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.core.domain.vehicle.VehicleStatus
import com.autolink.core.ui.asSecondsAgo
import com.autolink.core.ui.oneDecimal
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(viewModel: DashboardViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun DashboardScreen(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    val status = state.status
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Car displays vary a lot (this emulator is 1080x600); never clip the developer panel.
            .verticalScroll(rememberScrollState())
            .padding(Dimens.gutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.gutter),
    ) {
        Header(status)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gutter)) {
            val faded = status.quality != SignalQuality.LIVE
            SignalCard("Speed", status.speedKph?.let { it.toInt().toString() }, "km/h", faded)
            SignalCard("Battery", status.batteryPercent?.toString(), "%", faded)
            SignalCard("Cabin", status.cabinTempC?.oneDecimal(), "°C", faded)
        }
        if (state.showDeveloperPanel) DeveloperPanel(state, onAction)
    }
}

@Composable
private fun Header(status: VehicleStatus) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AutoLink", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Pill(
            text = if (status.source == SignalSource.SIMULATED) "SIMULATED" else "CAR API",
            color = AutoColors.Simulated,
        )
        QualityPill(status.quality)
        Spacer(Modifier.weight(1f))
        Text(
            text = status.ageMs?.let { "Last update ${it.asSecondsAgo()} ago" } ?: "Waiting for first signal…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QualityPill(quality: SignalQuality) {
    val (label, color) = when (quality) {
        SignalQuality.LIVE -> "LIVE" to AutoColors.Live
        SignalQuality.STALE -> "STALE" to AutoColors.Stale
        SignalQuality.UNAVAILABLE -> "UNAVAILABLE" to AutoColors.Unavailable
    }
    Pill(label, color, dot = true)
}

@Composable
private fun Pill(text: String, color: Color, dot: Boolean = false) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (dot) Box(Modifier.size(12.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun RowScope.SignalCard(label: String, value: String?, unit: String, faded: Boolean) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    // Unknown is "--", never 0.
                    text = value ?: "--",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (faded) 0.4f else 1f),
                )
                Text(
                    " $unit",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun DeveloperPanel(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    val d = state.status.diagnostics
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Developer · fault injection", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onAction(DashboardAction.OnTogglePause) },
                    modifier = Modifier.heightIn(min = Dimens.touchTarget),
                ) { Text(if (state.isSimulatorPaused) "Resume signals" else "Pause signals") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OnInjectDuplicate) },
                    modifier = Modifier.heightIn(min = Dimens.touchTarget),
                ) { Text("Send duplicate") }
                OutlinedButton(
                    onClick = { onAction(DashboardAction.OnInjectOutOfOrder) },
                    modifier = Modifier.heightIn(min = Dimens.touchTarget),
                ) { Text("Send out-of-order") }
            }
            Text(
                "Accepted ${d.accepted} · duplicates dropped ${d.discardedDuplicate} · out-of-order dropped ${d.discardedOutOfOrder}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val q = state.queue
            Text(
                // Survives app restarts: these counts come from the Room database, not memory.
                "Stored events ${q.total} · pending upload ${q.pending} · acknowledged ${q.acknowledged}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
