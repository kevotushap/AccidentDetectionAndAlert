package com.example.accidentdetectionandalert;

        import android.app.Notification;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Color;
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

        import com.github.mikephil.charting.components.YAxis;
        import com.github.mikephil.charting.data.Entry;
        import com.github.mikephil.charting.data.LineData;
        import com.github.mikephil.charting.data.LineDataSet;
        import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
        import com.github.mikephil.charting.utils.ColorTemplate;
        import com.github.mikephil.charting.charts.LineChart;


        import java.util.ArrayList;

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
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private boolean isChartUpdating = false;

    // LineChart variables
    LineChart lineChart;
    ArrayList<Entry> values;
    ILineDataSet lineDataSet;
    LineData lineData;
    int xValue = 0;

    private void startLineChartUpdates() {
        if (!isChartUpdating) {
            // Initialize the  LineChart and other variables
            lineChart = new LineChart(this);
            values = new ArrayList<>();
            lineDataSet = createSet();
            lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);

            // Start updating the LineChart with accelerometer data
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            isChartUpdating = true;
        }
    }
    private void stopLineChartUpdates() {
        // Stop updating the LineChart
        sensorManager.unregisterListener(this);
        isChartUpdating = false;
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

 /*
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

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

        // Start updating the LineChart
        startLineChartUpdates();

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
        stopLineChartUpdates();
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

    private void startLineChartUpdates() {
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

    private void updateLineChartWithAccelerometerData(float acceleration) {
        LineData data = lineChart.getData();
        ILineDataSet dataSet = data.getDataSetByIndex(0);

        if (dataSet == null) {
            dataSet = createSet();
            data.addDataSet(dataSet);
        }

        // Add new data entry with the current timestamp and accelerometer value
        long timestamp = System.currentTimeMillis();
        data.addEntry(new Entry(dataSet.getEntryCount(), acceleration), 0);

        // Limit the number of visible entries to 50 (adjust as needed)
       /*  int visibleRange = 1000;
        int entryCount = dataSet.getEntryCount();
        if (entryCount > visibleRange) {
            dataSet.removeEntry(0); // Remove the oldest entry from the dataset
            for (int i = 0; i < entryCount; i++) {
                Entry entry = dataSet.getEntryForIndex(i);
                entry.setX(entry.getX() - 1); // Shift the x-values to the left
            }
        }*/

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
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
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
}