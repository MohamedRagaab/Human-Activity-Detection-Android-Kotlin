package com.example.activitydetection

import android.app.NotificationManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class MainActivity2 : AppCompatActivity() {
    var tutorials = arrayOf(
        "Working on Computer", "On Phone",
        "Watching TV", "Listening to music/programme",
        "Reading", "Exercise",
        "Interacting with others", "Food prep",
        "Cooking", "Eating", "House Cleaning", "Resting/Quiet Time", "Sleeping 14. Locomotion/Moving", "Driving", "Riding on the bus", "Shopping"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        // remove notification *********************************************************************
        var notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MainActivity.notificationId)
        // Initialization Activity List Notification ***********************************************
        var listView: ListView  = findViewById(R.id.list_view)
        val arr: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.listitem,
            tutorials
        )
        listView.adapter = arr
    }
}