package com.aavishkar.pace1.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

data class SensorHealth(
    val hasAccelerometer: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasBarometer: Boolean = false,
    val hasMagnetometer: Boolean = false
)

data class LatestSensorData(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val pressureHpa: Float = 0f
)

class SensorCollector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val baroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

    val sensorHealth = SensorHealth(
        hasAccelerometer = accelSensor != null,
        hasGyroscope = gyroSensor != null,
        hasBarometer = baroSensor != null,
        hasMagnetometer = magSensor != null
    )

    var latestData = LatestSensorData()
        private set

    fun startListening() {
        accelSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        baroSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                latestData = latestData.copy(
                    accelX = event.values.getOrNull(0) ?: 0f,
                    accelY = event.values.getOrNull(1) ?: 0f,
                    accelZ = event.values.getOrNull(2) ?: 0f
                )
            }
            Sensor.TYPE_PRESSURE -> {
                latestData = latestData.copy(
                    pressureHpa = event.values.getOrNull(0) ?: 0f
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
