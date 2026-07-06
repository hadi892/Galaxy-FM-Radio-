package com.samsung.galaxyfmsdk.model

data class FmConfig(
    val band: FmBand = FmBand.US_EUROPE,
    val stepSizeMhz: Float = 0.1f,
    val deEmphasis50us: Boolean = false, // false = 75us (US), true = 50us (EU/Asia)
    val audioOutputWiredHeadset: Boolean = true
)
