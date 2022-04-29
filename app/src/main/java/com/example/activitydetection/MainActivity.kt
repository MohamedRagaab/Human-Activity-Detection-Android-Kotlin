package com.example.activitydetection

import android.animation.ObjectAnimator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(){

    // Declare UI Variables
    private lateinit var xAccelerometer: ProgressBar
    private lateinit var yAccelerometer: ProgressBar
    private lateinit var zAccelerometer: ProgressBar
    private lateinit var lux: ProgressBar
    private lateinit var circle : TextView

    // Sensor Variables
    private lateinit var accelerometer: SensorManager
    private lateinit var gyroscope: SensorManager
    private lateinit var lightSensor: SensorManager
    private lateinit var soundSensor: SensorManager
    private lateinit var locationSensor: SensorManager

    // On Sensor Changed
    private val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            // Accelerometer
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                xAccelerometer.progress = x.toInt() * 9
                yAccelerometer.progress = y.toInt() * 9
                zAccelerometer.progress = z.toInt() * 9
            }
            // Gyroscope
            if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE){
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val tx = ObjectAnimator.ofFloat(
                    circle,
                    "translationX",
                    circle.translationX,
                    circle.translationX + 100f
                )
                val ty = ObjectAnimator.ofFloat(
                    circle,
                    "translationY",
                    circle.translationY,
                    circle.translationY + 0
                )
                tx.duration = 500
                ty.duration = 500
                tx.start()
            }
            // Light
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val x = event.values[0]

                lux.progress = x.toInt()

            }

        }

        override fun onAccuracyChanged(event: Sensor?, p1: Int) {

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get UI Variables
        xAccelerometer = findViewById(R.id.xAccelerometer)
        yAccelerometer = findViewById(R.id.yAccelerometer)
        zAccelerometer = findViewById(R.id.zAccelerometer)
        lux = findViewById(R.id.lux)
        circle = findViewById(R.id.circle)
        // Initialize Sensor
        setupSensorStuff()
    }
    private fun setupSensorStuff(){
        // Accelerometer Initialization
        accelerometer = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            accelerometer.registerListener(sensorEventListener,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST)
        }
        // Gyroscope Initialization
        gyroscope = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            gyroscope.registerListener(sensorEventListener,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST)
        }
        // Light Sensor Initialization
        lightSensor = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor.getDefaultSensor(Sensor.TYPE_LIGHT)?.also {
            lightSensor.registerListener(sensorEventListener,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST)
        }


    }

    override fun onDestroy() {
        accelerometer.unregisterListener(sensorEventListener)
        super.onDestroy()
    }

}