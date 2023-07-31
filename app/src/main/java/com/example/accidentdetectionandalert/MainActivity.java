package com.example.accidentdetectionandalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final String FILE_NAME = "example.txt";
    String filepath;
    String firstNum, bloodGrp, secondNum, thirdNum;
    public static String no1, no2, no3, bgrp;
    EditText txt1, txt2, txt3, txtO;
    TextView contactTextView;
    Button btsave, btlod, bton;
    double latitude, longitude;
    LocationFinder finder;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private LineChart lineChart;
    private LineDataSet dataSet;
    private List<Entry> entries;
    private int xValue = 0;
    private Handler handler;
    private Runnable dataRunnable;

    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;


    private BroadcastReceiver accelerometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float acceleration = intent.getFloatExtra("ACCELERATION", 0);
            updateLineChartWithAccelerometerData();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactTextView = findViewById(R.id.contact);
        txt1 = findViewById(R.id.num1);
        txt2 = findViewById(R.id.num2);
        txt3 = findViewById(R.id.num3);
        txtO = findViewById(R.id.bloodgroup);
        btsave = findViewById(R.id.save);
        btlod = findViewById(R.id.load);

        filepath = "MyFileDir";

        btsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txt1.getText() != null)
                    firstNum = txt1.getText().toString();
                if (txt2.getText() != null)
                    secondNum = txt2.getText().toString();
                if (txt3.getText() != null)
                    thirdNum = txt3.getText().toString();
                if (txtO.getText() != null)
                    bloodGrp = txtO.getText().toString();
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    osw.append(firstNum);
                    osw.append("\n");
                    osw.append(secondNum);
                    osw.append("\n");
                    osw.append(thirdNum);
                    osw.append("\n");
                    osw.append(bloodGrp);
                    osw.close();
                    fos.close();
                    txt1.getText().clear();
                    txt2.getText().clear();
                    txt3.getText().clear();
                    txtO.getText().clear();
                    Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btlod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInputStream fis = null;
                try {
                    fis = openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    no1 = br.readLine();
                    no2 = br.readLine();
                    no3 = br.readLine();
                    bgrp = br.readLine();

                    br.close();
                    txt1.setText(no1);
                    txt2.setText(no2);
                    txt3.setText(no3);
                    txtO.setText(bgrp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer);

        //adding customised toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Save Drive");
        toolbar.setBackgroundColor(getColor(R.color.purple_700));
        //setSupportActionBar(toolbar);

        //toggle button
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //when an item is selected from menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.serviceActivate:
                        Intent i = new Intent(getApplicationContext(), AccelerometerService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(i);
                        } else {
                            startService(i);
                        }
                        Toast.makeText(getApplicationContext(), "Service On", Toast.LENGTH_SHORT).show();
                        // Open LineChart
                        lineChart.setVisibility(View.VISIBLE);
                        break;
                    case R.id.serviceStop:
                        Intent stopIntent = new Intent(getApplicationContext(), AccelerometerService.class);
                        stopService(stopIntent);
                        Toast.makeText(getApplicationContext(), "Service Stop", Toast.LENGTH_SHORT).show();
                        // Hide LineChart
                        lineChart.setVisibility(View.GONE);
                        break;

                    case R.id.settings:
                        Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.exit:
                        Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // LineChart initialization
        lineChart = findViewById(R.id.line_chart);
        initLineChart();

        // Register the accelerometer sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                // Handle the case where the accelerometer sensor is not available on the device
                Toast.makeText(this, "Accelerometer sensor not available.", Toast.LENGTH_SHORT).show();
            }
        }

        // Simulate real-time data updates with actual sensor data from the accelerometer
        handler = new Handler();
        dataRunnable = new Runnable() {
            @Override
            public void run() {
                updateLineChartWithAccelerometerData();
                handler.postDelayed(this, 1000); // Update chart every 1 second
            }
        };
        handler.post(dataRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the accelerometer receiver to receive data from AccelerometerService
        LocalBroadcastManager.getInstance(this).registerReceiver(accelerometerReceiver,
                new IntentFilter("ACCELEROMETER_DATA"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the accelerometer receiver to avoid leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accelerometerReceiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Get the accelerometer data here
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the acceleration
            acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            // Update the LineChart with accelerometer data
            updateLineChartWithAccelerometerData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void initLineChart() {
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "Real-Time Data");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize the appearance of the chart
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText(""); // Remove the description label
        lineChart.setDescription(description);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setVisibility(View.GONE); // Initially hide the chart
        lineChart.invalidate();
    }

    private void updateLineChartWithAccelerometerData() {
        // Add the new data entry to the chart
        entries.add(new Entry(xValue, acceleration));
        dataSet.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMaximum(10); // Display 10 entries at a time
        lineChart.moveViewToX(xValue); // Move the chart view to the latest entry
        lineChart.invalidate();

        xValue++; // Increment x-axis value for the next data point
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this); // Unregister the accelerometer sensor
        }
        handler.removeCallbacks(dataRunnable); // Stop the data updates when the Activity is destroyed
    }
}

    /*// Register accelerometer receiver
    private void registerAccelerometerReceiver() {
        accelerometerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float acceleration = intent.getFloatExtra("ACCELERATION", 0);
                updateLineChartWithAccelerometerData(acceleration);
            }
        };

        IntentFilter filter = new IntentFilter("com.example.accidentdetectionandalert.ACCELEROMETER_DATA");
        registerReceiver(accelerometerReceiver, filter);
    }

        // Update LineChart with accelerometer data
        private void updateLineChartWithAccelerometerData ( float acceleration){
            entries.add(new Entry(entries.size(), acceleration));
            dataSet.notifyDataSetChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(10); // Display 10 entries at a time
            lineChart.moveViewToX(entries.size() - 1); // Move the chart view to the latest entry
            lineChart.invalidate();
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            unregisterReceiver(accelerometerReceiver); // Unregister the accelerometer receiver
        }
    }
}*/
