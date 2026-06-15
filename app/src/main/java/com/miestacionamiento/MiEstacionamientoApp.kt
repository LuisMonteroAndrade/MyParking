package com.miestacionamiento

import android.app.Application
import com.miestacionamiento.data.local.AppDatabase
import com.miestacionamiento.data.repository.ParkingRepository
import com.miestacionamiento.utils.NotificationHelper

class MiEstacionamientoApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ParkingRepository(database.parkingDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
