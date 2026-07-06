package com.samsung.galaxyfmsdk

import android.util.Log
import com.samsung.galaxyfmsdk.jni.GalaxyFmJniBridge
import com.samsung.galaxyfmsdk.model.FmBand
import com.samsung.galaxyfmsdk.model.FmConfig
import com.samsung.galaxyfmsdk.model.FmRdsData
import com.samsung.galaxyfmsdk.model.FmStation
import com.samsung.galaxyfmsdk.model.QualcommFmHardwareInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class GalaxyFmManager {
    private val jniBridge = GalaxyFmJniBridge()
    private val sdkScope = CoroutineScope(Dispatchers.Default)
    private var telemetryJob: Job? = null
    private var scanJob: Job? = null

    private val _isPowerOn = MutableStateFlow(false)
    val isPowerOn: StateFlow<Boolean> = _isPowerOn.asStateFlow()

    private val _currentFrequency = MutableStateFlow(98.5f)
    val currentFrequency: StateFlow<Float> = _currentFrequency.asStateFlow()

    private val _rssiDb = MutableStateFlow(-120)
    val rssiDb: StateFlow<Int> = _rssiDb.asStateFlow()

    private val _isStereo = MutableStateFlow(false)
    val isStereo: StateFlow<Boolean> = _isStereo.asStateFlow()

    private val _rdsData = MutableStateFlow(FmRdsData("", "", 0, 0))
    val rdsData: StateFlow<FmRdsData> = _rdsData.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _audioOnHeadphones = MutableStateFlow(true)
    val audioOnHeadphones: StateFlow<Boolean> = _audioOnHeadphones.asStateFlow()

    private var currentBand = FmBand.US_EUROPE

    companion object {
        private const val TAG = "GalaxyFmManager"
        @Volatile
        private var INSTANCE: GalaxyFmManager? = null

        fun getInstance(): GalaxyFmManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GalaxyFmManager().also { INSTANCE = it }
            }
        }
    }

    init {
        jniBridge.openDevice("/dev/radio0")
    }

    fun getHardwareInfo(): QualcommFmHardwareInfo {
        return QualcommFmHardwareInfo()
    }

    fun powerOn(initialFreq: Float = 98.5f) {
        if (_isPowerOn.value) return
        Log.i(TAG, "Powering up Galaxy FM SDK on Snapdragon 695 at $initialFreq MHz")
        val success = jniBridge.powerUp(initialFreq)
        _isPowerOn.value = true
        _currentFrequency.value = initialFreq
        _isMuted.value = false
        startTelemetryLoop()
    }

    fun powerOff() {
        if (!_isPowerOn.value) return
        Log.i(TAG, "Powering down Galaxy FM SDK")
        stopTelemetryLoop()
        scanJob?.cancel()
        _isScanning.value = false
        jniBridge.powerDown()
        _isPowerOn.value = false
        _rssiDb.value = -120
        _rdsData.value = FmRdsData("OFF", "Power Off", 0, 0)
    }

    fun tune(freqMhz: Float) {
        if (!_isPowerOn.value) powerOn(freqMhz)
        val clampedFreq = clampFrequency(freqMhz)
        Log.i(TAG, "Tuning to $clampedFreq MHz")
        jniBridge.setFrequency(clampedFreq)
        _currentFrequency.value = clampedFreq
        updateTelemetryImmediate()
    }

    fun stepUp(stepMhz: Float = 0.1f) {
        val next = clampFrequency(_currentFrequency.value + stepMhz)
        tune(next)
    }

    fun stepDown(stepMhz: Float = 0.1f) {
        val next = clampFrequency(_currentFrequency.value - stepMhz)
        tune(next)
    }

    fun seekUp() {
        if (!_isPowerOn.value) return
        sdkScope.launch {
            _isScanning.value = true
            val seekResult = jniBridge.seek(true, true)
            if (seekResult) {
                delay(150) // Hardware PLL settling delay
                val newFreq = jniBridge.getFrequency()
                _currentFrequency.value = clampFrequency(newFreq)
            }
            _isScanning.value = false
            updateTelemetryImmediate()
        }
    }

    fun seekDown() {
        if (!_isPowerOn.value) return
        sdkScope.launch {
            _isScanning.value = true
            val seekResult = jniBridge.seek(false, true)
            if (seekResult) {
                delay(150)
                val newFreq = jniBridge.getFrequency()
                _currentFrequency.value = clampFrequency(newFreq)
            }
            _isScanning.value = false
            updateTelemetryImmediate()
        }
    }

    fun scanAllStations(onStationFound: (FmStation) -> Unit, onScanComplete: (List<FmStation>) -> Unit) {
        if (!_isPowerOn.value) powerOn(87.5f)
        scanJob?.cancel()
        scanJob = sdkScope.launch {
            _isScanning.value = true
            val foundStations = mutableListOf<FmStation>()
            var f = currentBand.minFreq
            while (f <= currentBand.maxFreq && _isScanning.value) {
                f = clampFrequency(f)
                _currentFrequency.value = f
                jniBridge.setFrequency(f)
                delay(90) // Allow physical RF tuner circuitry and AGC to settle
                val rssi = jniBridge.getRssi()
                if (rssi >= -85) { // Physical signal lock threshold
                    delay(180) // Accumulate RDS packets
                    val rds = jniBridge.getRdsData()
                    val ps = rds.psName.ifEmpty { "FM ${String.format("%.1f", f)}" }
                    val st = FmStation(
                        frequencyMhz = f,
                        stationName = ps,
                        radioText = rds.radioText,
                        rssiDb = rssi,
                        isStereo = jniBridge.isStereo(),
                        programType = rds.ptyDescription
                    )
                    foundStations.add(st)
                    onStationFound(st)
                }
                f += 0.2f
            }
            _isScanning.value = false
            if (foundStations.isNotEmpty()) {
                tune(foundStations.first().frequencyMhz)
            }
            onScanComplete(foundStations)
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        _isScanning.value = false
    }

    fun setMute(mute: Boolean) {
        _isMuted.value = mute
        jniBridge.setMute(mute)
    }

    fun toggleAudioRouting() {
        _audioOnHeadphones.value = !_audioOnHeadphones.value
    }

    fun setBand(band: FmBand) {
        currentBand = band
        jniBridge.setBand(if (band == FmBand.JAPAN) 1 else 0)
        if (_currentFrequency.value < band.minFreq || _currentFrequency.value > band.maxFreq) {
            tune(band.minFreq)
        }
    }

    private fun startTelemetryLoop() {
        stopTelemetryLoop()
        telemetryJob = sdkScope.launch {
            while (_isPowerOn.value) {
                if (!_isScanning.value) {
                    _rssiDb.value = jniBridge.getRssi()
                    _isStereo.value = jniBridge.isStereo()
                    _rdsData.value = jniBridge.getRdsData()
                }
                delay(500)
            }
        }
    }

    private fun stopTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    private fun updateTelemetryImmediate() {
        if (!_isPowerOn.value) return
        _rssiDb.value = jniBridge.getRssi()
        _isStereo.value = jniBridge.isStereo()
        _rdsData.value = jniBridge.getRdsData()
    }

    private fun clampFrequency(freq: Float): Float {
        val min = currentBand.minFreq
        val max = currentBand.maxFreq
        var clamped = freq
        if (clamped < min) clamped = max
        if (clamped > max) clamped = min
        return ((clamped * 10).roundToInt() / 10.0).toFloat()
    }
}
