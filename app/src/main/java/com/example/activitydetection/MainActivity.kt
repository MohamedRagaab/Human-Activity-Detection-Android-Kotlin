package com.example.activitydetection

import android.Manifest
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(){

    // Declare UI Variables ************************************************************************
    private lateinit var xAccelerometer: ProgressBar
    private lateinit var yAccelerometer: ProgressBar
    private lateinit var zAccelerometer: ProgressBar
    private lateinit var luxProgressBar: CircularProgressBar
    private lateinit var tempProgressBar: CircularProgressBar
    private lateinit var circle : TextView
    private lateinit var locactionText : TextView
    private lateinit var maxAmplitude : TextView
    private lateinit var switch_btn : SwitchCompat
    // Sensor Variables ****************************************************************************
    private lateinit var accelerometer: SensorManager
    private lateinit var gyroscope: SensorManager
    private lateinit var lightSensor: SensorManager
    private lateinit var tempSensor: SensorManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mRecorder : MediaRecorder
    val mainHandler = Handler(Looper.getMainLooper())

    private val channelId = "channel_id_foreground_service"
    companion object {
        var notificationId = 101
        var long:Double?=0.0
        var lat:Double?=0.0
        var stop = false
        var accelerometerValues : Array<Float> = arrayOf(0.0f, 0.0f, 0.0f)
        var gyroscopeValues : Array<Float> = arrayOf(0.0f, 0.0f, 0.0f)
        var luxValue : Float = 0.0f
        var tempValue : Float = 0.0f
        var soundValue : Float = 0.0f
        var speedValue : Double = 0.0

    }

    // On Sensor Changed ***************************************************************************
    private val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            // Accelerometer
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                ObjectAnimator.ofInt(xAccelerometer, "progress", x.toInt() * 9)
                    .setDuration(300)
                    .start();
                //xAccelerometer.progress = x.toInt() * 9
                yAccelerometer.progress = y.toInt() * 9
                zAccelerometer.progress = z.toInt() * 9
                accelerometerValues[0] = x
                accelerometerValues[1] = y
                accelerometerValues[2] = z

            }
            // Gyroscope
            if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE){
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                gyroscopeValues[0] = x
                gyroscopeValues[1] = y
                gyroscopeValues[2] = z
                /*
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
                */
            }
            // Light
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val x = event.values[0]
                luxValue = x
                luxProgressBar.progress = x


            }
            // Temperature
            if (event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                val x = event.values[0]
                tempValue = x
                tempProgressBar.progress = x

            }

        }

        override fun onAccuracyChanged(event: Sensor?, p1: Int) {

        }

    }
/* Check Foreground Service **/
 fun foregroundServiceRunning(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
        if (MyForegroundService::class.java.name == service.service.className) {
            return true
        }
    }
    return false
}
/* onCreate ****************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // remove notification
        var notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MainActivity.notificationId)
        // Get UI Variables
        xAccelerometer = findViewById(R.id.xAccelerometer)
        yAccelerometer = findViewById(R.id.yAccelerometer)
        zAccelerometer = findViewById(R.id.zAccelerometer)
        tempProgressBar = findViewById(R.id.temp)
        luxProgressBar = findViewById(R.id.lux)
        circle = findViewById(R.id.circle)
        locactionText = findViewById(R.id.longAndLat)
        maxAmplitude = findViewById(R.id.amplitude)
        switch_btn = findViewById(R.id.switchButton)
        // Initialize Sensors **********************************************************************
        // Accelerometer, Gyroscope, Light
        setupSensorStuff()
        // Location
        mainHandler.post(object : Runnable {
            override fun run() {
                getUserLocation()
                mainHandler.postDelayed(this, 4000)
            }
        })
        getSoundLevel()
        // Foreground Service
        if(!stop){
            switch_btn.toggle()
        }
        switch_btn.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                stop = false
                launchForegroundService()
            } else {
               stop = true

            }
        })


    }
    /* setup sound sensor **************************************************************************/
    private fun getSoundLevel() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.RECORD_AUDIO), 1)

        } else {
        mRecorder = MediaRecorder()
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mRecorder.setOutputFile("/dev/null")
        mRecorder.prepare()
        mRecorder.start()
        maxAmplitude.text = mRecorder.maxAmplitude.toString()

        }

    }

    /* Setup Sensors ************************************************************************************/
    fun setupSensorStuff(){
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
        // Temperature Sensor Initialization
        tempSensor = getSystemService(SENSOR_SERVICE) as SensorManager
        tempSensor.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)?.also {
            tempSensor.registerListener(sensorEventListener,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST)
        }
        // Location
        initLocationProviderClient()
    }
/* Location ****************************************************************************************/
    private fun getUserLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                return
        }else{
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                long = location?.longitude
                lat = location?.latitude
                if (long == null) {
                    long = 0.0
                    lat = 0.0
                }
                long = String.format("%.2f", long).toDouble()
                lat = String.format("%.2f", lat).toDouble()
                var text = "$lat, $long"
                locactionText.text = text

            }
        }
    }
/*
    fun getDistanceFromLatLonInKm(lat1: Double ,lon1: Double,lat2: Double,lon2: Double): Double {
        var p: Double = 0.017453292519943295    // Math.PI / 180
        var a: Double = 0.5 - cos((lat2 - lat1) * p)/2 +
                cos(lat1 * p) * cos(lat2 * p) *
                (1 - cos((lon2 - lon1) * p))/2

        return 12742 * asin(sqrt(a)) // 2 * R; R = 6371 km
    }
*/

    private fun initLocationProviderClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }
    /*
    fun createNotificationChannel(channelId:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "Activity Detection"
            val descriptionText = "Create Notification Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

*/



/* Foreground Service *************************************************************************************************/

    private fun launchForegroundService(){
         val serviceIntent : Intent = Intent(this,MyForegroundService::class.java)
        if(!foregroundServiceRunning()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                //startService(serviceIntent)
                startForegroundService(serviceIntent)


            }else
            {
                startService(serviceIntent)
            }
        }
    }


    override fun onDestroy() {
        //accelerometer.unregisterListener(sensorEventListener)
        //gyroscope.unregisterListener(sensorEventListener)
        //lightSensor.unregisterListener(sensorEventListener)
        super.onDestroy()
    }



}