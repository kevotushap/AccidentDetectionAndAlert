package com.example.accidentdetectionandalert;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public interface onAccuracyChanged {
    void onSensorChanged(SensorEvent event);

    void onAccuracyChanged(Sensor sensor, int accuracy);
}
