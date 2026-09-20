package com.aavishkar.pace1

import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
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
