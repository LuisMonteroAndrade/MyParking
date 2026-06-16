package com.miestacionamiento.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.miestacionamiento.data.local.entity.ParkingEntity

@Dao
interface ParkingDao {

    @Query("SELECT * FROM parkings")
    fun getAllParkings(): LiveData<List<ParkingEntity>>

    @Query("SELECT * FROM parkings WHERE isSaved = 1")
    fun getSavedParkings(): LiveData<List<ParkingEntity>>

    @Query("SELECT * FROM parkings WHERE isRecentlyViewed = 1 LIMIT 10")
    fun getRecentlyViewedParkings(): LiveData<List<ParkingEntity>>

    @Query("SELECT * FROM parkings WHERE id = :id")
    suspend fun getParkingById(id: Int): ParkingEntity?

    @Query("SELECT * FROM parkings WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchParkings(query: String): LiveData<List<ParkingEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParkings(parkings: List<ParkingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParkings(parkings: List<ParkingEntity>)

    @Query("SELECT id FROM parkings WHERE isSaved = 1")
    suspend fun getSavedParkingIds(): List<Int>

    @Query("SELECT id FROM parkings WHERE isRecentlyViewed = 1")
    suspend fun getRecentlyViewedParkingIds(): List<Int>

    @Query("UPDATE parkings SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Int, isSaved: Boolean)

    @Query("UPDATE parkings SET isRecentlyViewed = :isViewed WHERE id = :id")
    suspend fun updateRecentlyViewed(id: Int, isViewed: Boolean)

    @Query("SELECT COUNT(*) FROM parkings")
    suspend fun count(): Int
}
