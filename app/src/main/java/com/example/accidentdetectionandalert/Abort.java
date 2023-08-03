package com.example.accidentdetectionandalert;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class abort extends AppCompatActivity {

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    boolean smsSent = false;

    Button abortBt;
    LocationFinder finder;
    private static final float SHAKE_THRESHOLD = 12.0f; // m/s^2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abort);
        abortBt = findViewById(R.id.abort1);
        //extracting no//
        FileInputStream fis = null;
        try {
            fis = openFileInput(MainActivity.FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            MainActivity.no1 = br.readLine();
            MainActivity.no2 = br.readLine();
            MainActivity.no3 = br.readLine();
            MainActivity.bgrp = br.readLine();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 26) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    Toast.makeText(abort.this, "SHAKING", Toast.LENGTH_SHORT).show();
                } else {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
                }
            }
        }, 5000);

        // Get the device's location in a background thread
        new LocationTask().execute();

        abortBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity instead of exiting the application
            }
        });
    }

    private class LocationTask extends AsyncTask<Void, Void, Location> {

        @Override
        protected Location doInBackground(Void... voids) {
            finder = new LocationFinder(abort.this);
            if (finder.canGetLocation()) {
                latitude = finder.getLatitude();
                longitude = finder.getLongitude();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            if (latitude != 0.0 && longitude != 0.0) {
                sendAccidentSMS();
            } else {
                Toast.makeText(abort.this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendAccidentSMS() {
        // Send SMS
        String message = "Accident detected! Need help!";
        String phoneNumber = MainActivity.no1; // Change this to the desired phone number

        // Access SHAKE_THRESHOLD value from AccelerometerService class
        float shakeThreshold = AccelerometerService.getShakeThreshold();

        Toast.makeText(abort.this, "Latitude" + latitude + " Longitude" + longitude, Toast.LENGTH_LONG).show();
        SmsManager sm = SmsManager.getDefault();
        for (String s : Arrays.asList("Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml")) {
            sm.sendTextMessage(MainActivity.no1, null, s, null, null);
        }
        sm.sendTextMessage(MainActivity.no2, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, null, null);
        sm.sendTextMessage(MainActivity.no2, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml", null, null);
        sm.sendTextMessage(MainActivity.no3, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, null, null);
        sm.sendTextMessage(MainActivity.no3, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml", null, null);

        Toast.makeText(abort.this, "MESSAGE SEND", Toast.LENGTH_SHORT).show();
    }

    private class SendSmsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String phoneNumber = params[0];
            String message = params[1];

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            return null;
        }
    }
}