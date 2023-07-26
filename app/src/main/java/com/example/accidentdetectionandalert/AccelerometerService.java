package com.example.accidentdetectionandalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AccelerometerService extends Service implements SensorEventListener {

    int count = 1;
    private boolean init;
    private Sensor mySensor;
    private SensorManager SM;
    private float x1, x2, x3;
    private static final float ERROR = (float) 7.0;
    private static final float SHAKE_THRESHOLD = 10.00f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private TextView counter;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                Log.d("mySensor", "Acceleration is " + acceleration + "m/s^2");

                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(getApplicationContext(), "ACCIDENT DETECTED",
                            Toast.LENGTH_SHORT).show();
                    Intent ii = new Intent();
                    ii.setClass(this, Abort.class);
                    ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(ii);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }    /*
    protected void onResume() {
        super.onResume();
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        SM.unregisterListener(this);
    }

     */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this, "channelId1")

                .setContentTitle("Accident Detection And Alert")
                .setContentText("Accelerometer Service is Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pi).build();


        startForeground(1, notification);
        //notification end

        Toast.makeText(this, "Start Detecting", Toast.LENGTH_SHORT).show();
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener((SensorEventListener) this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        return Service.START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nFc = new NotificationChannel("channelId1", "Accident Detection", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(nFc);
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Accelerometer Service Stopped", Toast.LENGTH_LONG).show();
        stopForeground(true);
        SM.unregisterListener(this);
        super.onDestroy();
    }


   /* @Override
    public boolean stopService(Intent name) {
        Toast.makeText(this,"acc stopped",Toast.LENGTH_LONG).show();
        SM.unregisterListener(this);
        return super.stopService(name);
    }

    */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
