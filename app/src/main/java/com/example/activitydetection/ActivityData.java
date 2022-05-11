package com.example.activitydetection;

public class ActivityData {
    public String android_id = "";
    public String ActivityLabel = "";
    public Float[] Accelerometer = new Float[3];
    public Float[] Gyroscope = new Float[3];
    public Double[] Location = new Double[2];
    public Float LuxValue = 0.0f;
    public Float SoundValue = 0.0f;
    public Float TempValue = 0.0f;
    public Double Speed = 0.0;
    ActivityData(String android_id, String ActivityLabel, Float[] Accelerometer, Float[] Gyroscope, Double[] Location, Float LuxValue, Float SoundValue, Float TempValue, Double Speed){
        this.android_id = android_id;
        this.ActivityLabel = ActivityLabel;
        this.Accelerometer = Accelerometer;
        this.Gyroscope = Gyroscope;
        this.Location = Location;
        this.LuxValue = LuxValue;
        this.SoundValue = SoundValue;
        this.TempValue = TempValue;
        this.Speed = Speed;
    }

}
