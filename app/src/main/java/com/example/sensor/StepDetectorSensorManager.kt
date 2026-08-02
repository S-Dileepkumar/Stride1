package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class StepDetectorSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val stepDetector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _isSensorActive = MutableStateFlow(false)
    val isSensorActive: StateFlow<Boolean> = _isSensorActive.asStateFlow()

    private val _isAccelerometerAvailable = MutableStateFlow(accelerometer != null)
    val isAccelerometerAvailable: StateFlow<Boolean> = _isAccelerometerAvailable.asStateFlow()

    // Peak detection variables
    private var lastStepTimestamp = 0L
    private val minStepIntervalMs = 280L // ~214 steps/min max rate
    private var emaGravity = 9.81f
    private val alpha = 0.1f // EMA filter weight
    private val peakThreshold = 2.2f // Acceleration magnitude peak above gravity

    fun startTracking() {
        _stepCount.value = 0
        registerListeners()
    }

    fun resumeTracking() {
        registerListeners()
    }

    private fun registerListeners() {
        if (sensorManager == null) return

        // Try registering accelerometer for manual peak detection
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            _isSensorActive.value = true
        }

        // Also register step detector if hardware supports it for higher precision
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            _isSensorActive.value = true
        }
    }

    fun pauseTracking() {
        sensorManager?.unregisterListener(this)
        _isSensorActive.value = false
    }

    fun stopTracking() {
        sensorManager?.unregisterListener(this)
        _isSensorActive.value = false
    }

    fun setStepCount(count: Int) {
        _stepCount.value = count
    }

    fun incrementStepCount(amount: Int = 1) {
        _stepCount.value += amount
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isSensorActive.value) return

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            // Hardware step detector fired
            val now = System.currentTimeMillis()
            if (now - lastStepTimestamp > minStepIntervalMs) {
                lastStepTimestamp = now
                _stepCount.value += 1
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val rawMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            // Update exponential moving average for gravity
            emaGravity = alpha * rawMagnitude + (1 - alpha) * emaGravity

            // Delta from dynamic gravity
            val delta = rawMagnitude - emaGravity

            val now = System.currentTimeMillis()
            if (delta > peakThreshold && (now - lastStepTimestamp) > minStepIntervalMs) {
                lastStepTimestamp = now
                _stepCount.value += 1
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
