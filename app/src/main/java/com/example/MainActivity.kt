package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.FmRadioViewModel
import com.example.ui.components.FmHeaderBar
import com.example.ui.components.FmPresetsPanel
import com.example.ui.components.FmSignalMeter
import com.example.ui.components.FmTuningDial
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GalaxyCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: FmRadioViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          FmRadioScreen(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun FmRadioScreen(viewModel: FmRadioViewModel) {
  val isPowerOn by viewModel.isPowerOn.collectAsState()
  val currentFreq by viewModel.currentFrequency.collectAsState()
  val currentBand by viewModel.currentBand.collectAsState()
  val rssiDb by viewModel.rssiDb.collectAsState()
  val isStereo by viewModel.isStereo.collectAsState()
  val rdsData by viewModel.rdsData.collectAsState()
  val isScanning by viewModel.isScanning.collectAsState()
  val audioOnHeadphones by viewModel.audioOnHeadphones.collectAsState()
  val presets by viewModel.presets.collectAsState()
  val scannedStations by viewModel.scannedStations.collectAsState()

  var showHardwareModal by remember { mutableStateOf(false) }

  val isFavorite = presets.any { Math.abs(it.frequencyMhz - currentFreq) < 0.04f }

  Scaffold(
    containerColor = DarkBackground,
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp)
    ) {
      val isTabletWide = maxWidth >= 700.dp

      Column(modifier = Modifier.fillMaxSize()) {
        FmHeaderBar(
          isPowerOn = isPowerOn,
          audioOnHeadphones = audioOnHeadphones,
          onPowerToggle = { viewModel.togglePower() },
          onAudioToggle = { viewModel.toggleAudioRouting() },
          onShowHardwareInfo = { showHardwareModal = true }
        )

        if (isTabletWide) {
          // Canonical Tablet Layout (Side-by-Side)
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Column(
              modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              FmTuningDial(
                isPowerOn = isPowerOn,
                currentFreq = currentFreq,
                currentBand = currentBand,
                isFavorite = isFavorite,
                onTune = { viewModel.tuneTo(it) },
                onStepUp = { viewModel.stepUp() },
                onStepDown = { viewModel.stepDown() },
                onSeekUp = { viewModel.seekUp() },
                onSeekDown = { viewModel.seekDown() },
                onToggleFavorite = { viewModel.toggleFavoriteCurrentStation() },
                onSelectBand = { viewModel.setBand(it) }
              )

              FmSignalMeter(
                isPowerOn = isPowerOn,
                rssiDb = rssiDb,
                isStereo = isStereo,
                rdsData = rdsData
              )
            }

            Column(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            ) {
              FmPresetsPanel(
                isPowerOn = isPowerOn,
                presets = presets,
                scannedStations = scannedStations,
                isScanning = isScanning,
                onTunePreset = { viewModel.tuneTo(it) },
                onStartScan = { viewModel.startFullHardwareScan() },
                modifier = Modifier.fillMaxHeight()
              )
            }
          }
        } else {
          // Handheld / Compact Layout
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            FmTuningDial(
              isPowerOn = isPowerOn,
              currentFreq = currentFreq,
              currentBand = currentBand,
              isFavorite = isFavorite,
              onTune = { viewModel.tuneTo(it) },
              onStepUp = { viewModel.stepUp() },
              onStepDown = { viewModel.stepDown() },
              onSeekUp = { viewModel.seekUp() },
              onSeekDown = { viewModel.seekDown() },
              onToggleFavorite = { viewModel.toggleFavoriteCurrentStation() },
              onSelectBand = { viewModel.setBand(it) }
            )

            FmSignalMeter(
              isPowerOn = isPowerOn,
              rssiDb = rssiDb,
              isStereo = isStereo,
              rdsData = rdsData
            )

            FmPresetsPanel(
              isPowerOn = isPowerOn,
              presets = presets,
              scannedStations = scannedStations,
              isScanning = isScanning,
              onTunePreset = { viewModel.tuneTo(it) },
              onStartScan = { viewModel.startFullHardwareScan() }
            )
          }
        }
      }
    }
  }

  if (showHardwareModal) {
    val info = viewModel.hardwareInfo
    AlertDialog(
      onDismissRequest = { showHardwareModal = false },
      title = {
        Text("Qualcomm V4L2 Hardware Specifications", fontWeight = FontWeight.Bold, color = GalaxyCyan)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("• Device: ${info.deviceModel}")
          Text("• SoC Platform: ${info.socPlatform}")
          Text("• Radio HAL: ${info.fmRadioHal}")
          Text("• Linux Node: ${info.linuxV4l2Node}")
          Text("• Antenna: ${info.antennaType}")
          Text("• RDS Support: ${if (info.supportsRds) "Enabled (RBDS/RDS)" else "No"}")
          Text("• Hardware Seek: ${if (info.supportsHardwareSeek) "Supported via VIDIOC_S_HW_FREQ_SEEK" else "No"}")
        }
      },
      confirmButton = {
        Button(onClick = { showHardwareModal = false }) {
          Text("CLOSE")
        }
      }
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
