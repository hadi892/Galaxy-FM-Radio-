package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FmPresetEntity::class], version = 1, exportSchema = false)
abstract class FmPresetDatabase : RoomDatabase() {
    abstract fun presetDao(): FmPresetDao

    companion object {
        @Volatile
        private var INSTANCE: FmPresetDatabase? = null

        fun getDatabase(context: Context): FmPresetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FmPresetDatabase::class.java,
                    "fm_presets_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
