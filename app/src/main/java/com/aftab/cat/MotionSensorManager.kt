package com.aftab.cat


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotionSensorManager @Inject constructor() : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Motion data
    private var accelerationX = 0f
    private var accelerationY = 0f
    private var rotationX = 0f
    private var rotationY = 0f

    // Smoothing filter
    private val alpha = 0.8f
    private var smoothedAccelX = 0f
    private var smoothedAccelY = 0f
    private var smoothedRotX = 0f
    private var smoothedRotY = 0f

    // Motion callback
    private var motionCallback: ((swayX: Float, swayY: Float) -> Unit)? = null

    fun initialize(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun startListening() {
        accelerometer?.let { accel ->
            sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let { gyro ->
            sensorManager?.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setMotionCallback(callback: (swayX: Float, swayY: Float) -> Unit) {
        this.motionCallback = callback
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    // Get raw acceleration values
                    accelerationX = sensorEvent.values[0]
                    accelerationY = sensorEvent.values[1]

                    // Apply smoothing filter
                    smoothedAccelX = alpha * smoothedAccelX + (1 - alpha) * accelerationX
                    smoothedAccelY = alpha * smoothedAccelY + (1 - alpha) * accelerationY
                }
                Sensor.TYPE_GYROSCOPE -> {
                    // Get rotation values
                    rotationX = sensorEvent.values[0]
                    rotationY = sensorEvent.values[1]

                    // Apply smoothing filter
                    smoothedRotX = alpha * smoothedRotX + (1 - alpha) * rotationX
                    smoothedRotY = alpha * smoothedRotY + (1 - alpha) * rotationY
                }
            }

            // Calculate sway based on both acceleration and rotation
            calculateSway()
        }
    }

    private fun calculateSway() {
        // Combine accelerometer and gyroscope data for more realistic movement
        val swayX = (-smoothedAccelX * 0.3f) + (smoothedRotY * 10f)
        val swayY = (smoothedAccelY * 0.2f) + (smoothedRotX * 8f)

        // Limit the sway range (in pixels)
        val maxSway = 50f
        val clampedSwayX = swayX.coerceIn(-maxSway, maxSway)
        val clampedSwayY = swayY.coerceIn(-maxSway, maxSway)

        // Notify callback
        motionCallback?.invoke(clampedSwayX, clampedSwayY)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    fun cleanup() {
        stopListening()
        sensorManager = null
        accelerometer = null
        gyroscope = null
        motionCallback = null
    }
}