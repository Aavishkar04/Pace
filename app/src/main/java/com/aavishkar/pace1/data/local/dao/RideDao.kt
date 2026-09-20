package com.aavishkar.pace1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO interface providing Room database queries for rides and location points.
 */
@Dao
interface RideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("SELECT * FROM rides WHERE rideId = :rideId")
    suspend fun getRideById(rideId: String): RideEntity?

    @Query("SELECT * FROM rides WHERE status = 'RECORDING' ORDER BY startTimeMillis DESC LIMIT 1")
    suspend fun getActiveRecordingRide(): RideEntity?

    @Query("SELECT * FROM rides WHERE status = 'RECORDING' ORDER BY startTimeMillis DESC LIMIT 1")
    fun observeActiveRecordingRide(): Flow<RideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(point: LocationPointEntity)

    @Query("SELECT * FROM location_points WHERE rideId = :rideId ORDER BY timestamp ASC")
    suspend fun getLocationPointsForRide(rideId: String): List<LocationPointEntity>

    @Query("SELECT * FROM location_points WHERE rideId = :rideId ORDER BY timestamp ASC")
    fun observeLocationPointsForRide(rideId: String): Flow<List<LocationPointEntity>>
}
