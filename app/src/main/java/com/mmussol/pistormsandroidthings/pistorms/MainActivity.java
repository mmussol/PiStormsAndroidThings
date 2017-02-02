package com.mmussol.pistormsandroidthings.pistorms;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.mmussol.pistormsandroidthings.R;
import com.mmussol.pistormsandroidthings.programs.Demo;

public class MainActivity extends Activity implements Constants, PiStormsListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Thread mDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PiStorms piStorms = PiStorms.getPiStorms();
            piStorms.addListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create PiStorms");
        }
    }

    @Override
    public void onProgramGo() {
        mDemo = new Demo();
        mDemo.start();
    }

    @Override
    public void onProgramStop() {
    }
}
