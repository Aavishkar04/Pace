package com.aavishkar.pace1

import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.data.repository.RideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RideRepositoryTest {

    private lateinit var fakeDao: FakeRideDao
    private lateinit var repository: RideRepository

    @Before
    fun setUp() {
        fakeDao = FakeRideDao()
        repository = RideRepository(fakeDao)
    }

    @Test
    fun startNewRide_createsRecordingEntityAndSetsState() = runTest {
        val rideId = repository.startNewRide()

        assertNotNull(rideId)
        assertEquals(RideState.RECORDING, repository.activeRideState.value)
        assertEquals(rideId, repository.activeRideId.value)

        val entity = fakeDao.getRideById(rideId)
        assertNotNull(entity)
        assertEquals("RECORDING", entity?.status)
    }

    @Test
    fun addLocationPoint_accumulatesDistanceGeographicallyAndPersistsToDao() = runTest {
        val rideId = repository.startNewRide()

        // 10 seconds apart (100.3 meters / 10s = ~10 m/s = 36 km/h)
        val p1 = LocationPoint(19.0390978, 73.0697035, 1000L, 0.0, 5.0f, 0f, 3.0f, hasSpeed = true)
        val p2 = LocationPoint(19.0400000, 73.0697035, 11000L, 0.0, 10.0f, 0f, 3.0f, hasSpeed = true)

        repository.addLocationPoint(p1)
        assertEquals(0f, repository.activeRideMetrics.value.distanceMeters, 0.001f)
        assertEquals(5.0f, repository.activeRideMetrics.value.rawSpeedMps, 0.001f)

        repository.addLocationPoint(p2)
        val dist = repository.activeRideMetrics.value.distanceMeters
        assert(dist > 100f)
        assertEquals(10.0f, repository.activeRideMetrics.value.maxSpeedMps, 0.001f)

        val savedPoints = fakeDao.getLocationPointsForRide(rideId)
        assertEquals(2, savedPoints.size)
    }

    @Test
    fun stopRide_setsStatusStoppedAndFreezesRepository() = runTest {
        val rideId = repository.startNewRide()
        repository.incrementTimerSecond()
        repository.stopRide()

        assertEquals(RideState.STOPPED, repository.activeRideState.value)

        val savedEntity = fakeDao.getRideById(rideId)
        assertEquals("STOPPED", savedEntity?.status)
        assertNotNull(savedEntity?.endTimeMillis)
    }

    @Test
    fun recoverActiveRideFromDb_recoversOngoingRideAndPoints() = runTest {
        val rideId = "recovered-ride-123"
        val entity = RideEntity(
            rideId = rideId,
            startTimeMillis = 1000L,
            elapsedTimeSeconds = 30L,
            distanceMeters = 300f,
            maxSpeedMps = 12f,
            averageSpeedMps = 10f,
            status = "RECORDING"
        )
        fakeDao.insertRide(entity)

        val point = LocationPointEntity(
            id = 1,
            rideId = rideId,
            latitude = 19.0390978,
            longitude = 73.0697035,
            timestamp = 2000L,
            altitude = 10.0,
            speed = 8.0f,
            bearing = 0f,
            accuracy = 4.0f
        )
        fakeDao.insertLocationPoint(point)

        repository.recoverActiveRideFromDb()

        assertEquals(RideState.RECORDING, repository.activeRideState.value)
        assertEquals(rideId, repository.activeRideId.value)
        val metrics = repository.activeRideMetrics.value
        assertEquals(30L, metrics.elapsedTimeSeconds)
        assertEquals(300f, metrics.distanceMeters, 0.001f)
        assertEquals(8.0f, metrics.rawSpeedMps, 0.001f)
        assertEquals(10f, metrics.averageSpeedMps, 0.001f)
        assertEquals(12f, metrics.maxSpeedMps, 0.001f)
    }

    private class FakeRideDao : RideDao {
        private val rides = mutableMapOf<String, RideEntity>()
        private val points = mutableListOf<LocationPointEntity>()

        override suspend fun insertRide(ride: RideEntity) {
            rides[ride.rideId] = ride
        }

        override suspend fun updateRide(ride: RideEntity) {
            rides[ride.rideId] = ride
        }

        override suspend fun getRideById(rideId: String): RideEntity? {
            return rides[rideId]
        }

        override suspend fun getActiveRecordingRide(): RideEntity? {
            return rides.values.lastOrNull { it.status == "RECORDING" }
        }

        override fun getAllCompletedRides(): Flow<List<RideEntity>> {
            return flowOf(rides.values.filter { it.status == "STOPPED" })
        }

        override fun observeActiveRecordingRide(): Flow<RideEntity?> {
            return flowOf(rides.values.lastOrNull { it.status == "RECORDING" })
        }

        override suspend fun insertLocationPoint(point: LocationPointEntity) {
            points.add(point)
        }

        override suspend fun getLocationPointsForRide(rideId: String): List<LocationPointEntity> {
            return points.filter { it.rideId == rideId }
        }

        override fun observeLocationPointsForRide(rideId: String): Flow<List<LocationPointEntity>> {
            return flowOf(points.filter { it.rideId == rideId })
        }
    }
}
