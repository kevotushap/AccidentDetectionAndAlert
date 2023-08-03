package com.example.accidentdetectionandalert;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener {
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
    private final float receivedAcceleration = 0.0f; // Declare as a global variable
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

    private static final int PERMISSION_REQUEST_CODE = 1;

    private final BroadcastReceiver accelerometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float receivedAcceleration = intent.getFloatExtra("ACCELERATION", 0);
            if (lineChart.getVisibility() == View.VISIBLE) {
                acceleration = receivedAcceleration; // Update the acceleration value
                updateLineChartWithAccelerometerData(receivedAcceleration, lineChart, entries, xValue);
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the required permissions are granted, and request them if not
        if (checkPermissions()) {
            // Permissions are already granted, continue with your app logic
            init();
        } else {
            // Request permissions
            requestPermissions();
        }
        // Initialize the LineChart after setting the layout
        initLineChart();
    }

    // Check if the required permissions are granted
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    // Request permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.BODY_SENSORS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    // Handle permission request result
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, continue with your app logic
                init();
            } else {
                // Permissions denied, handle accordingly (e.g., show a message or exit the app)
                Toast.makeText(this, "Permissions denied! App may not work properly.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
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

        // Register the accelerometer receiver to receive data from AccelerometerService
        LocalBroadcastManager.getInstance(this).registerReceiver(accelerometerReceiver,
                new IntentFilter("ACCELEROMETER_DATA"));

        // Simulate real-time data updates with actual sensor data from the accelerometer
        startLineChartUpdates();
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

            // Increment the xValue by the time interval (e.g., 5 seconds per update)
            xValue +=5;

            // Update the LineChart with accelerometer data
            updateLineChartWithAccelerometerData(receivedAcceleration, lineChart, entries, xValue);

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

        // Applying custom colors to the chart
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setGridBackgroundColor(Color.LTGRAY);

        // Customizing axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Convert the float value to time representation (e.g., 10:30 AM)
                // You need to implement this conversion based on your time data
                // For example: return formatTime(value);
                return formatTime((int) value);
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable the right Y-axis

        // Enabling animations
        lineChart.animateX(1500);


        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        // Register OnChartValueSelectedListener
        lineChart.setOnChartValueSelectedListener(this);

        // Customize the appearance of the chart
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText(""); // Remove the description label
        lineChart.setDescription(description);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setVisibility(View.GONE);
        lineChart.invalidate();
    }

    public static void updateLineChartWithAccelerometerData(final float receivedAcceleration, final LineChart lineChart, final List<Entry> entries, final int xValue) {
        // Run on the UI thread to update the chart
        lineChart.post(new Runnable() {
            @Override
            public void run() {
                // Add the new data entry to the chart
                entries.add(new Entry(xValue, receivedAcceleration));

                // Check the size of the entries array before updating the LineChart
                if (entries.size() > 0) {
                    LineDataSet dataSet = new LineDataSet(entries, "Data");
                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                    dataSet.setDrawCircles(false);
                    dataSet.setDrawValues(false);

                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);
                    lineChart.moveViewToX(xValue); // Move the chart view to the latest entry
                    lineChart.setVisibleXRangeMaximum(5); // Display 5 seconds of data at a time
                    lineChart.invalidate(); // Refresh the chart
                } else {
                    // Handle the case where the entries array is empty
                    // You can show an error message or take appropriate action
                    // For example, you can display a toast message
                    Toast.makeText(lineChart.getContext(), "No data available for the chart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startLineChartUpdates() {
        // Simulate real-time data updates with actual sensor data from the accelerometer
        handler = new Handler();
        dataRunnable = new Runnable() {
            @Override
            public void run() {
                if (lineChart.getVisibility() == View.VISIBLE) {
                    updateLineChartWithAccelerometerData(acceleration, lineChart, entries, xValue);
                }
                handler.postDelayed(this, 5000); // Update chart every 5 seconds
            }
        };
        handler.post(dataRunnable);
    }

    private void stopLineChartUpdates() {
        // Stop the data updates when the Activity is destroyed
        if (handler != null) {
            handler.removeCallbacks(dataRunnable);
        }
    }

    private void startAccelerometerService() {
        // Start the AccelerometerService
        Intent i = new Intent(getApplicationContext(), AccelerometerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    private void stopAccelerometerService() {
        // Stop the AccelerometerService
        Intent stopIntent = new Intent(getApplicationContext(), AccelerometerService.class);
        stopService(stopIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the LineChart updates and AccelerometerService
        stopLineChartUpdates();
        stopAccelerometerService();

        // Unregister the sensor listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        // Unregister the accelerometer receiver from LocalBroadcastManager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accelerometerReceiver);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // Handle point click event here
        int index = (int) e.getX(); // Get the index of the clicked point
        float acceleration = e.getY(); // Get the y-axis value of the clicked point

        String message = "Point " + index + " clicked. Acceleration: " + acceleration;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {
        // Handle when nothing is selected (Optional)
        // You can leave it empty or implement any specific action
    }

    private String formatTime(int value) {
        // Implement your time formatting logic here
        // For example: return formatted time in "HH:mm" format
        // or any other representation that fits your use case
        return String.format("%02d:%02d", value / 60, value % 60);
    }
}  