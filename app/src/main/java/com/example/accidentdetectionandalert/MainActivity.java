package com.example.accidentdetectionandalert;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.LineChart;
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

    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    // LineChart variables
    LineChart lineChart;
    ArrayList<DropBoxManager.Entry> values;
    LineDataSet lineDataSet;
    LineData lineData;
    int xValue = 0;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart);

        setTitle("LineChartActivity1");

        // ... Rest of the chart initialization code
        lineChart = findViewById(R.id.line_chart);
        values = new ArrayList<>();
        lineDataSet = new LineDataSet(values, "Accelerometer Data");
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);


        // Initialize the accelerometer service
        accelerometerService = new AccelerometerService();
        accelerometerService.setOnAccelerometerChangeListener(new AccelerometerService.OnAccelerometerChangeListener() {
            @Override
            public void onAccelerometerChange(float acceleration) {
                updateLineChartWithAccelerometerData(acceleration);
            }
        });

        // Start the accelerometer service
        startService(new Intent(this, AccelerometerService.class));
    }

    private void updateLineChartWithAccelerometerData(float acceleration) {
        LineData data = chart.getData();
        ILineDataSet dataSet = data.getDataSetByIndex(0);

        if (dataSet == null) {
            dataSet = createSet();
            data.addDataSet(dataSet);
        }

        // Add new data entry with the current timestamp and accelerometer value
        long timestamp = System.currentTimeMillis();
        data.addEntry(new Entry(timestamp, acceleration), 0);

        // Limit the number of visible entries to 50 (adjust as needed)
        int visibleRange = 50;
        if (data.getEntryCount() > visibleRange) {
            data.removeEntry(0);
        }

        // Notify the chart that the data has changed
        data.notifyDataChanged();

        // Move the chart view to the latest entry
        chart.moveViewToX(timestamp);

        // Refresh the chart
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Accelerometer Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
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
}

// ... Rest of the code (onOptionsItemSelected, saveToGallery, etc.)
