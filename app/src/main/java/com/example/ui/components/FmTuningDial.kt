package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.galaxyfmsdk.model.FmBand
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.GalaxyElectricBlue

@Composable
fun FmTuningDial(
    isPowerOn: Boolean,
    currentFreq: Float,
    currentBand: FmBand,
    isFavorite: Boolean,
    onTune: (Float) -> Unit,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit,
    onSeekUp: () -> Unit,
    onSeekDown: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectBand: (FmBand) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GalaxyCyan.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Band Selection Pills
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                FmBand.values().forEach { band ->
                    val selected = (band == currentBand)
                    FilterChip(
                        selected = selected,
                        onClick = { if (isPowerOn) onSelectBand(band) },
                        label = {
                            Text(
                                text = when (band) {
                                    FmBand.US_EUROPE -> "US / Europe (87.5 - 108)"
                                    FmBand.JAPAN -> "Japan (76.0 - 95.0)"
                                    FmBand.ITALY_THAILAND -> "Fine Step 50kHz"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GalaxyElectricBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("band_chip_${band.name}")
                    )
                }
            }

            // Digital LCD Screen Frequency Readout
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D121C))
                    .border(2.dp, GalaxyCyan, RoundedCornerShape(16.dp))
                    .padding(vertical = 28.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isPowerOn) String.format("%.1f", currentFreq) else "---.-",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPowerOn) GalaxyCyan else Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MHz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPowerOn) Color.White else Color.DarkGray,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                if (isPowerOn) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .testTag("favorite_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Preset",
                            tint = if (isFavorite) Color(0xFFFF4081) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Frequency Dial Slider
            Slider(
                value = currentFreq,
                onValueChange = { valMhz ->
                    if (isPowerOn) {
                        onTune(valMhz)
                    }
                },
                valueRange = currentBand.minFreq..currentBand.maxFreq,
                enabled = isPowerOn,
                colors = SliderDefaults.colors(
                    thumbColor = GalaxyCyan,
                    activeTrackColor = GalaxyElectricBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("frequency_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek Down
                FilledTonalButton(
                    onClick = onSeekDown,
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("seek_down_button")
                ) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Seek Down")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SEEK -")
                }

                // Step Down (-0.1)
                OutlinedButton(
                    onClick = onStepDown,
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("step_down_button")
                ) {
                    Text("- 0.1")
                }

                // Step Up (+0.1)
                OutlinedButton(
                    onClick = onStepUp,
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("step_up_button")
                ) {
                    Text("+ 0.1")
                }

                // Seek Up
                FilledTonalButton(
                    onClick = onSeekUp,
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("seek_up_button")
                ) {
                    Text("SEEK +")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.FastForward, contentDescription = "Seek Up")
                }
            }
        }
    }
}
