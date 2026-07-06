package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fm_presets")
data class FmPresetEntity(
    @PrimaryKey
    val frequencyMhz: Float,
    val stationName: String,
    val radioText: String,
    val programType: String,
    val categoryTag: String = "Favorite",
    val timestampAdded: Long = System.currentTimeMillis()
)
