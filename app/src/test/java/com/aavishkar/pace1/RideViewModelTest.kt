package com.aavishkar.pace1

import android.content.Context
import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.data.repository.RideRepository
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.location.LocationUpdateState
import com.aavishkar.pace1.ui.RideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakeRideDao
    private lateinit var repository: RideRepository
    private lateinit var fakeLocationClient: FakeLocationClient
    private lateinit var viewModel: RideViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeRideDao()
        repository = RideRepository(fakeDao)
        fakeLocationClient = FakeLocationClient()
        viewModel = RideViewModel(repository, fakeLocationClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdleWithZeroMetrics() {
        assertEquals(RideState.IDLE, viewModel.rideState.value)
        val metrics = viewModel.rideMetrics.value
        assertEquals(0L, metrics.elapsedTimeSeconds)
        assertEquals(0f, metrics.distanceMeters, 0.001f)
    }

    @Test
    fun resetRide_delegatesToRepositoryAndResetsState() = runTest {
        repository.startNewRide()
        repository.stopRide()

        viewModel.resetRide()
        assertEquals(RideState.IDLE, viewModel.rideState.value)
    }

    @Test
    fun incrementTimerSecond_incrementsElapsedTimeAndRecalculatesAvgSpeed() = runTest {
        repository.startNewRide()

        // Process initial location point
        val point1 = LocationPoint(
            latitude = 19.0390978,
            longitude = 73.0697035,
            timestamp = 1000L,
            altitude = 10.0,
            speed = 5.0f,
            bearing = 0f,
            accuracy = 5.0f,
            hasSpeed = true
        )
        repository.addLocationPoint(point1)

        // Process second location point 100m away (10 seconds later)
        val point2 = LocationPoint(
            latitude = 19.0400000,
            longitude = 73.0697035,
            timestamp = 11000L,
            altitude = 10.0,
            speed = 10.0f,
            bearing = 0f,
            accuracy = 5.0f,
            hasSpeed = true
        )
        repository.addLocationPoint(point2)

        // Increment timer to 10 seconds
        repeat(10) {
            repository.incrementTimerSecond()
        }

        val metrics = viewModel.rideMetrics.value
        assertEquals(10L, metrics.elapsedTimeSeconds)
        assertEquals(10.0f, metrics.currentSpeedMps, 0.001f)
        assertEquals(10.0f, metrics.maxSpeedMps, 0.001f)
        // Distance is ~100.3 meters. Avg speed over 10 seconds should be ~10.03 m/s
        assertEquals(metrics.distanceMeters / 10f, metrics.averageSpeedMps, 0.001f)
    }

    @Test
    fun processLocationPoint_accumulatesDistanceGeographicallyAndTracksMaxSpeed() = runTest {
        repository.startNewRide()

        val p1 = LocationPoint(19.0390978, 73.0697035, 1000L, 0.0, 4.0f, 0f, 3.0f, hasSpeed = true)
        val p2 = LocationPoint(19.0400000, 73.0697035, 11000L, 0.0, 8.0f, 0f, 3.0f, hasSpeed = true)
        val p3 = LocationPoint(19.0410000, 73.0697035, 21000L, 0.0, 6.0f, 0f, 3.0f, hasSpeed = true)

        repository.addLocationPoint(p1)
        assertEquals(0f, viewModel.rideMetrics.value.distanceMeters, 0.001f)
        assertEquals(4.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)

        repository.addLocationPoint(p2)
        val dist1 = viewModel.rideMetrics.value.distanceMeters
        assert(dist1 > 100f)
        assertEquals(8.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)

        repository.addLocationPoint(p3)
        val dist2 = viewModel.rideMetrics.value.distanceMeters
        assert(dist2 > dist1)
        assertEquals(8.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)
        assertEquals(6.0f, viewModel.rideMetrics.value.currentSpeedMps, 0.001f)
    }

    private class FakeLocationClient : LocationClient {
        override fun getLocationUpdates(intervalMs: Long): Flow<LocationUpdateState> = emptyFlow()
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

        override suspend fun getRideById(rideId: String): RideEntity? = rides[rideId]

        override suspend fun getActiveRecordingRide(): RideEntity? =
            rides.values.lastOrNull { it.status == "RECORDING" }

        override fun getAllCompletedRides(): Flow<List<RideEntity>> =
            flowOf(rides.values.filter { it.status == "STOPPED" })

        override fun observeActiveRecordingRide(): Flow<RideEntity?> =
            flowOf(rides.values.lastOrNull { it.status == "RECORDING" })

        override suspend fun insertLocationPoint(point: LocationPointEntity) {
            points.add(point)
        }

        override suspend fun getLocationPointsForRide(rideId: String): List<LocationPointEntity> =
            points.filter { it.rideId == rideId }

        override fun observeLocationPointsForRide(rideId: String): Flow<List<LocationPointEntity>> =
            flowOf(points.filter { it.rideId == rideId })
    }
}
