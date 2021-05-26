package com.example.distancemeasurment;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

public class PopHelp extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width *.8), (int)(height *.8));

        //Ustawienie listenera na btnClose
        Button closeButton = findViewById(R.id.btnClose);
        closeButton.setOnClickListener(this);
    }

    public void onClick (View v) {
        switch (v.getId()) {

            case R.id.btnHelp:
                finish();
                break;
            case R.id.btnClose:
                finish();
                break;
            default:
                break;
        }
    }
}
