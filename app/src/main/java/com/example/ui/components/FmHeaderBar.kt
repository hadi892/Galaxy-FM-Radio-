package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyNeonGreen

@Composable
fun FmHeaderBar(
    isPowerOn: Boolean,
    audioOnHeadphones: Boolean,
    onPowerToggle: () -> Unit,
    onAudioToggle: () -> Unit,
    onShowHardwareInfo: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPowerOn) GalaxyNeonGreen else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GALAXY FM STUDIO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Samsung Tab A9+ 5G • Snapdragon 695 V4L2",
                    style = MaterialTheme.typography.labelMedium,
                    color = GalaxyCyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Audio Routing Button
                OutlinedButton(
                    onClick = onAudioToggle,
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("audio_route_button")
                ) {
                    Icon(
                        imageVector = if (audioOnHeadphones) Icons.Default.Headset else Icons.Default.Speaker,
                        contentDescription = "Audio Routing",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (audioOnHeadphones) "Wired ANT" else "Speaker",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Hardware Info Button
                IconButton(
                    onClick = onShowHardwareInfo,
                    modifier = Modifier.testTag("hardware_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Qualcomm Hardware Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Main Power Toggle Button
                Button(
                    onClick = onPowerToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPowerOn) Color(0xFFD32F2F) else GalaxyNeonGreen
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("power_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isPowerOn) "Power Off" else "Power On",
                        tint = if (isPowerOn) Color.White else Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
