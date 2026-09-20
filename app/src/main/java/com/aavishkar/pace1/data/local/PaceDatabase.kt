package com.aavishkar.pace1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity

/**
 * Main Room Database for Pace1 local persistence.
 */
@Database(
    entities = [RideEntity::class, LocationPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PaceDatabase : RoomDatabase() {

    abstract fun rideDao(): RideDao

    companion object {
        @Volatile
        private var INSTANCE: PaceDatabase? = null

        fun getInstance(context: Context): PaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaceDatabase::class.java,
                    "pace1_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
