package com.mmussol.pistormsandroidthings.pistorms;

import java.io.IOException;

public class Led implements Constants {

    private PiStorms mPiStorms;
    private PS_Led mLed;

    public Led(PS_Led led) throws IOException {
        mPiStorms = PiStorms.getPiStorms();
        mLed = led;
    }

    public void set(int red, int green, int blue)
            throws InterruptedException, IOException {
        mPiStorms.setLed(mLed, red, green, blue);
    }
}
