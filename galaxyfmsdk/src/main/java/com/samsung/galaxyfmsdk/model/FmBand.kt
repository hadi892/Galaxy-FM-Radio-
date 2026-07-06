package com.samsung.galaxyfmsdk.model

enum class FmBand(val minFreq: Float, val maxFreq: Float, val defaultStep: Float) {
    US_EUROPE(87.5f, 108.0f, 0.1f),
    JAPAN(76.0f, 95.0f, 0.1f),
    ITALY_THAILAND(87.5f, 108.0f, 0.05f)
}
