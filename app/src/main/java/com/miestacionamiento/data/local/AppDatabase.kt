package com.miestacionamiento.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miestacionamiento.data.local.dao.ParkingDao
import com.miestacionamiento.data.local.entity.ParkingEntity

@Database(entities = [ParkingEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parkingDao(): ParkingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_estacionamiento_db"
                ).build().also { INSTANCE = it }
            }
    }
}
