package com.mmussol.pistormsandroidthings.pistorms;

import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mmussol.pistormsandroidthings.R;
import com.mmussol.pistormsandroidthings.programs.Demo;

public class MainActivity extends Activity implements Constants, PiStormsListener, SurfaceHolder.Callback {

    private PiStorms mPiStorms;
    private Thread mDemo;
    SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.pistorms_surface_view);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(MainActivity.this);
        try {
            mPiStorms = PiStorms.getPiStorms();
            mPiStorms.addListener(this);
        } catch (Exception e) {
            Log.e(PS_TAG, "Failed to create PiStorms");
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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        int densityDpi = 30;
        VirtualDisplay virtualDisplay = dm.createVirtualDisplay("test", PS_TFT_WIDTH, PS_TFT_HEIGHT, densityDpi,
                surfaceHolder.getSurface(), DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION);
        Display display = virtualDisplay.getDisplay();
        mPiStorms.addDisplay(MainActivity.this, mSurfaceView, display);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mPiStorms.removeDisplay();
    }
}
