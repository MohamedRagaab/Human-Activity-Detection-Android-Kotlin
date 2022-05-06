package com.example.activitydetection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyForegroundService extends Service {
    // old
    Float[] AccelerometerValues = {0.0f,0.0f,0.0f};
    Float[] GyroscopeValues = {0.0f,0.0f,0.0f};
    Float LuxValue = 0.0f;
    Double Long = 0.0;
    Double Lat = 0.0;
    Float SoundValue = 0.0f;

    // new
    Float[] NewAccelerometerValues = new Float[3];
    Float[] NewGyroscopeValues = new Float[3];
    Float NewLuxValue;
    Double NewLong;
    Double NewLat;
    Float NewSoundValue;

    String channelId = "foreground";
    String channelIdActivity = "Activity";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            // Activity Detection **************************************************
                            NewInitialization();
                            activityDetection();
                            //MainActivity.
                            Log.e("Service", MainActivity.Companion.getAccelerometerValues()[1].toString());
                            Log.e("Service", MainActivity.Companion.getGyroscopeValues()[1].toString());
                            Log.e("Service", String.valueOf(MainActivity.Companion.getLuxValue()));
                            Log.e("Service", String.valueOf(MainActivity.Companion.getLong()));
                            Log.e("Service", String.valueOf(MainActivity.Companion.getLat()));
                            Log.e("Service", String.valueOf(MainActivity.Companion.getSoundValue()));
                            Log.e("Service", "Service is running...");

                            if(MainActivity.Companion.getStop()){
                                stopServiceRunning();
                            }

                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        createNotificationChannel(channelId);

        startForeground(1001,sendNotificationForForegroundService(1001).build());


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /* Sensors Parameters Changed **********************************************************************/
    private void activityDetection() {

        // Orientation ************************************************************
        if(Math.abs(NewAccelerometerValues[1]-AccelerometerValues[1])>5.0){
            AccelerometerValues[0] = MainActivity.Companion.getAccelerometerValues()[0];
            AccelerometerValues[1] = MainActivity.Companion.getAccelerometerValues()[1];
            AccelerometerValues[2] = MainActivity.Companion.getAccelerometerValues()[2];
            createNotificationChannel(channelIdActivity);
            sendNotificationForUser(1111);
        }

    }




    /* Notification Channel ****************************************************************************/
    void createNotificationChannel(String channelId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            String name = "Activity Detection";
            String descriptionText = "Create Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }
/* Send Notification For Foreground Service ********************************************************/
    NotificationCompat.Builder sendNotificationForForegroundService(int notificationId){
        // Tapping Notification
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(this, 0, mainIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Activity Detection is running")
                .setContentText("The app is detecting your Activity")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPendingIntent);
        return builder;
    }

    // Activity Detection Notification *************************************************************
    void sendNotificationForUser(int notificationId){
        // Tapping Notification
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(this, 0, mainIntent, 0);
        // Tapping Select btn
        Intent selectionIntent = new Intent(this, MainActivity2.class);
        selectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent selectionPendingIntent =
                PendingIntent.getActivity(this, 0, selectionIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("What are you doing now?")
                .setContentText("Click select to show activities list")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground,"Select",selectionPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground,"Cancel",mainPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }



/* Helper Functions ********************************************************************************/
    void NewInitialization (){
        // Initialization ******************************************************
        NewAccelerometerValues[0] = MainActivity.Companion.getAccelerometerValues()[0];
        NewAccelerometerValues[1] = MainActivity.Companion.getAccelerometerValues()[1];
        NewAccelerometerValues[2] = MainActivity.Companion.getAccelerometerValues()[2];
        NewGyroscopeValues[0] = MainActivity.Companion.getGyroscopeValues()[0];
        NewGyroscopeValues[1] = MainActivity.Companion.getGyroscopeValues()[1];
        NewGyroscopeValues[2] = MainActivity.Companion.getGyroscopeValues()[2];
        NewLuxValue = MainActivity.Companion.getLuxValue();
        NewLong = MainActivity.Companion.getLong();
        NewLat = MainActivity.Companion.getLat();
        NewSoundValue = MainActivity.Companion.getSoundValue();
    }
    /*
    void OldInitialization(){
        // New Initialization ******************************************************
        AccelerometerValues[0] = MainActivity.Companion.getAccelerometerValues()[0];
        AccelerometerValues[1] = MainActivity.Companion.getAccelerometerValues()[1];
        AccelerometerValues[2] = MainActivity.Companion.getAccelerometerValues()[2];
        GyroscopeValues[0] = MainActivity.Companion.getGyroscopeValues()[0];
        GyroscopeValues[1] = MainActivity.Companion.getGyroscopeValues()[1];
        GyroscopeValues[2] = MainActivity.Companion.getGyroscopeValues()[2];
        LuxValue = MainActivity.Companion.getLuxValue();
        Long = MainActivity.Companion.getLong();
        Lat = MainActivity.Companion.getLat();
        SoundValue = MainActivity.Companion.getSoundValue();
    }
*/
    Double getDistanceFromLatLonInKm(Double lat1,Double lon1,Double lat2,Double lon2) {
        int R = 6371; // Radius of the earth in km
        Double dLat = deg2rad(lat2-lat1);  // deg2rad below
        Double dLon = deg2rad(lon2-lon1);
        Double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double d = R * c; // Distance in km
        return d;
    }

    Double deg2rad(Double deg) {
        return deg * (Math.PI/180);
    }


/* Stop Foreground Service *************************************************************************/
    void stopServiceRunning(){
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();

    }
}