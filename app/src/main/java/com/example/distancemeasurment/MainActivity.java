package com.example.distancemeasurment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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


    //kamera nowa
    private TextureView textureView;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }



    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.btnCapture:
                System.out.println("Azimuth: " + floatOrientation[0] + "\nPitch: " + floatOrientation[1] + "\nRoll: " + floatOrientation[2]);
                TextView txtMessage = findViewById(R.id.textView);

                double distance = calculateDistance(floatOrientation);

                txtMessage.setText(
                        "Azimuth: " + floatOrientation[0] +
                        "\nPitch: " + floatOrientation[1] +
                        "\nRoll: " + floatOrientation[2] +
                        "\nDistance: " + distance);
                break;

            case R.id.btnHelp:
                startActivity(new Intent(MainActivity.this, PopHelp.class));
                break;

            default:
                break;
        }
    }

    public double calculateDistance(float [] floatOrientation) {
        double distance;

        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            distance = 1.5 * Math.tan(-floatOrientation[1]);
        } else {
            distance = 1.5 * Math.tan(-floatOrientation[2]);
        }
        return distance;
    }



}