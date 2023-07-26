package com.example.accidentdetectionandalert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {
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


    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    /* // LineChart variables
     LineChart lineChart;
     ArrayList<Entry> values;
     ILineDataSet lineDataSet;
     LineData lineData;
     int xValue = 0;
 */
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
                        break;

                    case R.id.serviceStop:
                        Intent stopIntent = new Intent(getApplicationContext(), AccelerometerService.class);
                        stopService(stopIntent);
                        Toast.makeText(getApplicationContext(), "Service Stop", Toast.LENGTH_SHORT).show();
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


/*
        // LineChart initialization
        lineChart = findViewById(R.id.line_chart);
        values = new ArrayList<>();
        lineDataSet = new LineDataSet(values, "Accelerometer Data");
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Initialize sensor variables
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register the sensor listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Start the accelerometer service
        startService(new Intent(this, AccelerometerService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Start the accelerometer service
        startService(new Intent(this, AccelerometerService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener to save battery
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Update the acceleration value when the sensor data changes
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the overall acceleration
            acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            // Update the LineChart with the new acceleration value
            updateLineChartWithAccelerometerData(acceleration);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }


    private void updateLineChartWithAccelerometerData(float acceleration) {
        LineData data = lineChart.getData();
        ILineDataSet dataSet = data.getDataSetByIndex(0);

        if (dataSet == null) {
            dataS et = createSet();
            data.addDataSet(dataSet);
        }

        // Add new data entry with the current timestamp and accelerometer value
        long timestamp = System.currentTimeMillis();
        data.addEntry(new Entry(dataSet.getEntryCount(), acceleration), 0);

        // Limit the number of visible entries to 50 (adjust as needed)
        int visibleRange = 1000;
        int entryCount = dataSet.getEntryCount();
        if (entryCount > visibleRange) {
            dataSet.removeEntry(0); // Remove the oldest entry from the dataset
            for (int i = 0; i < entryCount; i++) {
                Entry entry = dataSet.getEntryForIndex(i);
                entry.setX(entry.getX() - 1); // Shift the x-values to the left
            }
        }

        // Notify the chart that the data has changed
        data.notifyDataChanged();

        // Move the chart view to the latest entry
        lineChart.moveViewToX(dataSet.getEntryCount() - 1);

        // Refresh the chart
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Accelerometer Data");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
*/

            // LineChart initialization
            lineChart = findViewById(R.id.lineChart);
            initLineChart();

            // Simulate real-time data updates (You can replace this with actual sensor data)
            handler = new Handler();
            dataRunnable = new Runnable() {
                @Override
                public void run() {
                    updateData();
                    handler.postDelayed(this, 1000); // Update chart every 1 second
                }
            };
            handler.post(dataRunnable);
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
        lineChart.invalidate();
    }

    private void updateData() {
        // Generate random y-values for the chart (You can replace this with actual sensor data)
        Random random = new Random();
        float value = random.nextFloat() * 10;

        // Add the new data entry to the chart
        entries.add(new Entry(xValue, value));
        dataSet.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMaximum(10); // Display 10 entries at a time
        lineChart.moveViewToX(xValue); // Move the chart view to the latest entry
        lineChart.invalidate();

        xValue++; // Increment x-axis value for the next data point
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(dataRunnable); // Stop the data updates when the Activity is destroyed
        super.onDestroy();
    }
}
}