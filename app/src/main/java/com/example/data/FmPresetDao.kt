package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FmPresetDao {
    @Query("SELECT * FROM fm_presets ORDER BY frequencyMhz ASC")
    fun getAllPresets(): Flow<List<FmPresetEntity>>

    @Query("SELECT * FROM fm_presets WHERE frequencyMhz = :freq LIMIT 1")
    suspend fun getPresetByFrequency(freq: Float): FmPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: FmPresetEntity)

    @Delete
    suspend fun deletePreset(preset: FmPresetEntity)

    @Query("DELETE FROM fm_presets WHERE frequencyMhz = :freq")
    suspend fun deleteByFrequency(freq: Float)
}
