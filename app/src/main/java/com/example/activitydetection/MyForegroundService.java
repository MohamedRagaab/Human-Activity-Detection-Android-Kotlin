package com.example.activitydetection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyForegroundService extends Service {
    static boolean Labeling = false;
    // old
    Float[] AccelerometerValues = {0.0f,0.0f,0.0f};
    Float[] GyroscopeValues = {0.0f,0.0f,0.0f};
    Float LuxValue = 0.0f;
    Double Long = 0.0;
    Double Lat = 0.0;
    Float SoundValue = 0.0f;
    Float TempValue;
    Double Speed;

    // new
    static Float[] NewAccelerometerValues = new Float[3];
    static Float[] NewGyroscopeValues = new Float[3];
    static Float NewLuxValue;
    static Double NewLong;
    static Double NewLat;
    static Double[] location = new Double[2];
    static Float NewSoundValue;
    static Float NewTempValue;
    static Double NewSpeed;
    // Activity Data
    static ActivityData activityData = new ActivityData("","",NewAccelerometerValues,NewGyroscopeValues,location,NewLuxValue,NewSoundValue,NewTempValue,NewSpeed);

    String channelId = "foreground";
    String channelIdActivity = "Activity";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OldInitialization();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // Activity Detection **************************************************
                            NewInitialization();
                            activityDetection();

                            if(MainActivity.Companion.getStop()){
                                stopServiceRunning();
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

        //if(!Labeling && checkNotyExist()){

            // Calculate Speed *************************************************************************
            NewSpeed = getDistanceFromLatLonInKm(Lat,Long,NewLat,NewLong)/(5.555555555556*Math.pow(10,-4));

            // Orientation *****************************************************************************
            if(Math.abs(NewAccelerometerValues[0]-AccelerometerValues[0])>6.0){
                UpdateParameters();
            }

            // Speed ***********************************************************************************
            if(Math.abs(NewSpeed-Speed)>100 ){
                Speed = NewSpeed;
                UpdateParameters();
            }else  if(Math.abs(NewSpeed-Speed)>50){
                Speed = NewSpeed;
                UpdateParameters();
            }else  if(Math.abs(NewSpeed-Speed)>20){
                Speed = NewSpeed;
                UpdateParameters();
            }else  if(Math.abs(NewSpeed-Speed)>7){
                Speed = NewSpeed;
                UpdateParameters();
            }else  if(Math.abs(NewSpeed-Speed)>2){
                Speed = NewSpeed;
                UpdateParameters();
            }

            // Light ***********************************************************************************
            if(Math.abs(LuxValue-NewLuxValue)>800){
                UpdateParameters();
            }else if(Math.abs(LuxValue-NewLuxValue)>200){
                UpdateParameters();
            }else if(NewLuxValue == 0.0){
                UpdateParameters();
            }

            // Sound ***********************************************************************************
            if(Math.abs(SoundValue-NewSoundValue)>80){
                UpdateParameters();
            }
            // Temp ************************************************************************************
            if(Math.abs(TempValue-NewTempValue)>5){
                UpdateParameters();
            }
        //}
    }

    private boolean checkNotyExist() {

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications =
                new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == 1111) {
                    return false;
                }
            }
        }
        return true;
    }


    void UpdateParameters(){
        OldInitialization();
        if(!Labeling && checkNotyExist()){
            CreateModel();
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
                .addAction(R.drawable.ic_launcher_foreground,"Dismiss",mainPendingIntent)
                .setContentIntent(selectionPendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }



/* Helper Functions ********************************************************************************/
    void CreateModel(){
        activityData.Accelerometer[0] = AccelerometerValues[0];
        activityData.Accelerometer[1] = AccelerometerValues[1];
        activityData.Accelerometer[2] = AccelerometerValues[2];
        activityData.Gyroscope[0] = MainActivity.Companion.getAccelerometerValues()[0];
        activityData.Gyroscope[1] = MainActivity.Companion.getAccelerometerValues()[1];
        activityData.Gyroscope[2] = MainActivity.Companion.getAccelerometerValues()[2];
        activityData.SoundValue = MainActivity.Companion.getSoundValue();
        activityData.LuxValue = MainActivity.Companion.getLuxValue();
        activityData.Location[0] = MainActivity.Companion.getLong();
        activityData.Location[1] = MainActivity.Companion.getLat();
        activityData.TempValue = MainActivity.Companion.getTempValue();
        activityData.Speed = NewSpeed;
    }
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
        NewTempValue = MainActivity.Companion.getTempValue();
    }

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
        TempValue = MainActivity.Companion.getTempValue();
        Speed = 0.0;
    }

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