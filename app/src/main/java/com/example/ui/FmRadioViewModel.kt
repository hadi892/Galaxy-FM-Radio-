package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FmPresetDatabase
import com.example.data.FmPresetEntity
import com.samsung.galaxyfmsdk.GalaxyFmManager
import com.samsung.galaxyfmsdk.model.FmBand
import com.samsung.galaxyfmsdk.model.FmRdsData
import com.samsung.galaxyfmsdk.model.FmStation
import com.samsung.galaxyfmsdk.model.QualcommFmHardwareInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FmRadioViewModel(application: Application) : AndroidViewModel(application) {
    private val fmManager = GalaxyFmManager.getInstance()
    private val presetDao = FmPresetDatabase.getDatabase(application).presetDao()

    val isPowerOn: StateFlow<Boolean> = fmManager.isPowerOn
    val currentFrequency: StateFlow<Float> = fmManager.currentFrequency
    val rssiDb: StateFlow<Int> = fmManager.rssiDb
    val isStereo: StateFlow<Boolean> = fmManager.isStereo
    val rdsData: StateFlow<FmRdsData> = fmManager.rdsData
    val isScanning: StateFlow<Boolean> = fmManager.isScanning
    val audioOnHeadphones: StateFlow<Boolean> = fmManager.audioOnHeadphones

    private val _currentBand = MutableStateFlow(FmBand.US_EUROPE)
    val currentBand: StateFlow<FmBand> = _currentBand.asStateFlow()

    private val _scannedStations = MutableStateFlow<List<FmStation>>(emptyList())
    val scannedStations: StateFlow<List<FmStation>> = _scannedStations.asStateFlow()

    val presets: StateFlow<List<FmPresetEntity>> = presetDao.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hardwareInfo: QualcommFmHardwareInfo = fmManager.getHardwareInfo()

    init {
        // Pre-populate default favorites on first launch if empty
        viewModelScope.launch {
            presetDao.getAllPresets().collect { list ->
                if (list.isEmpty()) {
                    presetDao.insertPreset(FmPresetEntity(88.1f, "BBC R1", "Hit Music & Live Shows", "Pop Music", "Pop"))
                    presetDao.insertPreset(FmPresetEntity(94.3f, "CLASSIC", "Symphony No. 5 in C Minor", "Serious Classical", "Classical"))
                    presetDao.insertPreset(FmPresetEntity(98.5f, "GALAXY FM", "Samsung Galaxy Tab A9+ Snapdragon 695 Live", "Pop Music", "Favorite"))
                    presetDao.insertPreset(FmPresetEntity(104.7f, "NEWS 24", "Live Global Headlines & Market Watch", "News", "News"))
                }
            }
        }
    }

    fun togglePower() {
        if (isPowerOn.value) {
            fmManager.powerOff()
        } else {
            fmManager.powerOn(98.5f)
        }
    }

    fun tuneTo(freqMhz: Float) {
        fmManager.tune(freqMhz)
    }

    fun stepUp() {
        fmManager.stepUp(_currentBand.value.defaultStep)
    }

    fun stepDown() {
        fmManager.stepDown(_currentBand.value.defaultStep)
    }

    fun seekUp() {
        fmManager.seekUp()
    }

    fun seekDown() {
        fmManager.seekDown()
    }

    fun startFullHardwareScan() {
        _scannedStations.value = emptyList()
        fmManager.scanAllStations(
            onStationFound = { st ->
                _scannedStations.value = _scannedStations.value + st
            },
            onScanComplete = { list ->
                _scannedStations.value = list
            }
        )
    }

    fun toggleFavoriteCurrentStation() {
        val freq = currentFrequency.value
        val currentRds = rdsData.value
        viewModelScope.launch {
            val existing = presetDao.getPresetByFrequency(freq)
            if (existing != null) {
                presetDao.deleteByFrequency(freq)
            } else {
                val stName = if (currentRds.psName.isNotBlank() && currentRds.psName != "FM") currentRds.psName else "FM ${String.format("%.1f", freq)}"
                presetDao.insertPreset(
                    FmPresetEntity(
                        frequencyMhz = ((freq * 10).roundToInt() / 10.0).toFloat(),
                        stationName = stName,
                        radioText = currentRds.radioText,
                        programType = currentRds.ptyDescription
                    )
                )
            }
        }
    }

    fun toggleAudioRouting() {
        fmManager.toggleAudioRouting()
    }

    fun setBand(band: FmBand) {
        _currentBand.value = band
        fmManager.setBand(band)
    }
}
