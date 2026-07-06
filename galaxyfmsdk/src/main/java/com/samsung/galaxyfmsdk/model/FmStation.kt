package com.samsung.galaxyfmsdk.model

data class FmStation(
    val frequencyMhz: Float,
    val stationName: String = "",
    val radioText: String = "",
    val rssiDb: Int = -70,
    val isStereo: Boolean = true,
    val programType: String = "Pop Music",
    val isFavorite: Boolean = false
) {
    val formattedFrequency: String
        get() = String.format("%.1f MHz", frequencyMhz)
}
