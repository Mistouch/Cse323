package com.example.deepfocus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener {
    private static final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 1;
    private static final int ACTIVITY_RECOGNITION = 1;
    //private static final int ACTIVITY_RECOGNITION = 1;
    private SensorManager mSensorManager= null;
    private Sensor stepSensor;

    private int totalStep=0;
    private int previousTotalStep=0;
    private ProgressBar progressBar;
    private TextView step;

    private Button stop;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
        }

        progressBar = findViewById(R.id.progress);
        step = findViewById(R.id.steps);

        stop = findViewById(R.id.StopUp);
        resetSteps();
        loadData();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    protected  void onResume()
    {
        super.onResume();

        if(stepSensor == null)
        {
            Toast.makeText(this, "This device has no  sensor",Toast.LENGTH_SHORT).show();
        }
        else
        {
            mSensorManager.registerListener((SensorEventListener) this,stepSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }


    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()== Sensor.TYPE_STEP_COUNTER)
        {
            totalStep = (int)event.values[0];
            int currentSteps = totalStep - previousTotalStep;
            step.setText(String.valueOf(currentSteps));
            progressBar.setProgress(currentSteps);
        }

    }

    private void resetSteps()
    {

        step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity2.this,"Long Press to reset",Toast.LENGTH_SHORT);
            }
        });

        step.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                previousTotalStep=totalStep;
                step.setText("0");
                totalStep=0;
                progressBar.setProgress(0);
                saveData();
                return true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calculate total steps before clicking the button
                int totalStepsBeforeStop = totalStep-previousTotalStep;
                if(totalStepsBeforeStop<0)
                    totalStepsBeforeStop=0;
                double calBurn= 1.0*totalStepsBeforeStop*0.04;
                // Display toast message with the total steps
                Toast.makeText(MainActivity2.this, "Total Steps: " + totalStepsBeforeStop, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity2.this, "Total Calorie Burn: " + calBurn, Toast.LENGTH_SHORT).show();

                // Start Profile activity
            }
        });
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes here
    }
    private void saveData()
    {
        SharedPreferences sharePref= getSharedPreferences("Mypref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePref.edit();
        editor.putString("Key1", String.valueOf(previousTotalStep));
    }

    private void loadData()
    {
        SharedPreferences sharePref= getSharedPreferences("Mypref", Context.MODE_PRIVATE);
        int saveNumber= (int) sharePref.getFloat("Key1",0f);
        previousTotalStep=saveNumber;
    }

}