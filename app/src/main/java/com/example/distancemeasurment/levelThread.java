package com.example.distancemeasurment;

import android.app.Activity;
import android.content.res.Configuration;

public class levelThread extends Thread {


    //Ustawianie listenera, który będzie będzie dawał info o potrzebie wypoziomowania
    public interface levelThreadListener {
        public void levelingNeeded(boolean needLeftRotation);
        public void levelResetNeeded();
    }

    private levelThreadListener listener;

    public void setlevelThreadListener(levelThreadListener listener) {
        this.listener = listener;
    }



    public Activity activity;
    public float[] floatOrientation;

    public levelThread(Activity _activity, float[] _floatOrientation){

        this.activity = _activity;
        this.floatOrientation = _floatOrientation;
        this.listener = null;
    }

    @Override
    public void run() {

        while(true) {

            if(activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {     //Telefon pionowo
                checkLevel(2);
            } else {                                                                                                //Telefon poziomo
                checkLevel(1);
            }

        try {
            Thread.sleep(200);                                 //Co 200ms sprawdzamy, czy urządzenie jest pionowo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }
    }

    public void checkLevel(int orientationId) {
        double trigger;

        if(orientationId == 2) {
            trigger = -0.15;
            if(floatOrientation != null) {
                if(floatOrientation[orientationId] < trigger) {
                    listener.levelingNeeded(false);
                } else if(floatOrientation[orientationId] > -trigger) {
                    listener.levelingNeeded(true);
                } else {
                    listener.levelResetNeeded();
                }
            } else {
                System.out.println("Nie można odczytać orientacji");
            }
        } else {
            trigger = 0.05;
            if(floatOrientation != null) {
                if(floatOrientation[orientationId] > trigger) {
                    listener.levelingNeeded(false);
                } else if(floatOrientation[orientationId] < -trigger) {
                    listener.levelingNeeded(true);
                } else {
                    listener.levelResetNeeded();
                }
            } else {
                System.out.println("Nie można odczytać orientacji");
            }
        }

        return;
    }


};
