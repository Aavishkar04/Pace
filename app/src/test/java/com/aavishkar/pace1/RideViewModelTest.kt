package com.aavishkar.pace1

import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.location.LocationUpdateState
import com.aavishkar.pace1.ui.RideViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeLocationClient: FakeLocationClient
    private lateinit var viewModel: RideViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeLocationClient = FakeLocationClient()
        viewModel = RideViewModel(fakeLocationClient)
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
        assertEquals(0f, metrics.currentSpeedMps, 0.001f)
        assertEquals(0f, metrics.averageSpeedMps, 0.001f)
        assertEquals(0f, metrics.maxSpeedMps, 0.001f)
    }

    @Test
    fun startRide_changesStateToRecording() {
        viewModel.startRide()
        assertEquals(RideState.RECORDING, viewModel.rideState.value)
    }

    @Test
    fun incrementTimerSecond_incrementsElapsedTimeAndRecalculatesAvgSpeed() {
        viewModel.startRide()

        // Process initial location point
        val point1 = LocationPoint(
            latitude = 19.0390978,
            longitude = 73.0697035,
            timestamp = 1000L,
            altitude = 10.0,
            speed = 5.0f,
            bearing = 0f,
            accuracy = 5.0f
        )
        viewModel.processLocationPoint(point1)

        // Process second location point 100m away
        val point2 = LocationPoint(
            latitude = 19.0400000,
            longitude = 73.0697035,
            timestamp = 2000L,
            altitude = 10.0,
            speed = 10.0f,
            bearing = 0f,
            accuracy = 5.0f
        )
        viewModel.processLocationPoint(point2)

        // Increment timer to 10 seconds
        repeat(10) {
            viewModel.incrementTimerSecond()
        }

        val metrics = viewModel.rideMetrics.value
        assertEquals(10L, metrics.elapsedTimeSeconds)
        assertEquals(10.0f, metrics.currentSpeedMps, 0.001f)
        assertEquals(10.0f, metrics.maxSpeedMps, 0.001f)
        // Distance is ~100.3 meters. Avg speed over 10 seconds should be ~10.03 m/s
        assertEquals(metrics.distanceMeters / 10f, metrics.averageSpeedMps, 0.001f)
    }

    @Test
    fun processLocationPoint_accumulatesDistanceGeographicallyAndTracksMaxSpeed() {
        viewModel.startRide()

        val p1 = LocationPoint(19.0390978, 73.0697035, 1000L, 0.0, 4.0f, 0f, 3.0f)
        val p2 = LocationPoint(19.0400000, 73.0697035, 2000L, 0.0, 8.0f, 0f, 3.0f)
        val p3 = LocationPoint(19.0410000, 73.0697035, 3000L, 0.0, 6.0f, 0f, 3.0f)

        viewModel.processLocationPoint(p1)
        assertEquals(0f, viewModel.rideMetrics.value.distanceMeters, 0.001f)
        assertEquals(4.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)

        viewModel.processLocationPoint(p2)
        val dist1 = viewModel.rideMetrics.value.distanceMeters
        assert(dist1 > 100f)
        assertEquals(8.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)

        viewModel.processLocationPoint(p3)
        val dist2 = viewModel.rideMetrics.value.distanceMeters
        assert(dist2 > dist1)
        // Max speed should remain 8.0 m/s even though current speed dropped to 6.0 m/s
        assertEquals(8.0f, viewModel.rideMetrics.value.maxSpeedMps, 0.001f)
        assertEquals(6.0f, viewModel.rideMetrics.value.currentSpeedMps, 0.001f)
        assertNotNull(viewModel.rideMetrics.value.lastLocationPoint)
    }

    @Test
    fun stopRide_changesStateToStoppedAndFreezesMetrics() {
        viewModel.startRide()
        viewModel.incrementTimerSecond()
        assertEquals(RideState.RECORDING, viewModel.rideState.value)

        viewModel.stopRide()
        assertEquals(RideState.STOPPED, viewModel.rideState.value)
        assertEquals(1L, viewModel.rideMetrics.value.elapsedTimeSeconds)

        // Further location points or timer increments should be ignored
        viewModel.incrementTimerSecond()
        assertEquals(1L, viewModel.rideMetrics.value.elapsedTimeSeconds)
    }

    @Test
    fun resetRide_resetsStateToIdleAndClearsMetrics() {
        viewModel.startRide()
        viewModel.incrementTimerSecond()
        viewModel.stopRide()

        viewModel.resetRide()
        assertEquals(RideState.IDLE, viewModel.rideState.value)
        assertEquals(0L, viewModel.rideMetrics.value.elapsedTimeSeconds)
        assertEquals(0f, viewModel.rideMetrics.value.distanceMeters, 0.001f)
    }

    private class FakeLocationClient : LocationClient {
        override fun getLocationUpdates(intervalMs: Long): Flow<LocationUpdateState> = emptyFlow()
    }
}
