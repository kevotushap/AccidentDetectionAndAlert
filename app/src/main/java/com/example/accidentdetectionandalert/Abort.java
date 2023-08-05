package com.example.accidentdetectionandalert;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class abort extends AppCompatActivity {

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    boolean abortButtonClicked = false;
    boolean messageSent = false;

    Button abortBt;
    LocationFinder finder;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abort);

        abortBt = findViewById(R.id.abort1);

        // Check and request SMS permission if not granted
        if (checkSMSPermission()) {
            // Permission already granted, proceed with the logic
            init();
        } else {
            // Request permission
            requestSMSPermission();
        }
    }

    // Check if SMS permission is granted
    private boolean checkSMSPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SmsManager)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request SMS permission
    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SmsManager},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the logic
                init();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Permission denied. Cannot send SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void init() {
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
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                    Toast.makeText(abort.this, "SHAKING", Toast.LENGTH_SHORT).show();


                } else {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
                }
            }
        }, 2000);

        finder = new LocationFinder(this);
        if (finder.canGetLocation()) {
            latitude = finder.getLatitude();
            longitude = finder.getLongitude();
        } else {
            finder.showSettingsAlert();
         }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!abortButtonClicked && !messageSent) {
                    sendMessages();
                }
            }
        }, 15000);

        abortBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abortButtonClicked = true;
                finish();
            }
        });
    }

    private void sendMessages() {
        Toast.makeText(abort.this, "Latitude" + latitude + " Longitude" + longitude, Toast.LENGTH_LONG).show();
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(MainActivity.no1, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, null, null);
        sm.sendTextMessage(MainActivity.no1, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml", null, null);
        sm.sendTextMessage(MainActivity.no2, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, null, null);
        sm.sendTextMessage(MainActivity.no2, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml", null, null);
        sm.sendTextMessage(MainActivity.no3, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "\nMy Blood Group is = " + MainActivity.bgrp, null, null);
        sm.sendTextMessage(MainActivity.no3, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&output=kml", null, null);

        // Mark the message as sent
        messageSent = true;

        Toast.makeText(abort.this, "MESSAGE SEND", Toast.LENGTH_SHORT).show();
    }
}
