package com.example.activitydetection

import android.app.NotificationManager
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity2 : AppCompatActivity() {
    var tutorials = arrayOf(
        "Working on Computer", "On Phone",
        "Watching TV", "Listening to music-programme",
        "Reading", "Exercise",
        "Interacting with others", "Food prep",
        "Cooking", "Eating", "House Cleaning", "Resting/Quiet Time", "Sleeping 14. Locomotion-Moving", "Driving", "Riding on the bus", "Shopping"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        // remove notification *********************************************************************
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MainActivity.notificationId)
        // Initialization Activity List Notification ***********************************************
        val listView: ListView  = findViewById(R.id.list_view)
        val arr: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.listitem,
            tutorials
        )
        listView.adapter = arr

        // set activity labeled
        val id: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        MyForegroundService.activityData.android_id = id
        listView.onItemClickListener =
            OnItemClickListener {
                    parent, view, position, id ->
                MyForegroundService.activityData.ActivityLabel = tutorials[position]
                sendActivityToDatabase()
                Toast.makeText(this,tutorials[position],Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendActivityToDatabase() {
        var database = FirebaseDatabase.getInstance("https://activity-detection-app-default-rtdb.europe-west1.firebasedatabase.app").reference
        var key = MyForegroundService.activityData.android_id
        var data = MyForegroundService.activityData
        database.child(key).child(data.ActivityLabel).child("Accelerometer").setValue("X: "+data.Accelerometer[0].toString()+" Y: "+data.Accelerometer[1].toString()+" Z: "+data.Accelerometer[2].toString())
        database.child(key).child(data.ActivityLabel).child("Gyroscope").setValue("X: "+data.Gyroscope[0].toString()+" Y: "+data.Gyroscope[1].toString()+" Z: "+data.Gyroscope[2].toString())
        database.child(key).child(data.ActivityLabel).child("Location").setValue("Long: "+data.Location[0].toString()+" Lat: "+data.Location[1].toString())
        database.child(key).child(data.ActivityLabel).child("LuxValue").setValue("Lux: "+data.LuxValue.toString())
        database.child(key).child(data.ActivityLabel).child("SoundLevel").setValue("DP: "+data.SoundValue.toString())
    }
}