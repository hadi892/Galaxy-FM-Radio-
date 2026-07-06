package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FmPresetEntity
import com.samsung.galaxyfmsdk.model.FmStation
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue

@Composable
fun FmPresetsPanel(
    isPowerOn: Boolean,
    presets: List<FmPresetEntity>,
    scannedStations: List<FmStation>,
    isScanning: Boolean,
    onTunePreset: (Float) -> Unit,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Presets, 1 = Scan Results
    var showDirectTuneDialog by remember { mutableStateOf(false) }
    var inputFreqText by remember { mutableStateOf("98.5") }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = GalaxyCyan,
                    modifier = Modifier.weight(1f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("FAVORITES (${presets.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isScanning) "SCANNING..." else "SCAN RESULTS (${scannedStations.size})") }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showDirectTuneDialog = true },
                    modifier = Modifier.testTag("direct_tune_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Direct Tune", tint = GalaxyCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                if (presets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("No favorite stations saved. Click the ❤️ on the dial to add!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(presets, key = { it.frequencyMhz }) { preset ->
                            PresetItemCard(
                                title = preset.stationName,
                                subtitle = "${preset.frequencyMhz} MHz • ${preset.programType}",
                                description = preset.radioText,
                                onClick = { if (isPowerOn) onTunePreset(preset.frequencyMhz) }
                            )
                        }
                    }
                }
            } else {
                // Scan Results Tab
                Column {
                    Button(
                        onClick = onStartScan,
                        enabled = isPowerOn && !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = GalaxyElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hardware_scan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SCANNING QUALCOMM V4L2 SPECTRUM...")
                        } else {
                            Icon(Icons.Default.Radar, contentDescription = "Hardware Scan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("START FULL HARDWARE SCAN")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (scannedStations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text("Click 'START FULL HARDWARE SCAN' to seek active FM stations across the band.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(scannedStations, key = { it.frequencyMhz }) { st ->
                                PresetItemCard(
                                    title = if (st.stationName.isNotBlank()) st.stationName else "FM ${st.frequencyMhz}",
                                    subtitle = "${st.frequencyMhz} MHz • ${st.rssiDb} dBm (${if (st.isStereo) "Stereo" else "Mono"})",
                                    description = st.radioText,
                                    onClick = { if (isPowerOn) onTunePreset(st.frequencyMhz) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDirectTuneDialog) {
        AlertDialog(
            onDismissRequest = { showDirectTuneDialog = false },
            title = { Text("Direct Frequency Tuning") },
            text = {
                Column {
                    Text("Enter FM station frequency (MHz):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputFreqText,
                        onValueChange = { inputFreqText = it },
                        label = { Text("Frequency (87.5 - 108.0)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    inputFreqText.toFloatOrNull()?.let { f ->
                        onTunePreset(f)
                    }
                    showDirectTuneDialog = false
                }) {
                    Text("TUNE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectTuneDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun PresetItemCard(
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = GalaxyCyan)
                if (description.isNotBlank()) {
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Tune Station", tint = GalaxyCyan, modifier = Modifier.size(24.dp))
        }
    }
}
