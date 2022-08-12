package com.example.activitydetection

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(){

    // Timer for get sound Amplitude
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    private val mToastRunnable: Runnable = object : Runnable {
        override fun run() {
            Toast.makeText(this@MainActivity, "This is a delayed toast", Toast.LENGTH_SHORT).show()
            mHandler.postDelayed(this, 5000)
        }
    }
    fun startRepeating(v: View) {
        //mHandler.postDelayed(mToastRunnable, 5000);
        mToastRunnable.run()
    }

    // Declare UI Variables ************************************************************************
    private lateinit var xAccelerometer: ProgressBar
    private lateinit var yAccelerometer: ProgressBar
    private lateinit var zAccelerometer: ProgressBar
    private lateinit var luxProgressBar: CircularProgressBar
    private lateinit var tempProgressBar: CircularProgressBar
    private lateinit var circle : TextView
    private lateinit var locactionText : TextView
    private lateinit var LuxText : TextView
    private lateinit var sound1 : ProgressBar
    private lateinit var sound2 : ProgressBar
    private lateinit var sound3 : ProgressBar
    private lateinit var sound4 : ProgressBar
    private lateinit var sound5 : ProgressBar
    private lateinit var sound6 : ProgressBar
    private lateinit var textTempValue : TextView
    private lateinit var switch_btn : SwitchCompat
    // Sensor Variables ****************************************************************************
    private lateinit var accelerometer: SensorManager
    private lateinit var gyroscope: SensorManager
    private lateinit var lightSensor: SensorManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //private var mRecorder : MediaRecorder?=null
    val mainHandler = Handler(Looper.getMainLooper())

    private val channelId = "channel_id_foreground_service"
    companion object {
        //Permissions

        val REQ_CODE_REC_AUDIO_AND_WRITE_EXTERNAL: Int = 101
        val REQ_CODE_RECORD_AUDIO = 303
        val REQ_CODE_WRITE_EXTERNAL_STORAGE = 404
        val REQ_CODE_READ_EXTERNAL_STORAGE_IMPORT = 405
        val REQ_CODE_READ_EXTERNAL_STORAGE_PLAYBACK = 406
        val REQ_CODE_READ_EXTERNAL_STORAGE_DOWNLOAD = 407
        val REQ_CODE_IMPORT_AUDIO = 11

        var notificationId = 1111
        var long:Double?=0.0
        var lat:Double?=0.0
        var stop = true
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
                xAccelerometer.progress = x.toInt() * 9
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
                luxProgressBar.progress = x.toInt().toFloat()
                LuxText.text = "${x.toInt()} lux"
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
        // Screen Width
        var displayMetrics: DisplayMetrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(displayMetrics)
        var height:Float = displayMetrics.ydpi
        var width:Float = displayMetrics.xdpi
        // remove notification
        var notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MainActivity.notificationId)
        // Get UI Variables
        xAccelerometer = findViewById(R.id.xAccelerometer)
        yAccelerometer = findViewById(R.id.yAccelerometer)
        zAccelerometer = findViewById(R.id.zAccelerometer)
        tempProgressBar = findViewById(R.id.temp)
        luxProgressBar = findViewById(R.id.lux)
        LuxText = findViewById(R.id.luxValue)
        circle = findViewById(R.id.circle)
        locactionText = findViewById(R.id.longAndLat)
        sound1 = findViewById(R.id.sound1)
        sound2 = findViewById(R.id.sound2)
        sound3 = findViewById(R.id.sound3)
        sound4 = findViewById(R.id.sound4)
        sound5 = findViewById(R.id.sound5)
        switch_btn = findViewById(R.id.switchButton)
        textTempValue = findViewById(R.id.tempValue)
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
        // Sound
        getSoundLevel()
        // Temperature
        registerReceiver(this.batteryBroadcast, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        // Foreground Service
        if(!stop){
            switch_btn.isChecked = true
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

    // Get Temperature *****************************************************************************
    var batteryBroadcast = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            val temp = intent.getIntExtra("temperature",-1)/ 10
            textTempValue.text = "$temp °C"
            tempValue = temp.toFloat()
            tempProgressBar.progress = temp.toFloat()
        }
    }


    /* setup sound sensor **************************************************************************/

    private fun getSoundLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,1)
            }
            while(true){
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                    break
                }
            }
                //var AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" +"audio_record.mp3"
                //var direPath = "${externalCacheDir?.absolutePath}/audio_record.mp3"
                var soundMeterObj: SoundMeter = SoundMeter()
                soundMeterObj.start("/dev/null")
                //soundMeterObj.start(AudioSavePathInDevice)
                mainHandler.post(object : Runnable {
                    override fun run() {
                        var amp = soundMeterObj.amplitude/2700.0
                        sound1.progress = amp.toInt() * 50
                        sound2.progress = (amp).toInt() * 40
                        sound3.progress = (amp).toInt() * 30
                        sound4.progress = (amp).toInt() * 20
                        sound5.progress = (amp).toInt() * 10
                        mainHandler.postDelayed(this, 50)
                        soundValue = amp.toFloat() * 10
                    }
                })


        }
    }

    /* Setup Sensors ************************************************************************************/
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
                long = (long!! * 10.0).roundToInt() / 10.0
                lat = (lat!! * 10.0).roundToInt() / 10.0
                //long = String.format("%.2f", long).toDouble()
                //lat = String.format("%.2f", lat).toDouble()
                var text = "$lat, $long"
                locactionText.text = text
            }
        }
    }
    private fun initLocationProviderClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

/* Foreground Service *************************************************************************************************/

    private fun launchForegroundService(){
         val serviceIntent : Intent = Intent(this,MyForegroundService::class.java)
        if(!foregroundServiceRunning()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                applicationContext.startForegroundService(serviceIntent)
                //startService(serviceIntent)
                //startForegroundService(serviceIntent)


            }else
            {
                startService(serviceIntent)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }



}