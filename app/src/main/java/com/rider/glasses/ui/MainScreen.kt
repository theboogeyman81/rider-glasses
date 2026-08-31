package com.rider.glasses.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rider.glasses.glasses.GlassesConnectionState

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val glassesState by viewModel.glassesState.collectAsStateWithLifecycle()

    val dotColor = if (glassesState is GlassesConnectionState.Connected) Color(0xFF4CAF50) else Color.Gray

    val statusLabel = when (val s = glassesState) {
        is GlassesConnectionState.Connected    -> "Connected · ${s.batteryPct}%"
        is GlassesConnectionState.Scanning     -> "Scanning…"
        is GlassesConnectionState.Disconnected -> "Disconnected"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(Modifier.height(8.dp))
        Text(statusLabel, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = viewModel::onTap) { Text("Simulate TAP") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = viewModel::onHold) { Text("Simulate HOLD") }
    }
}
