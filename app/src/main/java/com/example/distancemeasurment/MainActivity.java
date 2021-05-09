package com.example.distancemeasurment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    //Dotyczące kamery
    private android.hardware.Camera camera;
    private FrameLayout frameLayout;
    private ShowCamera showCamera;

    //Dotyczące sensorów
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];
    private levelThread thread = new levelThread(this, floatOrientation);

    private int userHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

//        Chowanie navbara i statusbara
        View decorView = getWindow().getDecorView();
        final int uiOptions =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        //Pokazanie widoku z kamery na ekranie
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        camera = Camera.open(0);
        showCamera = new ShowCamera(this, camera);
        frameLayout.addView(showCamera);



        //Znajdowanie pozycji
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelerometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorEventListenerAccelerometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);

        //Ustawienie listenera na btnCapture
        Button captureButton = findViewById(R.id.btnCapture);
        captureButton.setOnClickListener(this);

        //Ustawienie listenera na btnHelp
        Button helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(this);

        //Ustawianie customowego listenera - uruchamia się w momencie zbyt mocnego przechylenia urządzenia
        thread.setlevelThreadListener(new levelThread.levelThreadListener() {
            @Override
            public void levelingNeeded(boolean needLeftRotation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        popLevelWarning(needLeftRotation);
                    }
                });
            }

            @Override
            public void levelResetNeeded() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetLevelPop();
                    }
                });
            }
        });

        //Uruchomienie wątku obsługującego poziomowanie urządzenia
        thread.start();
    }



    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.btnCapture:
                hideKeybaord(v);
                TextView resultText = findViewById(R.id.resultText);
                EditText inputText = findViewById(R.id.heightInput);

                String enteredHeight = inputText.getText().toString();

                double distance = calculateDistance(floatOrientation, enteredHeight);

                resultText.setText("Odległość: " + Math.round(distance * 100.0) / 100.0 + " m");
                break;

            case R.id.btnHelp:
                hideKeybaord(v);
                startActivity(new Intent(MainActivity.this, PopHelp.class));
                break;

            default:
                break;
        }
    }

    public double calculateDistance(float [] floatOrientation, String enteredHeight) {
        double distance;
        double phoneAttitudeInMeters;

        if(enteredHeight.equals("")) {
            Toast.makeText(this, "Podaj wzrost", Toast.LENGTH_SHORT).show();
            distance = 0;
        } else {
            userHeight = Integer.parseInt(enteredHeight);

            if (userHeight > 220)
                phoneAttitudeInMeters = (double) userHeight / 100;
            else
                phoneAttitudeInMeters = (double) userHeight / 100 - 0.3;

            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                distance = phoneAttitudeInMeters * Math.tan(-floatOrientation[1]);
            } else {
                distance = phoneAttitudeInMeters * Math.tan(-floatOrientation[2]);
            }
        }
        return distance;
    }

    public void popLevelWarning (boolean needLeftRotation) {
        if(needLeftRotation) {
            ImageView leftArrow = findViewById(R.id.arrowTurnLeft);
            TextView leftText = findViewById(R.id.textTurnLeft);

            leftArrow.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.VISIBLE);
        } else {
            ImageView rightArrow = findViewById(R.id.arrowTurnRight);
            TextView rightText = findViewById(R.id.textTurnRight);

            rightArrow.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.VISIBLE);
        }
        return;
    }

    public void resetLevelPop() {

            ImageView leftArrow = findViewById(R.id.arrowTurnLeft);
            TextView leftText = findViewById(R.id.textTurnLeft);

            leftArrow.setVisibility(View.GONE);
            leftText.setVisibility(View.GONE);

            ImageView rightArrow = findViewById(R.id.arrowTurnRight);
            TextView rightText = findViewById(R.id.textTurnRight);

            rightArrow.setVisibility(View.GONE);
            rightText.setVisibility(View.GONE);
    }

    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }
}