package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samsung.galaxyfmsdk.model.FmRdsData
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyNeonGreen
import com.example.ui.theme.GalaxyWarningAmber

@Composable
fun FmSignalMeter(
    isPowerOn: Boolean,
    rssiDb: Int,
    isStereo: Boolean,
    rdsData: FmRdsData,
    modifier: Modifier = Modifier
) {
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
            // Top Row: Signal Level & Stereo Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Signal Quality",
                        tint = if (isPowerOn && rssiDb > -75) GalaxyNeonGreen else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUALCOMM V4L2 RSSI METER",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = if (!isPowerOn) Color.DarkGray else if (isStereo) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (!isPowerOn) "OFF" else if (isStereo) "STEREO FM" else "MONO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // LED Segment Signal Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val normalizedLevel = if (!isPowerOn) 0 else ((rssiDb + 115).coerceIn(0, 75)) / 5 // 0..15 bars
                for (i in 1..15) {
                    val active = i <= normalizedLevel
                    val barColor = when {
                        !active -> MaterialTheme.colorScheme.surfaceVariant
                        i <= 8 -> GalaxyCyan
                        i <= 12 -> GalaxyNeonGreen
                        else -> GalaxyWarningAmber
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-120 dBm (Weak)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(if (isPowerOn) "$rssiDb dBm" else "--- dBm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GalaxyCyan)
                Text("-40 dBm (Max)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // RDS Telemetry Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radio, contentDescription = "RDS Data", tint = GalaxyCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RBDS / RDS TELEMETRY DECODER", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Program Service (PS) Name
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PROGRAM SERVICE (PS)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPowerOn) rdsData.psName else "---",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Program Type (PTY)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PROGRAM TYPE (PTY)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPowerOn) rdsData.ptyDescription else "---",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GalaxyCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Radio Text Banner
            Surface(
                color = Color(0xFF0F141F),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("RADIO TEXT (RT)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPowerOn) rdsData.radioText else "FM Tuner Power Off",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
