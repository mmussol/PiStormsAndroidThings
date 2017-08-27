package com.mmussol.pistormsandroidthings.pistorms;

import android.util.Log;
import java.io.IOException;

public class PspController extends Sensor implements Constants {

    private static PiStorms mPiStorms;
    private static PS_Port mPort;

    private static final int PSP_CTRL_I2C_ADDR = 0x02;

    private static final int PSP_CTRL_COMMAND_I = 0x49; // Initialize PSP wireless dongle

    private static final int PSP_CTRL_MODE = 0x41;
    private static final int PSP_CTRL_BUTTON_SET_1 = 0x42;
    private static final int PSP_CTRL_BUTTON_SET_2 = 0x43;
    private static final int PSP_CTRL_JOYSTICK_LEFT_X = 0x44;
    private static final int PSP_CTRL_JOYSTICK_LEFT_Y = 0x45;
    private static final int PSP_CTRL_JOYSTICK_RIGHT_X = 0x46;
    private static final int PSP_CTRL_JOYSTICK_RIGHT_Y = 0x47;

    public PspController(PS_Port port) throws IOException, InterruptedException {
        mPiStorms = PiStorms.getPiStorms();
        mPort = port;

        int i2cAddr = PSP_CTRL_I2C_ADDR;
        try {
            mPiStorms.addI2CSensor(this, mPort, i2cAddr);
            mPiStorms.writeByte(mPort, PSP_CTRL_MODE, (byte) PSP_CTRL_COMMAND_I);
        } catch (IOException e) {
            String error = "Could not communicate with PspController I2C device: " + port;
            Log.e(PS_TAG, error);
            throw new IOException(error);
        }
    }

    public void getButtons(PspControllerButtons b) throws IOException, InterruptedException {

        byte[] buffer = new byte[6];
        mPiStorms.readBuffer(mPort, PSP_CTRL_BUTTON_SET_1, buffer, buffer.length);

        /* Somethings wrong with the MSB value read back. Seems like the MSB value
         * shows up in the next byte (sort-of). Need to debug this further.
         * Possibly because I2C is running too fast, 400Kbps? Not sure
         * how to change this with current version Android Things.
         * For now just ignore MSB bit (some buttons won't work).
         *
         * See https://issuetracker.google.com/issues/65046298
         */

        b.left = false; // TODO: (~(buffer[0] >> 7) & 0x01) == 0x01;
        b.sqr = false; // TODO: (~(buffer[1] >> 7) & 0x01) == 0x01;
        b.ljx = buffer[2] & 0x7f; // TODO: 0xff;
        b.ljy = buffer[3] & 0x7f; // TODO: 0xff;
        b.rjx = buffer[4] & 0x7f; // TODO: 0xff;
        b.rjy = buffer[5] & 0x7f; // TODO: 0xff;

        b.ljb   = (~(buffer[0] >> 1) & 0x01) == 0x01;
        b.rjb   = (~(buffer[0] >> 2) & 0x01) == 0x01;
        b.up    = (~(buffer[0] >> 4) & 0x01) == 0x01;
        b.right = (~(buffer[0] >> 5) & 0x01) == 0x01;
        b.down  = (~(buffer[0] >> 6) & 0x01) == 0x01;
        b.l2 = (~(buffer[1]) & 0x01) == 0x01;
        b.r2 = (~(buffer[1] >> 1) & 0x01) == 0x01;
        b.l1 = (~(buffer[1] >> 2) & 0x01) == 0x01;
        b.r1 = (~(buffer[1] >> 3) & 0x01) == 0x01;
        b.tri = (~(buffer[1] >> 4) & 0x01) == 0x01;
        b.cir = (~(buffer[1] >> 5) & 0x01) == 0x01;
        b.cro = (~(buffer[1] >> 6) & 0x01) == 0x01;

        /*
        if (b.l1) Log.d(TAG, "l1");
        if (b.l2) Log.d(TAG, "l2");
        if (b.r1) Log.d(TAG, "r1");
        if (b.r2) Log.d(TAG, "r2");
        if (b.up) Log.d(TAG, "up");
        if (b.down) Log.d(TAG, "down");
        if (b.left) Log.d(TAG, "left");
        if (b.right) Log.d(TAG, "right");
        if (b.tri) Log.d(TAG, "tri");
        if (b.sqr) Log.d(TAG, "sqr");
        if (b.cir) Log.d(TAG, "cir");
        if (b.cro) Log.d(TAG, "cro");
        if (b.ljb) Log.d(TAG, "ljb");
        if (b.rjb) Log.d(TAG, "rjb");
        if (b.ljx != 0) Log.d(TAG, "ljx = " + b.ljx);
        if (b.ljy != 0) Log.d(TAG, "ljy = " + b.ljy);
        if (b.rjx != 0) Log.d(TAG, "rjx = " + b.rjx);
        if (b.rjy != 0) Log.d(TAG, "rjy = " + b.rjy);
        */

    }
}
